package org.obiba.magma.datasource.jdbc;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.sql.DataSource;

import org.obiba.magma.ValueTable;
import org.obiba.magma.ValueTableWriter;
import org.obiba.magma.datasource.jdbc.support.CreateTableChangeBuilder;
import org.obiba.magma.datasource.jdbc.support.MySqlEngineVisitor;
import org.obiba.magma.datasource.jdbc.support.NameConverter;
import org.obiba.magma.support.AbstractDatasource;
import org.obiba.magma.type.TextType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.core.JdbcTemplate;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import liquibase.change.Change;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.sql.SqlStatement;
import liquibase.database.sql.visitor.SqlVisitor;
import liquibase.database.structure.DatabaseSnapshot;
import liquibase.database.structure.Table;
import liquibase.exception.JDBCException;
import liquibase.exception.UnsupportedChangeException;

import static org.obiba.magma.datasource.jdbc.JdbcValueTableWriter.ATTRIBUTE_METADATA_TABLE;
import static org.obiba.magma.datasource.jdbc.JdbcValueTableWriter.CATEGORY_METADATA_TABLE;
import static org.obiba.magma.datasource.jdbc.JdbcValueTableWriter.VARIABLE_METADATA_TABLE;

public class JdbcDatasource extends AbstractDatasource {

  private static final Logger log = LoggerFactory.getLogger(JdbcDatasource.class);

  private static final Set<String> RESERVED_NAMES = ImmutableSet
      .of(VARIABLE_METADATA_TABLE, ATTRIBUTE_METADATA_TABLE, CATEGORY_METADATA_TABLE);

  private static final String TYPE = "jdbc";

  private final JdbcTemplate jdbcTemplate;

  private final JdbcDatasourceSettings settings;

  private DatabaseSnapshot snapshot;

  public JdbcDatasource(String name, DataSource datasource, JdbcDatasourceSettings settings) {
    super(name, TYPE);

    if(settings == null) {
      throw new IllegalArgumentException("null settings");
    }
    if(datasource == null) {
      throw new IllegalArgumentException("null datasource");
    }

    this.settings = settings;
    jdbcTemplate = new JdbcTemplate(datasource);
  }

  public JdbcDatasource(String name, DataSource datasource, String defaultEntityType, boolean useMetadataTables) {
    this(name, datasource, new JdbcDatasourceSettings(defaultEntityType, null, null, useMetadataTables));
  }

  //
  // AbstractDatasource Methods
  //

