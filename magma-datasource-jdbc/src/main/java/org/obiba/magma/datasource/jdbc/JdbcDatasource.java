package org.obiba.magma.datasource.jdbc;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;
import javax.sql.DataSource;
import javax.validation.constraints.NotNull;

import org.obiba.magma.ValueTable;
import org.obiba.magma.ValueTableWriter;
import org.obiba.magma.datasource.jdbc.support.CreateTableChangeBuilder;
import org.obiba.magma.datasource.jdbc.support.InsertDataChangeBuilder;
import org.obiba.magma.datasource.jdbc.support.MySqlEngineVisitor;
import org.obiba.magma.datasource.jdbc.support.TableUtils;
import org.obiba.magma.support.AbstractDatasource;
import org.obiba.magma.support.Initialisables;
import org.obiba.magma.type.TextType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import liquibase.change.Change;
import liquibase.change.core.InsertDataChange;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.DatabaseList;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.DatabaseException;
import liquibase.exception.LiquibaseException;
import liquibase.snapshot.DatabaseSnapshot;
import liquibase.snapshot.SnapshotControl;
import liquibase.snapshot.SnapshotGeneratorFactory;
import liquibase.sql.visitor.SqlVisitor;
import liquibase.statement.SqlStatement;
import liquibase.structure.core.Table;

import static org.obiba.magma.datasource.jdbc.JdbcValueTableWriter.*;
import static org.obiba.magma.datasource.jdbc.support.TableUtils.newTable;

public class JdbcDatasource extends AbstractDatasource {

  private static final Logger log = LoggerFactory.getLogger(JdbcDatasource.class);

  public static final String VARIABLES_MAPPING = "variables_mapping";

  private static final Set<String> RESERVED_NAMES = ImmutableSet
      .of(VARIABLES_TABLE, VARIABLE_ATTRIBUTES_TABLE, CATEGORIES_TABLE);

  private static final String TYPE = "jdbc";

  private final JdbcTemplate jdbcTemplate;

  private final JdbcDatasourceSettings settings;

  private DatabaseSnapshot snapshot;

  private Map<String, String> valueTableMap;

  @SuppressWarnings("ConstantConditions")
  public JdbcDatasource(String name, @NotNull DataSource datasource, @NotNull JdbcDatasourceSettings settings) {
    super(name, TYPE);
    if(settings == null) throw new IllegalArgumentException("null settings");
    if(datasource == null) throw new IllegalArgumentException("null datasource");
    this.settings = settings;
    jdbcTemplate = new JdbcTemplate(datasource);
  }

  public JdbcDatasource(String name, DataSource datasource, String defaultEntityType, boolean useMetadataTables) {
    this(name, datasource, new JdbcDatasourceSettings(defaultEntityType, null, null, useMetadataTables));
  }

  //
  // AbstractDatasource Methods
  //
  @Override
  public boolean canDropTable(String tableName) {
    return hasValueTable(tableName);
  }

  @Override
  public void dropTable(@NotNull String tableName) {
    JdbcValueTable table = (JdbcValueTable) getValueTable(tableName);
    table.drop();
    removeValueTable(table);
    valueTableMap.remove(tableName);
  }

  @Override
  public boolean canRenameTable(String tableName) {
    return hasValueTable(tableName);
  }

  @Override
  public void renameTable(String tableName, String newName) {
    String tableSqlName = getValueTableMap().get(tableName);
    getValueTableMap().put(newName, tableSqlName);
    getValueTableMap().remove(tableName);
  }

  @Override
  public boolean canDrop() {
    return true;
  }

  @Override
  public void drop() {
    for(ValueTable valueTable : ImmutableList.copyOf(getValueTables())) {
      dropTable(valueTable.getName());
    }
    // TODO drop datasource info if any
  }

