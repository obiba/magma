/*
 * Copyright (c) 2016 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.magma.datasource.jdbc;

import com.google.common.base.Predicate;
import com.google.common.base.Strings;
import com.google.common.collect.*;
import liquibase.change.Change;
import liquibase.change.core.RenameTableChange;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.DatabaseList;
import liquibase.database.ObjectQuotingStrategy;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.LiquibaseException;
import liquibase.snapshot.DatabaseSnapshot;
import liquibase.snapshot.SnapshotControl;
import liquibase.snapshot.SnapshotGeneratorFactory;
import liquibase.sql.visitor.SqlVisitor;
import liquibase.statement.SqlStatement;
import liquibase.structure.core.Column;
import liquibase.structure.core.Table;
import liquibase.structure.core.View;
import org.obiba.magma.MagmaRuntimeException;
import org.obiba.magma.ValueTable;
import org.obiba.magma.ValueTableWriter;
import org.obiba.magma.datasource.jdbc.support.*;
import org.obiba.magma.support.AbstractDatasource;
import org.obiba.magma.support.Initialisables;
import org.obiba.magma.type.TextType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import javax.annotation.Nullable;
import javax.sql.DataSource;
import javax.validation.constraints.NotNull;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

import static org.obiba.magma.datasource.jdbc.JdbcValueTableWriter.*;
import static org.obiba.magma.datasource.jdbc.support.TableUtils.newTable;
import static org.obiba.magma.datasource.jdbc.support.TableUtils.newView;

public class JdbcDatasource extends AbstractDatasource {

  private static final Logger log = LoggerFactory.getLogger(JdbcDatasource.class);

  private static final Set<String> RESERVED_NAMES = ImmutableSet
      .of(VALUE_TABLES_TABLE, VARIABLES_TABLE, VARIABLE_ATTRIBUTES_TABLE, CATEGORIES_TABLE, CATEGORY_ATTRIBUTES_TABLE);

  private static final String TYPE = "jdbc";

  public static final String EPOCH = "1970-01-02 00:00:00.000";

  private final JdbcTemplate jdbcTemplate;

  private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

  private final JdbcDatasourceSettings settings;

  private DatabaseSnapshot snapshot;

  private Map<String, String> valueTableMap;

  private PlatformTransactionManager txManager;

  private Database databaseTmpl;

  private Map<String, String> escapedColumnNames = Maps.newConcurrentMap();

  private Map<String, String> escapedTableNames = Maps.newConcurrentMap();

  private final String ESC_ENTITY_TYPE_COLUMN, ESC_VALUE_TABLES_TABLE, ESC_DATASOURCE_COLUMN, ESC_NAME_COLUMN,
      ESC_VALUE_TABLE_COLUMN, ESC_SQL_NAME_COLUMN;

  @SuppressWarnings("ConstantConditions")
  public JdbcDatasource(String name, @NotNull DataSource datasource, @NotNull JdbcDatasourceSettings settings,
      PlatformTransactionManager txManager) {
    super(name, TYPE);
    if(settings == null) throw new IllegalArgumentException("null settings");
    if(datasource == null) throw new IllegalArgumentException("null datasource");
    this.settings = settings;
    jdbcTemplate = new JdbcTemplate(datasource);
    namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(datasource);
    this.txManager = txManager;
    ESC_ENTITY_TYPE_COLUMN = escapeColumnName(ENTITY_TYPE_COLUMN);
    ESC_VALUE_TABLES_TABLE = escapeTableName(VALUE_TABLES_TABLE);
    ESC_DATASOURCE_COLUMN = escapeColumnName(DATASOURCE_COLUMN);
    ESC_NAME_COLUMN = escapeColumnName(NAME_COLUMN);
    ESC_SQL_NAME_COLUMN = escapeColumnName(SQL_NAME_COLUMN);
    ESC_VALUE_TABLE_COLUMN = escapeColumnName(VALUE_TABLE_COLUMN);
  }

  public JdbcDatasource(String name, @NotNull DataSource datasource, @NotNull JdbcDatasourceSettings settings) {
    this(name, datasource, settings, new DataSourceTransactionManager(datasource));
  }

  public JdbcDatasource(String name, DataSource datasource, String defaultEntityType, boolean useMetadataTables,
      PlatformTransactionManager txManager) {
    this(name, datasource,
        JdbcDatasourceSettings.newSettings(defaultEntityType).useMetadataTables(useMetadataTables).build(), txManager);
  }

  public JdbcDatasource(String name, DataSource datasource, String defaultEntityType, boolean useMetadataTables) {
    this(name, datasource, defaultEntityType, useMetadataTables, new DataSourceTransactionManager(datasource));
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
    if(hasValueTable(newName)) throw new MagmaRuntimeException("A table already exists with the name: " + newName);

    JdbcValueTable table = (JdbcValueTable) getValueTable(tableName);
    if (table.isSQLView()) throw new MagmaRuntimeException("A SQL view cannot be renamed");
    removeValueTable(table);
    String newSqlName = getSettings().isUseMetadataTables() ? generateSqlTableName(newName) : newName;
    getValueTableMap().remove(tableName);
    getValueTableMap().put(newName, newSqlName);

    doWithDatabase(
        new ChangeDatabaseCallback(getTableRenameChanges(tableName, table.getSqlName(), newName, newSqlName)));
    databaseChanged();

    ValueTable vt = initialiseValueTable(newName);
    Initialisables.initialise(vt);
    addValueTable(vt);
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
        tableSettings = JdbcValueTableSettings.newSettings(generateSqlTableName(tableName))
            .tableName(tableName) //
            .entityType(entityType) //
            .entityIdentifierColumn(settings.getDefaultEntityIdColumnName()).build();
        settings.getTableSettings().add(tableSettings);
      }

      table = new JdbcValueTable(this, tableSettings);
      Initialisables.initialise(table);
      addValueTable(table);
      addTableMetaData(tableName, tableSettings);
    }

    return new JdbcValueTableWriter(table);
  }

  private void addTableMetaData(@NotNull String tableName, @NotNull JdbcValueTableSettings tableSettings) {
    if(!getSettings().isUseMetadataTables()) return;

    InsertDataChangeBuilder idc = InsertDataChangeBuilder.newBuilder() //
        .tableName(VALUE_TABLES_TABLE);

    if(getSettings().isMultipleDatasources()) idc.withColumn(DATASOURCE_COLUMN, getName());

    idc.withColumn(NAME_COLUMN, tableName) //
        .withColumn(ENTITY_TYPE_COLUMN, tableSettings.getEntityType()) //
        .withColumn(CREATED_COLUMN, new Date()) //
        .withColumn(UPDATED_COLUMN, new Date()) //
        .withColumn(SQL_NAME_COLUMN, tableSettings.getSqlTableName());

    doWithDatabase(new ChangeDatabaseCallback(idc.build()));
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

  @Override
  protected ValueTable initialiseValueTable(String tableName) {
    JdbcValueTableSettings tableSettings = settings.getTableSettingsForMagmaTable(tableName);
    String sqlTableName = getValueTableMap().containsKey(tableName) ? getValueTableMap().get(tableName) : tableName;
    String entityType = null;

    if(getSettings().isUseMetadataTables()) {
      String sql = getSettings().isMultipleDatasources()
          ? String.format("SELECT %s FROM %s WHERE %s = ? AND %s = ?", ESC_ENTITY_TYPE_COLUMN, ESC_VALUE_TABLES_TABLE,
          ESC_DATASOURCE_COLUMN, ESC_NAME_COLUMN)
          : String.format("SELECT %s FROM %s WHERE %s = ?", ESC_ENTITY_TYPE_COLUMN, ESC_VALUE_TABLES_TABLE,
              ESC_NAME_COLUMN);
      Object[] params = getSettings().isMultipleDatasources()
          ? new Object[] { getName(), tableName }
          : new Object[] { tableName };
      entityType = getJdbcTemplate().queryForObject(sql, params, String.class);
    }

    entityType = Strings.isNullOrEmpty(entityType) ? settings.getDefaultEntityType() : entityType;

    if(tableSettings != null) return new JdbcValueTable(this, tableSettings);

    Table table = getDatabaseSnapshot().get(newTable(sqlTableName));
    if (table != null) {
      return new JdbcValueTable(this, tableName, table, entityType);
    }

    View view = getDatabaseSnapshot().get(newView(sqlTableName));
    if (view != null) {
      return new JdbcValueTable(this, tableName, view, entityType, settings.getDefaultEntityIdColumnName());
    }

    return new JdbcValueTable(this,
          JdbcValueTableSettings.newSettings(generateSqlTableName(tableName)) //
              .tableName(tableName) //
              .entityType(entityType) //
              .entityIdentifierColumn(settings.getDefaultEntityIdColumnName()).build());
  }

  //
  // Methods
  //

  @NotNull
  private Set<String> getRegisteredValueTableNames() {
    Set<String> names = new LinkedHashSet<>();
    String select = getSettings().isMultipleDatasources() ? String
        .format("SELECT %s FROM %s WHERE %s = '%s'", ESC_NAME_COLUMN, ESC_VALUE_TABLES_TABLE, ESC_DATASOURCE_COLUMN,
            getName()) : String.format("SELECT %s FROM %s", ESC_NAME_COLUMN, ESC_VALUE_TABLES_TABLE);
    names.addAll(getJdbcTemplate().query(select, (rs, rowNum) -> rs.getString(NAME_COLUMN)));
    return names;
  }

  @NotNull
  private Set<String> getObservedValueTableNames() {
    Set<String> names = new LinkedHashSet<>();

    getDatabaseSnapshot().get(Table.class).stream() //
        .filter(table -> isTableIncluded(table.getName())) //
        .forEach(table -> {
          String tableName = table.getName();
          List<JdbcValueTableSettings> tableSettings = settings.getTableSettingsForSqlTable(tableName);
          if (tableSettings != null && !tableSettings.isEmpty()) {
            tableSettings.forEach(settings -> names.add(settings.getMagmaTableName()));
          } else if (!Strings.isNullOrEmpty(JdbcValueTable.getEntityIdentifierColumn(table))) {
            names.add(tableName);
          }
        });

    getDatabaseSnapshot().get(View.class).stream() //
        .filter(view -> isTableIncluded(view.getName())) //
        .forEach(view -> {
          String viewName = view.getName();
          List<JdbcValueTableSettings> tableSettings = settings.getTableSettingsForSqlTable(viewName);
          if (tableSettings != null && !tableSettings.isEmpty()) {
            tableSettings.forEach(settings -> names.add(settings.getMagmaTableName()));
          } else {
            names.add(viewName);
          }
        });

    return names;
  }

  /**
   * SQL Table (or View) is included if it has not a reserved name and it was filtered in by settings.
   *
   * @param sqlTableName
   * @return
   */
  private boolean isTableIncluded(String sqlTableName) {
    return !RESERVED_NAMES.contains(sqlTableName.toLowerCase())
        && (settings.hasTableSettingsForSqlTable(sqlTableName) || !settings.hasMappedTables() || settings.hasMappedTable(sqlTableName));
  }

  /**
   * Changes when a table is renamed.
   *
   * @param tableName
   * @param sqlName
   * @param newName
   * @param newSqlName
   */
  private List<Change> getTableRenameChanges(String tableName, String sqlName, String newName, String newSqlName) {
    List<Change> changes = Lists.newArrayList();
    if(!getSettings().isUseMetadataTables()) return changes;

    String whereClause = getSettings().isMultipleDatasources()
        ? String.format("%s = '%s' AND %s = '%s'", ESC_DATASOURCE_COLUMN, getName(), ESC_NAME_COLUMN, tableName)
        : String.format("%s = '%s'", ESC_NAME_COLUMN, tableName);
    changes.add(UpdateDataChangeBuilder.newBuilder().tableName(VALUE_TABLES_TABLE) //
        .withColumn(NAME_COLUMN, newName) //
        .withColumn(SQL_NAME_COLUMN, newSqlName) //
        .withColumn(UPDATED_COLUMN, new Date()) //
        .where(whereClause).build());

    whereClause = getSettings().isMultipleDatasources()
        ? String.format("%s = '%s' AND %s = '%s'", ESC_DATASOURCE_COLUMN, getName(), ESC_VALUE_TABLE_COLUMN, tableName)
        : String.format("%s = '%s'", ESC_VALUE_TABLE_COLUMN, tableName);

    changes.add(UpdateDataChangeBuilder.newBuilder().tableName(VARIABLES_TABLE) //
        .withColumn(VALUE_TABLE_COLUMN, newName) //
        .where(whereClause).build());

    changes.add(UpdateDataChangeBuilder.newBuilder().tableName(VARIABLE_ATTRIBUTES_TABLE) //
        .withColumn(VALUE_TABLE_COLUMN, newName) //
        .where(whereClause).build());

    changes.add(UpdateDataChangeBuilder.newBuilder().tableName(CATEGORIES_TABLE) //
        .withColumn(VALUE_TABLE_COLUMN, newName) //
        .where(whereClause).build());

    changes.add(UpdateDataChangeBuilder.newBuilder().tableName(CATEGORY_ATTRIBUTES_TABLE) //
        .withColumn(VALUE_TABLE_COLUMN, newName) //
        .where(whereClause).build());

    RenameTableChange rtc = new RenameTableChange();
    rtc.setOldTableName(sqlName);
    rtc.setNewTableName(newSqlName);
    changes.add(rtc);

    return changes;
  }

  private String generateSqlTableName(String tableName) {
    return getSettings().isMultipleDatasources()
        ? String.format("%s_%s", TableUtils.normalize(getName()), TableUtils.normalize(tableName))
        : TableUtils.normalize(tableName);
  }

  private Map<String, String> getValueTableMap() {
    if(valueTableMap == null) {
      valueTableMap = new HashMap<>();

      if(getSettings().isUseMetadataTables()) {
        String select = getSettings().isMultipleDatasources()
            ? String.format("SELECT %s, %s FROM %s WHERE %s = '%s'", ESC_NAME_COLUMN, ESC_SQL_NAME_COLUMN,
            ESC_VALUE_TABLES_TABLE, ESC_DATASOURCE_COLUMN, getName())
            : String.format("SELECT %s, %s FROM %s", ESC_NAME_COLUMN, ESC_SQL_NAME_COLUMN, ESC_VALUE_TABLES_TABLE);

        List<Map.Entry<String, String>> entries = getJdbcTemplate() //
            .query(select,
                (rs, rowNum) -> Maps.immutableEntry(rs.getString(NAME_COLUMN), rs.getString(SQL_NAME_COLUMN)));

        ImmutableMap.Builder<String, String> builder = ImmutableMap.builder();
        entries.forEach(builder::put);
        valueTableMap.putAll(builder.build());
      }
    }

    return valueTableMap;
  }

  JdbcDatasourceSettings getSettings() {
    return settings;
  }

  JdbcTemplate getJdbcTemplate() {
    return jdbcTemplate;
  }

  NamedParameterJdbcTemplate getNamedParameterJdbcTemplate() {
    return namedParameterJdbcTemplate;
  }

  TransactionTemplate getTransactionTemplate() {
    TransactionTemplate txTemplate = new TransactionTemplate(txManager);
    txTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);

    return txTemplate;
  }

  String escapeTableName(final String identifier) {
    if(!escapedTableNames.containsKey(identifier)) {
      String escaped = doWithDatabase(new DatabaseCallback<String>() {
        @Nullable
        @Override
        public String doInDatabase(Database database) throws LiquibaseException {
          return database.escapeObjectName(identifier, Table.class);
        }
      });
      escapedTableNames.put(identifier, escaped);
    }
    return escapedTableNames.get(identifier);
  }

  String escapeColumnName(final String identifier) {
    if (!escapedColumnNames.containsKey(identifier)) {
      String escaped = doWithDatabase(new DatabaseCallback<String>() {
        @Nullable
        @Override
        public String doInDatabase(Database database) throws LiquibaseException {
          return database.escapeObjectName(identifier, Column.class);
        }
      });
      escapedColumnNames.put(identifier, escaped);
    }
    return escapedColumnNames.get(identifier);
  }

  DatabaseSnapshot getDatabaseSnapshot() {
    if(snapshot == null) {
      snapshot = doWithDatabase(database -> SnapshotGeneratorFactory.getInstance()
          .createSnapshot(database.getDefaultSchema(), database, new SnapshotControl(database)));
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
        try {
          JdbcConnection jdbcCon = new JdbcConnection(con);
          Database database = newDatabaseInstance(jdbcCon);
          database.setConnection(jdbcCon);
          database.setObjectQuotingStrategy(ObjectQuotingStrategy.QUOTE_ALL_OBJECTS);
          return databaseCallback.doInDatabase(database);
        } catch(LiquibaseException e) {
          throw new SQLException(e);
        }
      }
    });
  }

  private synchronized Database newDatabaseInstance(JdbcConnection jdbcCon) throws SQLException {
    try {
      if(databaseTmpl == null) {
        databaseTmpl = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(jdbcCon);
      }
      return databaseTmpl.getClass().newInstance();
    } catch(Exception e) {
      throw new SQLException(e);
    }
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
      CreateTableChangeBuilder builder = new CreateTableChangeBuilder()//
          .tableName(VALUE_TABLES_TABLE);

      if(getSettings().isMultipleDatasources()) builder.withColumn(DATASOURCE_COLUMN, "VARCHAR(255)").primaryKey();

      builder.withColumn(NAME_COLUMN, "VARCHAR(255)").primaryKey() //
          .withColumn(ENTITY_TYPE_COLUMN, "VARCHAR(255)").notNull() //
          .withColumn(CREATED_COLUMN, "TIMESTAMP", EPOCH).notNull() //
          .withColumn(UPDATED_COLUMN, "TIMESTAMP", EPOCH).notNull() //
          .withColumn(SQL_NAME_COLUMN, "VARCHAR(255)").notNull();
      changes.add(builder.build());
    }
  }

  private void createVariableMetadataTablesIfNotPresent(List<Change> changes) {
    if(getDatabaseSnapshot().get(newTable(VARIABLES_TABLE)) == null) {
      CreateTableChangeBuilder builder = new CreateTableChangeBuilder().tableName(VARIABLES_TABLE);

      if(getSettings().isMultipleDatasources()) builder.withColumn(DATASOURCE_COLUMN, "VARCHAR(255)").primaryKey();

      builder.withColumn(VALUE_TABLE_COLUMN, "VARCHAR(255)").primaryKey() //
          .withColumn(NAME_COLUMN, "VARCHAR(255)").primaryKey() //
          .withColumn(VALUE_TYPE_COLUMN, "VARCHAR(255)").notNull() //
          .withColumn("mime_type", "VARCHAR(255)") //
          .withColumn("units", "VARCHAR(255)") //
          .withColumn("is_repeatable", "BOOLEAN") //
          .withColumn("occurrence_group", "VARCHAR(255)") //
          .withColumn("index", "INT") //
          .withColumn(SQL_NAME_COLUMN, "VARCHAR(255)").notNull();
      changes.add(builder.build());
    }

    if(getDatabaseSnapshot().get(newTable(VARIABLE_ATTRIBUTES_TABLE)) == null) {
      CreateTableChangeBuilder builder = new CreateTableChangeBuilder() //
          .tableName(VARIABLE_ATTRIBUTES_TABLE);

      if(getSettings().isMultipleDatasources()) builder.withColumn(DATASOURCE_COLUMN, "VARCHAR(255)");

      builder.withColumn(VALUE_TABLE_COLUMN, "VARCHAR(255)") //
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
      CreateTableChangeBuilder builder = new CreateTableChangeBuilder() //
          .tableName(CATEGORIES_TABLE);

      if(getSettings().isMultipleDatasources()) builder.withColumn(DATASOURCE_COLUMN, "VARCHAR(255)").primaryKey();

      builder.withColumn(VALUE_TABLE_COLUMN, "VARCHAR(255)").primaryKey() //
          .withColumn(VARIABLE_COLUMN, "VARCHAR(255)").primaryKey() //
          .withColumn(NAME_COLUMN, "VARCHAR(255)").primaryKey() //
          .withColumn(MISSING_COLUMN, "BOOLEAN").notNull();
      changes.add(builder.build());
    }

    if(getDatabaseSnapshot().get(newTable(CATEGORY_ATTRIBUTES_TABLE)) == null) {
      CreateTableChangeBuilder builder = new CreateTableChangeBuilder() //
          .tableName(CATEGORY_ATTRIBUTES_TABLE);

      if(getSettings().isMultipleDatasources()) builder.withColumn(DATASOURCE_COLUMN, "VARCHAR(255)");

      builder.withColumn(VALUE_TABLE_COLUMN, "VARCHAR(255)") //
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
    public Object doInDatabase(final Database database) throws LiquibaseException {
      for(Change change : changes) {
        if(log.isDebugEnabled()) {
          for(SqlStatement st : change.generateStatements(database)) {
            log.debug("Issuing statement: {}", st);
          }
        }

        database.execute(change.generateStatements(database), getFilteredVisitors(database));

        try {
          database.commit(); //explicit commit needed for postgres.
        } catch(Exception e) {
          if(!e.getMessage().contains("Commit can not be set while enrolled in a transaction")) throw e;
        }
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
