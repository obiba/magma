package org.obiba.magma.datasource.jdbc;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.sql.DataSource;

import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.structure.DatabaseSnapshot;
import liquibase.database.structure.Table;
import liquibase.exception.JDBCException;

import org.obiba.magma.MagmaRuntimeException;
import org.obiba.magma.ValueTable;
import org.obiba.magma.ValueTableWriter;
import org.obiba.magma.datasource.jdbc.support.NameConverter;
import org.obiba.magma.support.AbstractDatasource;
import org.springframework.jdbc.core.JdbcTemplate;

import com.google.common.collect.ImmutableSet;

public class JdbcDatasource extends AbstractDatasource {
  //
  // Constants
  //

  private static final String TYPE = "jdbc";

  //
  // Instance Variables
  //

  private Set<String> RESERVED_NAMES = ImmutableSet.of("variables");

  private Database database;

  private DatabaseSnapshot snapshot;

  private JdbcTemplate jdbcTemplate;

  private JdbcDatasourceSettings settings;

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
      table = new JdbcValueTable(this, new JdbcValueTableSettings(NameConverter.toSqlName(tableName), tableName, entityType, Arrays.asList("entity_id")));
      addValueTable(table);
    }

    return new JdbcValueTableWriter(table);
  }

  @Override
  protected void onInitialise() {
    try {
      database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(jdbcTemplate.getDataSource().getConnection());
      /*
       * if(snapshot.getTable("variables") == null) { CreateTableChange ctc = new CreateTableChange();
       * ctc.setTableName("variables");
       * 
       * ColumnConfig cc = new ColumnConfig(); cc.setName("name"); cc.setType("VARCHAR"); ctc.addColumn(cc);
       * ctc.executeStatements(database, (List<SqlVisitor>) Collections.EMPTY_LIST); }
       */
    } catch(JDBCException e) {
      throw new MagmaRuntimeException(e);
    } catch(SQLException e) {
      throw new MagmaRuntimeException(e);
    }

  }

  @Override
  protected void onDispose() {
    try {
      this.database.close();
    } catch(JDBCException e) {
    }
  }

  protected Set<String> getValueTableNames() {
    Set<String> names = new LinkedHashSet<String>();
    for(Table table : getDatabaseSnapshot().getTables()) {
      if(RESERVED_NAMES.contains(table.getName().toLowerCase()) == false) {
        names.add(table.getName());
      }
    }
    return names;
  }

  protected ValueTable initialiseValueTable(String tableName) {
    return new JdbcValueTable(this, getDatabaseSnapshot().getTable(tableName), "Participant");
  }

  //
  // Methods
  //

  public JdbcDatasourceSettings getSettings() {
    return settings;
  }

  Database getDatabase() {
    return database;
  }

  DatabaseSnapshot getDatabaseSnapshot() {
    if(snapshot == null) {
      try {
        snapshot = database.createDatabaseSnapshot(null, null);
      } catch(JDBCException e) {
        throw new MagmaRuntimeException(e);
      }
    }
    return snapshot;
  }

  void databaseChanged() {
    snapshot = null;
  }

  JdbcTemplate getJdbcTemplate() {
    return jdbcTemplate;
  }
}
