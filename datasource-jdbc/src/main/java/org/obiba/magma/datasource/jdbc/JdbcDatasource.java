package org.obiba.magma.datasource.jdbc;

import static org.obiba.magma.datasource.jdbc.JdbcValueTableWriter.ATTRIBUTE_METADATA_TABLE;
import static org.obiba.magma.datasource.jdbc.JdbcValueTableWriter.CATEGORY_METADATA_TABLE;
import static org.obiba.magma.datasource.jdbc.JdbcValueTableWriter.VARIABLE_METADATA_TABLE;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.sql.DataSource;

import liquibase.change.Change;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.sql.SqlStatement;
import liquibase.database.sql.visitor.SqlVisitor;
import liquibase.database.structure.DatabaseSnapshot;
import liquibase.database.structure.Table;
import liquibase.exception.JDBCException;
import liquibase.exception.UnsupportedChangeException;

import org.obiba.magma.ValueTable;
import org.obiba.magma.ValueTableWriter;
import org.obiba.magma.datasource.jdbc.support.NameConverter;
import org.obiba.magma.support.AbstractDatasource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.core.JdbcTemplate;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

public class JdbcDatasource extends AbstractDatasource {

  private static final Logger log = LoggerFactory.getLogger(JdbcDatasource.class);

  //
  // Constants
  //

  private static final String TYPE = "jdbc";

  //
  // Instance Variables
  //

  private Set<String> RESERVED_NAMES = ImmutableSet.of(VARIABLE_METADATA_TABLE, ATTRIBUTE_METADATA_TABLE, CATEGORY_METADATA_TABLE);

  private final JdbcTemplate jdbcTemplate;

  private final JdbcDatasourceSettings settings;

  private DatabaseSnapshot snapshot;

  //
  // Constructors
  //

  public JdbcDatasource(String name, DataSource datasource, JdbcDatasourceSettings settings) {
    super(name, TYPE);

    if(settings == null) {
      throw new IllegalArgumentException("null settings");
    }
    if(datasource == null) {
      throw new IllegalArgumentException("null datasource");
    }

    this.settings = settings;
    this.jdbcTemplate = new JdbcTemplate(datasource);
  }

  public JdbcDatasource(String name, DataSource datasource, String defaultEntityType, boolean useMetadataTables) {
    this(name, datasource, new JdbcDatasourceSettings(defaultEntityType, null, null, useMetadataTables));
  }

  //
  // AbstractDatasource Methods
  //

  /**
   * Returns a {@link ValueTableWriter} for writing to a new or existing {@link JdbcValueTable}.
   * 
   * Note: Newly created tables have a single entity identifier column, "entity_id".
   */
  @Override
  public ValueTableWriter createWriter(String tableName, String entityType) {
    if(entityType == null) {
      entityType = settings.getDefaultEntityType();
    }

    JdbcValueTable table;
    if(hasValueTable(tableName)) {
      table = (JdbcValueTable) getValueTable(tableName);
    } else {
      // Create a new JdbcValueTable. This will create the SQL table if it does not exist.
      JdbcValueTableSettings tableSettings = settings.getTableSettingsForMagmaTable(tableName);
      if(tableSettings == null) {
        tableSettings = new JdbcValueTableSettings(NameConverter.toSqlName(tableName), tableName, entityType, Arrays.asList("entity_id"));
      }
      table = new JdbcValueTable(this, tableSettings);
      addValueTable(table);
    }

    return new JdbcValueTableWriter(table);
  }

  @Override
  protected void onInitialise() {
  }

  @Override
  protected void onDispose() {
  }

  protected Set<String> getValueTableNames() {
    Set<String> names = new LinkedHashSet<String>();
    for(Table table : getDatabaseSnapshot().getTables()) {
      // Ignore tables with "reserved" names (i.e., the metadata tables).
      if(!RESERVED_NAMES.contains(table.getName().toLowerCase())) {
        // If a set of mapped tables has been defined, only include the tables in that set.
        if(settings.getMappedTables().isEmpty() || settings.getMappedTables().contains(table.getName())) {
          names.add(NameConverter.toMagmaName(table.getName()));
        }
      }
    }
    return names;
  }

  protected ValueTable initialiseValueTable(String tableName) {
    JdbcValueTableSettings tableSettings = settings.getTableSettingsForMagmaTable(tableName);
    if(tableSettings != null) {
      return new JdbcValueTable(this, tableSettings);
    } else {
      return new JdbcValueTable(this, getDatabaseSnapshot().getTable(tableName), settings.getDefaultEntityType());
    }
  }

  //
  // Methods
  //

  public JdbcDatasourceSettings getSettings() {
    return settings;
  }

  JdbcTemplate getJdbcTemplate() {
    return jdbcTemplate;
  }

  DatabaseSnapshot getDatabaseSnapshot() {
    if(snapshot == null) {
      snapshot = doWithDatabase(new DatabaseCallback<DatabaseSnapshot>() {

        @Override
        public DatabaseSnapshot doInDatabase(Database database) throws JDBCException {
          return database.createDatabaseSnapshot(null, null);

        }
      });
    }
    return snapshot;
  }

  void databaseChanged() {
    snapshot = null;
  }

  String escapeSqlTableName(String sqlTableName) {
    return getDatabaseSnapshot().getDatabase().escapeTableName(null, sqlTableName);
  }

  @SuppressWarnings("unchecked")
  <T> T doWithDatabase(final DatabaseCallback<T> databaseCallback) {
    return (T) jdbcTemplate.execute(new ConnectionCallback() {
      @Override
      public Object doInConnection(Connection con) throws SQLException, DataAccessException {
        try {
          return databaseCallback.doInDatabase(DatabaseFactory.getInstance().findCorrectDatabaseImplementation(con));
        } catch(JDBCException e) {
          throw new SQLException(e);
        }
      }
    });
  }

  /**
   * Callback used for accessing the {@code Database} instance in a safe and consistent way.
   * @param <T> the type of object returned by the callback if any
   */
  interface DatabaseCallback<T> {
    public T doInDatabase(Database database) throws JDBCException;
  }

  /**
   * An implementation of {@code DatabaseCallback} for issuing {@code Change} instances to the {@code Database}
   */
  static class ChangeDatabaseCallback implements DatabaseCallback<Object> {

    private final List<SqlVisitor> sqlVisitors;

    private final Iterable<Change> changes;

    ChangeDatabaseCallback(Change... changes) {
      this(Arrays.asList(changes));
    }

    ChangeDatabaseCallback(Iterable<Change> changes) {
      this(changes, new LinkedList<SqlVisitor>());
    }

    ChangeDatabaseCallback(Iterable<Change> changes, Iterable<? extends SqlVisitor> visitors) {
      if(changes == null) throw new IllegalArgumentException("changes cannot be null");
      if(visitors == null) throw new IllegalArgumentException("visitors cannot be null");
      this.changes = changes;
      this.sqlVisitors = ImmutableList.copyOf(visitors);
    }

    @Override
    public Object doInDatabase(Database database) throws JDBCException {
      try {
        for(Change change : changes) {
          if(log.isDebugEnabled()) {
            for(SqlStatement st : change.generateStatements(database)) {
              log.debug("Issuing statement: {}", st.getSqlStatement(database));
            }
          }
          change.executeStatements(database, sqlVisitors);
        }
      } catch(UnsupportedChangeException e) {
        throw new JDBCException(e);
      }
      return null;
    }
  }
}