  /**
   * Returns a {@link ValueTableWriter} for writing to a new or existing {@link JdbcValueTable}.
   * <p/>
   * Note: Newly created tables have a single entity identifier column, "entity_id".
   */
  @SuppressWarnings({ "AssignmentToMethodParameter", "PMD.AvoidReassigningParameters" })
  @Nonnull
  @Override
  public ValueTableWriter createWriter(String tableName, @Nullable String entityType) {
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
        tableSettings = new JdbcValueTableSettings(NameConverter.toSqlName(tableName), tableName, entityType,
            Arrays.asList("entity_id"));
        settings.getTableSettings().add(tableSettings);
      }
      table = new JdbcValueTable(this, tableSettings);
      addValueTable(table);
    }

    return new JdbcValueTableWriter(table);
  }

  @Override
  protected void onInitialise() {
    if(getSettings().useMetadataTables()) {
      createMetadataTablesIfNotPresent();
    }
  }

  @Override
  protected void onDispose() {
  }

  @Override
  protected Set<String> getValueTableNames() {
    Set<String> names = new LinkedHashSet<String>();
    for(Table table : getDatabaseSnapshot().getTables()) {
      // Ignore tables with "reserved" names (i.e., the metadata tables).
      if(!RESERVED_NAMES.contains(table.getName().toLowerCase())) {
        // If a set of mapped tables has been defined, only include the tables in that set.
        if(settings.getMappedTables().isEmpty() || settings.getMappedTables().contains(table.getName())) {
          JdbcValueTableSettings tableSettings = settings.getTableSettingsForSqlTable(table.getName());
          if(tableSettings != null) {
            names.add(tableSettings.getMagmaTableName());
          } else {
            // Only add the table if it has a primary key
            if(!JdbcValueTable.getEntityIdentifierColumns(table).isEmpty()) {
              names.add(NameConverter.toMagmaName(table.getName()));
            }
          }
        }
      }
    }
    return names;
  }

  @Override
  protected ValueTable initialiseValueTable(String tableName) {
    JdbcValueTableSettings tableSettings = settings.getTableSettingsForMagmaTable(tableName);
    return tableSettings != null
        ? new JdbcValueTable(this, tableSettings)
        : new JdbcValueTable(this, getDatabaseSnapshot().getTable(tableName), settings.getDefaultEntityType());
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

  <T> T doWithDatabase(final DatabaseCallback<T> databaseCallback) {
    return jdbcTemplate.execute(new ConnectionCallback<T>() {
      @Nullable
      @Override
      public T doInConnection(Connection con) throws SQLException, DataAccessException {
        try {
          return databaseCallback.doInDatabase(DatabaseFactory.getInstance().findCorrectDatabaseImplementation(con));
        } catch(JDBCException e) {
          throw new SQLException(e);
        }
      }
    });
  }

  private void createMetadataTablesIfNotPresent() {
    List<Change> changes = new ArrayList<Change>();
    if(getDatabaseSnapshot().getTable(VARIABLE_METADATA_TABLE) == null) {
      CreateTableChangeBuilder builder = new CreateTableChangeBuilder();
      builder.tableName(VARIABLE_METADATA_TABLE).withColumn(JdbcValueTableWriter.VALUE_TABLE_COLUMN, "VARCHAR(255)")
          .primaryKey().withColumn("name", "VARCHAR(255)").primaryKey()
          .withColumn(JdbcValueTableWriter.VALUE_TYPE_COLUMN, "VARCHAR(255)").notNull()
          .withColumn("mime_type", "VARCHAR(255)").withColumn("units", "VARCHAR(255)")
          .withColumn("is_repeatable", "BOOLEAN").withColumn("occurrence_group", "VARCHAR(255)");
      changes.add(builder.build());
    }
    if(getDatabaseSnapshot().getTable(ATTRIBUTE_METADATA_TABLE) == null) {
      CreateTableChangeBuilder builder = new CreateTableChangeBuilder();
      builder.tableName(ATTRIBUTE_METADATA_TABLE).withColumn(JdbcValueTableWriter.VALUE_TABLE_COLUMN, "VARCHAR(255)")
          .primaryKey().withColumn(JdbcValueTableWriter.VARIABLE_NAME_COLUMN, "VARCHAR(255)").primaryKey()
          .withColumn(JdbcValueTableWriter.ATTRIBUTE_NAME_COLUMN, "VARCHAR(255)").primaryKey()
          .withColumn(JdbcValueTableWriter.ATTRIBUTE_LOCALE_COLUMN, "VARCHAR(20)").primaryKey()
          .withColumn(JdbcValueTableWriter.ATTRIBUTE_NAMESPACE_COLUMN, "VARCHAR(20)").primaryKey()
          .withColumn(JdbcValueTableWriter.ATTRIBUTE_VALUE_COLUMN,
              SqlTypes.sqlTypeFor(TextType.get(), SqlTypes.TEXT_TYPE_HINT_MEDIUM));
      changes.add(builder.build());
    }
    if(getDatabaseSnapshot().getTable(CATEGORY_METADATA_TABLE) == null) {
      CreateTableChangeBuilder builder = new CreateTableChangeBuilder();
      builder.tableName(CATEGORY_METADATA_TABLE).withColumn(JdbcValueTableWriter.VALUE_TABLE_COLUMN, "VARCHAR(255)")
          .primaryKey().withColumn(JdbcValueTableWriter.VARIABLE_NAME_COLUMN, "VARCHAR(255)").primaryKey()
          .withColumn(JdbcValueTableWriter.CATEGORY_NAME_COLUMN, "VARCHAR(255)").primaryKey()
          .withColumn(JdbcValueTableWriter.CATEGORY_CODE_COLUMN, "VARCHAR(255)")
          .withColumn(JdbcValueTableWriter.CATEGORY_MISSING_COLUMN, "BOOLEAN").notNull();
      changes.add(builder.build());
    }
    doWithDatabase(new ChangeDatabaseCallback(changes, ImmutableList.of(new MySqlEngineVisitor())));
  }

  /**
   * Callback used for accessing the {@code Database} instance in a safe and consistent way.
   *
   * @param <T> the type of object returned by the callback if any
   */
  interface DatabaseCallback<T> {
    @Nullable
    T doInDatabase(Database database) throws JDBCException;
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
      this(changes, Collections.<SqlVisitor>emptyList());
    }

    ChangeDatabaseCallback(Iterable<Change> changes, Iterable<? extends SqlVisitor> visitors) {
      if(changes == null) throw new IllegalArgumentException("changes cannot be null");
      if(visitors == null) throw new IllegalArgumentException("visitors cannot be null");
      this.changes = changes;
      sqlVisitors = ImmutableList.copyOf(visitors);
    }

    @Nullable
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