  /**
   * Returns a {@link ValueTableWriter} for writing to a new or existing {@link JdbcValueTable}.
   * <p/>
   * Note: Newly created tables have a single entity identifier column, "entity_id".
   */
  @SuppressWarnings({ "AssignmentToMethodParameter", "PMD.AvoidReassigningParameters" })
  @NotNull
  @Override
  public ValueTableWriter createWriter(@NotNull String tableName, @NotNull String entityType) {
    //noinspection ConstantConditions
    if(entityType == null) {
      entityType = settings.getDefaultEntityType();
    }

    JdbcValueTable table;

    if(hasValueTable(tableName)) {
      table = (JdbcValueTable) getValueTable(tableName);
    } else {
      JdbcValueTableSettings tableSettings = settings.getTableSettingsForMagmaTable(tableName);

      if(tableSettings == null) {
        tableSettings = new JdbcValueTableSettings(generateTableName(tableName), tableName, entityType,
            Arrays.asList("entity_id"));
        settings.getTableSettings().add(tableSettings);
      }

      table = new JdbcValueTable(this, tableSettings);
      Initialisables.initialise(table);
      addValueTable(table);

      if (getSettings().isUseMetadataTables()) {
        InsertDataChange idc = InsertDataChangeBuilder.newBuilder() //
            .tableName(VALUE_TABLES_TABLE) //
            .withColumn(DATASOURCE_COLUMN, getName()) //
            .withColumn(NAME_COLUMN, tableName) //
            .withColumn(ENTITY_TYPE_COLUMN, tableSettings.getEntityType()) //
            .withColumn(SQL_NAME_COLUMN, tableSettings.getSqlTableName()) //
            .build();

        doWithDatabase(new ChangeDatabaseCallback(idc));
      }
    }

    return new JdbcValueTableWriter(table);
  }

  @Override
  protected void onInitialise() {
    if(getSettings().isUseMetadataTables()) {
      createMetadataTablesIfNotPresent();
    }
  }

  @Override
  protected void onDispose() {
  }

  @Override
  protected Set<String> getValueTableNames() {
    if(!getValueTableMap().isEmpty()) return getValueTableMap().keySet();

    Set<String> names = getSettings().isUseMetadataTables()
        ? getRegisteredValueTableNames()
        : getObservedValueTableNames();

    for(String name : names) {
      getValueTableMap().put(name, name);
    }

    return names;
  }

  @NotNull
  private Set<String> getRegisteredValueTableNames() {
    Set<String> names = new LinkedHashSet<>();

    names.addAll(
        getJdbcTemplate().query(String.format("SELECT " + NAME_COLUMN + ", " + SQL_NAME_COLUMN + " FROM %s WHERE " +
            DATASOURCE_COLUMN + " = ?", VALUE_TABLES_TABLE), new Object[] { getName() }, new RowMapper<String>() {
          @Override
          public String mapRow(ResultSet rs, int rowNum) throws SQLException {
            return rs.getString(NAME_COLUMN);
          }
        }));

    return names;
  }

  @NotNull
  private Set<String> getObservedValueTableNames() {
    Set<String> names = new LinkedHashSet<>();
    for(Table table : getDatabaseSnapshot().get(Table.class)) {
      String tableName = table.getName();

      if(!RESERVED_NAMES.contains(tableName.toLowerCase())) {
        if(settings.getMappedTables().contains(tableName)) {
          JdbcValueTableSettings tableSettings = settings.getTableSettingsForSqlTable(tableName);

          if(tableSettings != null) {
            names.add(tableSettings.getMagmaTableName());
          } else {
            if(!JdbcValueTable.getEntityIdentifierColumns(table).isEmpty()) {
              names.add(tableName);
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
    String sqlTableName = getValueTableMap().containsKey(tableName) ? getValueTableMap().get(tableName) : tableName;

    return tableSettings != null
        ? new JdbcValueTable(this, tableSettings)
        : new JdbcValueTable(this, tableName, getDatabaseSnapshot().get(newTable(sqlTableName)),
            settings.getDefaultEntityType());
  }

  //
  // Methods
  //

  private String generateTableName(String tableName) {
    return String.format("%s_%s", TableUtils.normalize(getName()), TableUtils.normalize(tableName));
  }

  private Map<String, String> getValueTableMap() {
    if(valueTableMap == null) {
      valueTableMap = new HashMap<>();

      if(getSettings().isUseMetadataTables()) {
        List<Map.Entry<String, String>> entries = getJdbcTemplate().query(String
                .format("SELECT " + NAME_COLUMN + ", " + SQL_NAME_COLUMN + " FROM %s WHERE " + DATASOURCE_COLUMN + " = '%s'",
                    VALUE_TABLES_TABLE, getName()), new RowMapper<Map.Entry<String, String>>() {
              @Override
              public Map.Entry<String, String> mapRow(ResultSet rs, int rowNum) throws SQLException {
                return Maps.immutableEntry(rs.getString(NAME_COLUMN), rs.getString(SQL_NAME_COLUMN));
              }
            });

        ImmutableMap.Builder<String, String> builder = ImmutableMap.builder();

        for(Map.Entry<String, String> entry : entries) {
          builder.put(entry);
        }

        valueTableMap.putAll(builder.build());
      }
    }

    return valueTableMap;
  }

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
        public DatabaseSnapshot doInDatabase(Database database) throws LiquibaseException {
          return SnapshotGeneratorFactory.getInstance()
              .createSnapshot(database.getDefaultSchema(), database, new SnapshotControl(database));
        }
      });
    }

    return snapshot;
  }

  void databaseChanged() {
    snapshot = null;
  }

  <T> T doWithDatabase(final DatabaseCallback<T> databaseCallback) {
    return jdbcTemplate.execute(new ConnectionCallback<T>() {
      @Nullable
      @Override
      public T doInConnection(Connection con) throws SQLException, DataAccessException {
        Database database = null;

        try {
          database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(con));

          return databaseCallback.doInDatabase(database);
        } catch(LiquibaseException e) {
          throw new SQLException(e);
        } finally {
          if(database != null) try {
            database.commit();
          } catch(DatabaseException e) {
            //ignore
          }
        }
      }
    });
  }

  private void createMetadataTablesIfNotPresent() {
    List<Change> changes = new ArrayList<>();
    createDatasourceMetadataTablesIfNotPresent(changes);
    createVariableMetadataTablesIfNotPresent(changes);
    createCategoryMetadataTablesIfNotPresent(changes);
    doWithDatabase(new ChangeDatabaseCallback(changes));
  }

  private void createDatasourceMetadataTablesIfNotPresent(List<Change> changes) {
    if(getDatabaseSnapshot().get(newTable(VALUE_TABLES_TABLE)) == null) {
      CreateTableChangeBuilder builder = new CreateTableChangeBuilder();
      builder.tableName(VALUE_TABLES_TABLE) //
          .withColumn(DATASOURCE_COLUMN, "VARCHAR(255)").primaryKey() //
          .withColumn(NAME_COLUMN, "VARCHAR(255)").primaryKey() //
          .withColumn(ENTITY_TYPE_COLUMN, "VARCHAR(255)").notNull() //
          .withColumn(SQL_NAME_COLUMN, "VARCHAR(255)").notNull();
      changes.add(builder.build());
    }
  }

  private void createVariableMetadataTablesIfNotPresent(List<Change> changes) {
    if(getDatabaseSnapshot().get(newTable(VARIABLES_TABLE)) == null) {
      CreateTableChangeBuilder builder = new CreateTableChangeBuilder();
      builder.tableName(VARIABLES_TABLE) //
          .withColumn(DATASOURCE_COLUMN, "VARCHAR(255)").primaryKey() //
          .withColumn(VALUE_TABLE_COLUMN, "VARCHAR(255)").primaryKey() //
          .withColumn(NAME_COLUMN, "VARCHAR(255)").primaryKey() //
          .withColumn(VALUE_TYPE_COLUMN, "VARCHAR(255)").notNull() //
          .withColumn("mime_type", "VARCHAR(255)") //
          .withColumn("units", "VARCHAR(255)") //
          .withColumn("is_repeatable", "BOOLEAN") //
          .withColumn("occurrence_group", "VARCHAR(255)") //
          .withColumn(SQL_NAME_COLUMN, "VARCHAR(255)").notNull();
      changes.add(builder.build());
    }

    if(getDatabaseSnapshot().get(newTable(VARIABLE_ATTRIBUTES_TABLE)) == null) {
      CreateTableChangeBuilder builder = new CreateTableChangeBuilder();
      builder.tableName(VARIABLE_ATTRIBUTES_TABLE) //
          .withColumn(DATASOURCE_COLUMN, "VARCHAR(255)") //
          .withColumn(VALUE_TABLE_COLUMN, "VARCHAR(255)") //
          .withColumn(VARIABLE_COLUMN, "VARCHAR(255)") //
          .withColumn(NAMESPACE_COLUMN, "VARCHAR(20)") //
          .withColumn(NAME_COLUMN, "VARCHAR(255)") //
          .withColumn(LOCALE_COLUMN, "VARCHAR(20)") //
          .withColumn(VALUE_COLUMN, SqlTypes.sqlTypeFor(TextType.get(), SqlTypes.TEXT_TYPE_HINT_MEDIUM));
      changes.add(builder.build());
    }
  }

  private void createCategoryMetadataTablesIfNotPresent(List<Change> changes) {
    if(getDatabaseSnapshot().get(newTable(CATEGORIES_TABLE)) == null) {
      CreateTableChangeBuilder builder = new CreateTableChangeBuilder();
      builder.tableName(CATEGORIES_TABLE) //
          .withColumn(DATASOURCE_COLUMN, "VARCHAR(255)").primaryKey() //
          .withColumn(VALUE_TABLE_COLUMN, "VARCHAR(255)").primaryKey() //
          .withColumn(VARIABLE_COLUMN, "VARCHAR(255)").primaryKey() //
          .withColumn(NAME_COLUMN, "VARCHAR(255)").primaryKey() //
          .withColumn(MISSING_COLUMN, "BOOLEAN").notNull();
      changes.add(builder.build());
    }

    if(getDatabaseSnapshot().get(newTable(CATEGORY_ATTRIBUTES_TABLE)) == null) {
      CreateTableChangeBuilder builder = new CreateTableChangeBuilder();
      builder.tableName(CATEGORY_ATTRIBUTES_TABLE) //
          .withColumn(DATASOURCE_COLUMN, "VARCHAR(255)") //
          .withColumn(VALUE_TABLE_COLUMN, "VARCHAR(255)") //
          .withColumn(VARIABLE_COLUMN, "VARCHAR(255)") //
          .withColumn(CATEGORY_COLUMN, "VARCHAR(255)") //
          .withColumn(NAMESPACE_COLUMN, "VARCHAR(20)") //
          .withColumn(NAME_COLUMN, "VARCHAR(255)") //
          .withColumn(LOCALE_COLUMN, "VARCHAR(20)") //
          .withColumn(VALUE_COLUMN, SqlTypes.sqlTypeFor(TextType.get(), SqlTypes.TEXT_TYPE_HINT_MEDIUM));
      changes.add(builder.build());
    }
  }

  /**
   * Callback used for accessing the {@code Database} instance in a safe and consistent way.
   *
   * @param <T> the type of object returned by the callback if any
   */
  interface DatabaseCallback<T> {
    @Nullable
    T doInDatabase(Database database) throws LiquibaseException;
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
      this(changes, Lists.newArrayList(new MySqlEngineVisitor()));
    }

    ChangeDatabaseCallback(Iterable<Change> changes, Iterable<? extends SqlVisitor> visitors) {
      if(changes == null) throw new IllegalArgumentException("changes cannot be null");
      if(visitors == null) throw new IllegalArgumentException("visitors cannot be null");
      this.changes = changes;
      sqlVisitors = ImmutableList.copyOf(visitors);
    }

    @Nullable
    @Override
    public Object doInDatabase(Database database) throws LiquibaseException {
      for(Change change : changes) {
        if(log.isDebugEnabled()) {
          for(SqlStatement st : change.generateStatements(database)) {
            log.debug("Issuing statement: {}", st);
          }
        }

        database.execute(change.generateStatements(database), getFilteredVisitors(database));
      }

      return null;
    }

    private List<SqlVisitor> getFilteredVisitors(final Database database) {
      return Lists.newArrayList(Iterables.filter(sqlVisitors, new Predicate<SqlVisitor>() {
        @Override
        public boolean apply(@Nullable SqlVisitor input) {
          return DatabaseList.definitionMatches(input.getApplicableDbms(), database.getShortName(), true);
        }
      }));
    }
  }
}
