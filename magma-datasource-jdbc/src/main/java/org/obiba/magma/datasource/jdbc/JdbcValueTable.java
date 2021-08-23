/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.magma.datasource.jdbc;

import com.google.common.base.Strings;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import liquibase.change.Change;
import liquibase.change.core.DropTableChange;
import liquibase.snapshot.DatabaseSnapshot;
import liquibase.structure.core.*;
import org.obiba.magma.*;
import org.obiba.magma.datasource.jdbc.JdbcDatasource.ChangeDatabaseCallback;
import org.obiba.magma.datasource.jdbc.support.CreateIndexChangeBuilder;
import org.obiba.magma.datasource.jdbc.support.CreateTableChangeBuilder;
import org.obiba.magma.datasource.jdbc.support.TableUtils;
import org.obiba.magma.support.AbstractValueTable;
import org.obiba.magma.support.Initialisables;
import org.obiba.magma.support.VariableEntityProvider;
import org.obiba.magma.type.DateTimeType;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.obiba.magma.datasource.jdbc.JdbcValueTableWriter.*;
import static org.obiba.magma.datasource.jdbc.support.TableUtils.newTable;
import static org.obiba.magma.datasource.jdbc.support.TableUtils.newView;

class JdbcValueTable extends AbstractValueTable {

  private final JdbcValueTableSettings settings;

  private Relation tableOrView;

  private BiMap<String, String> variableMap;

  private JdbcValueTableTimestamps jdbcValueTableTimestamps;

  private final String ESC_CATEGORY_ATTRIBUTES_TABLE, ESC_DATASOURCE_COLUMN,
      ESC_VALUE_TABLE_COLUMN, ESC_CATEGORIES_TABLE, ESC_VARIABLES_TABLE,
      ESC_VARIABLE_ATTRIBUTES_TABLE, ESC_VALUE_TABLES_TABLE, ESC_NAME_COLUMN,
      ESC_VARIABLE_COLUMN, ESC_CATEGORY_COLUMN, ESC_MISSING_COLUMN, ESC_SQL_NAME_COLUMN;

  JdbcValueTable(Datasource datasource, JdbcValueTableSettings settings) {
    super(datasource, settings.getMagmaTableName());
    this.settings = settings;

    // first, check if it is an existing View
    View view = getDatasource().getDatabaseSnapshot().get(newView(getSqlName()));
    if (view == null) {
      // if not a view, make sure the SQL table exists
      if (getDatasource().getDatabaseSnapshot().get(newTable(getSqlName())) == null) {
        createSqlTable(getSqlName());
        getDatasource().databaseChanged();
      }
      tableOrView = getDatasource().getDatabaseSnapshot().get(newTable(getSqlName()));
    } else {
      tableOrView = view;
    }

    setVariableEntityProvider(new JdbcVariableEntityProvider(this));

    ESC_CATEGORY_ATTRIBUTES_TABLE = getDatasource().escapeTableName(CATEGORY_ATTRIBUTES_TABLE);
    ESC_CATEGORIES_TABLE = getDatasource().escapeTableName(CATEGORIES_TABLE);
    ESC_VARIABLES_TABLE = getDatasource().escapeTableName(VARIABLES_TABLE);
    ESC_VARIABLE_ATTRIBUTES_TABLE = getDatasource().escapeTableName(VARIABLE_ATTRIBUTES_TABLE);
    ESC_VALUE_TABLES_TABLE = getDatasource().escapeTableName(VALUE_TABLES_TABLE);
    ESC_DATASOURCE_COLUMN = getDatasource().escapeColumnName(DATASOURCE_COLUMN);
    ESC_VALUE_TABLE_COLUMN = getDatasource().escapeColumnName(VALUE_TABLE_COLUMN);
    ESC_NAME_COLUMN = getDatasource().escapeColumnName(NAME_COLUMN);
    ESC_VARIABLE_COLUMN = getDatasource().escapeColumnName(VARIABLE_COLUMN);
    ESC_CATEGORY_COLUMN = getDatasource().escapeColumnName(CATEGORY_COLUMN);
    ESC_MISSING_COLUMN = getDatasource().escapeColumnName(MISSING_COLUMN);
    ESC_SQL_NAME_COLUMN = getDatasource().escapeColumnName(SQL_NAME_COLUMN);
  }

  JdbcValueTable(Datasource datasource, String tableName, Table table, String entityType) {
    this(datasource,
        JdbcValueTableSettings.newSettings(table.getName()).tableName(tableName).entityType(entityType) //
            .entityIdentifierColumn(getEntityIdentifierColumn(table)).build());
  }

  JdbcValueTable(Datasource datasource, String tableName, View view, String entityType, String entityIdentifierColumn) {
    this(datasource,
        JdbcValueTableSettings.newSettings(view.getName()).tableName(tableName).entityType(entityType) //
            .entityIdentifierColumn(entityIdentifierColumn).build());
  }

  //
  // AbstractValueTable Methods
  //

  @Override
  public void initialise() {
    super.initialise();
    Initialisables.initialise(getVariableEntityProvider());
    refreshVariablesMap();
    initialiseVariableValueSources();
  }

  @Override
  public String getEntityType() {
    return settings.getEntityType() == null
        ? getDatasource().getSettings().getDefaultEntityType()
        : settings.getEntityType();
  }

  @NotNull
  @Override
  public JdbcDatasource getDatasource() {
    return (JdbcDatasource) super.getDatasource();
  }

  @NotNull
  JdbcVariableEntityProvider getJdbcVariableEntityProvider() {
    return (JdbcVariableEntityProvider)getVariableEntityProvider();
  }

  @Override
  public int getVariableEntityCount() {
    return getJdbcVariableEntityProvider().getVariableEntityCount();
  }

  @Override
  public List<VariableEntity> getVariableEntities(int offset, int limit) {
    return getJdbcVariableEntityProvider().getVariableEntities(offset, limit);
  }

  @Override
  public boolean hasValueSet(VariableEntity entity) {
    return getJdbcVariableEntityProvider().hasVariableEntity(entity);
  }

  @Override
  public ValueSet getValueSet(VariableEntity entity) throws NoSuchValueSetException {
    return new JdbcValueSet(this, entity);
  }

  @Override
  protected ValueSetBatch getValueSetsBatch(List<VariableEntity> entities) {
    return new JdbcValueSetBatch(this, entities);
  }

  @Override
  public Timestamps getValueSetTimestamps(VariableEntity entity) throws NoSuchValueSetException {
    return new ValueSetTimestamps(entity, getCreatedTimestampColumnName(), getUpdatedTimestampColumnName());
  }

  @Override
  public void dropValueSets() {
    String sql = String.format("TRUNCATE TABLE %s", getDatasource().escapeTableName(getSqlName()));
    getDatasource().getTransactionTemplate().execute(new TransactionCallbackWithoutResult() {
      @Override
      protected void doInTransactionWithoutResult(TransactionStatus status) {
        getDatasource().getJdbcTemplate().update(sql);
        getJdbcVariableEntityProvider().removeAll();
      }
    });
  }

  @NotNull
  @Override
  public Timestamps getTimestamps() {
    if (jdbcValueTableTimestamps != null) return jdbcValueTableTimestamps;

    return jdbcValueTableTimestamps = new JdbcValueTableTimestamps(this);
  }

  //
  // Methods
  //

  void clearTimestamps() {
    jdbcValueTableTimestamps = null;
  }

  boolean isSQLView() {
    return tableOrView instanceof View;
  }

  public void drop() {
    if (getDatasource().getDatabaseSnapshot().get(newTable(getSqlName())) != null) {
      DropTableChange dtt = new DropTableChange();
      dtt.setTableName(getSqlName());
      getDatasource().doWithDatabase(new ChangeDatabaseCallback(dtt));
      getDatasource().databaseChanged();
    }

    if (getDatasource().getSettings().isUseMetadataTables()) {
      getDatasource().getTransactionTemplate().execute(new TransactionCallbackWithoutResult() {
        @Override
        protected void doInTransactionWithoutResult(TransactionStatus status) {
          dropCategoriesMetaData();
          dropVariablesMetaData();
          dropTableMetaData();
        }
      });
    }
  }

  private void dropCategoriesMetaData() {
    Object[] params = getDatasource().getSettings().isMultipleDatasources() ? new Object[]{getDatasource().getName(),
        getName()} : new Object[]{getName()};

    String sql = getDatasource().getSettings().isMultipleDatasources()
        ? String.format("DELETE FROM %s WHERE %s = ? AND %s = ?", ESC_CATEGORY_ATTRIBUTES_TABLE, ESC_DATASOURCE_COLUMN,
        ESC_VALUE_TABLE_COLUMN)
        : String.format("DELETE FROM %s WHERE %s = ?", ESC_CATEGORY_ATTRIBUTES_TABLE, ESC_VALUE_TABLE_COLUMN);
    getDatasource().getJdbcTemplate().update(sql, params);

    sql = getDatasource().getSettings().isMultipleDatasources()
        ? String
        .format("DELETE FROM %s WHERE %s = ? AND %s = ?", ESC_CATEGORIES_TABLE, ESC_DATASOURCE_COLUMN, ESC_VALUE_TABLE_COLUMN)
        : String.format("DELETE FROM %s WHERE %s = ?", ESC_CATEGORIES_TABLE, ESC_VALUE_TABLE_COLUMN);
    getDatasource().getJdbcTemplate().update(sql, params);
  }

  private void dropVariablesMetaData() {
    Object[] params = getDatasource().getSettings().isMultipleDatasources() ? new Object[]{getDatasource().getName(),
        getName()} : new Object[]{getName()};

    String sql = getDatasource().getSettings().isMultipleDatasources()
        ? String.format("DELETE FROM %s WHERE %s = ? AND %s = ?", ESC_VARIABLE_ATTRIBUTES_TABLE, ESC_DATASOURCE_COLUMN,
        ESC_VALUE_TABLE_COLUMN)
        : String.format("DELETE FROM %s WHERE %s = ?", ESC_VARIABLE_ATTRIBUTES_TABLE, ESC_VALUE_TABLE_COLUMN);
    getDatasource().getJdbcTemplate().update(sql, params);

    sql = getDatasource().getSettings().isMultipleDatasources()
        ? String
        .format("DELETE FROM %s WHERE %s = ? AND %s = ?", ESC_VARIABLES_TABLE, ESC_DATASOURCE_COLUMN, ESC_VALUE_TABLE_COLUMN)
        : String.format("DELETE FROM %s WHERE %s = ?", ESC_VARIABLES_TABLE, ESC_VALUE_TABLE_COLUMN);
    getDatasource().getJdbcTemplate().update(sql, params);
  }

  private void dropTableMetaData() {
    Object[] params = getDatasource().getSettings().isMultipleDatasources() ? new Object[]{getDatasource().getName(),
        getName()} : new Object[]{getName()};
    String sql = getDatasource().getSettings().isMultipleDatasources()
        ? String.format("DELETE FROM %s WHERE %s = ? AND %s = ?", ESC_VALUE_TABLES_TABLE, ESC_DATASOURCE_COLUMN, ESC_NAME_COLUMN)
        : String.format("DELETE FROM %s WHERE %s = ?", ESC_VALUE_TABLES_TABLE, ESC_NAME_COLUMN);
    getDatasource().getJdbcTemplate().update(sql, params);
  }

  public JdbcValueTableSettings getSettings() {
    return settings;
  }

  String getSqlName() {
    return settings.getSqlTableName();
  }

  void tableChanged() {
    refreshTable();
    initialise();
  }

  boolean hasCreatedTimestampColumn() {
    return getSettings().hasCreatedTimestampColumnName() ||
        getDatasource().getSettings().hasCreatedTimestampColumnName();
  }

  String getCreatedTimestampColumnName() {
    return getSettings().hasCreatedTimestampColumnName()
        ? getSettings().getCreatedTimestampColumnName()
        : getDatasource().getSettings().getDefaultCreatedTimestampColumnName();
  }

  boolean hasUpdatedTimestampColumn() {
    return getSettings().hasUpdatedTimestampColumnName() ||
        getDatasource().getSettings().hasUpdatedTimestampColumnName();
  }

  String getUpdatedTimestampColumnName() {
    return getSettings().hasUpdatedTimestampColumnName()
        ? getSettings().getUpdatedTimestampColumnName()
        : getDatasource().getSettings().getDefaultUpdatedTimestampColumnName();
  }

  void writeVariableValueSource(Variable source) {
    addVariableValueSource(new JdbcVariableValueSource(this, source));
  }

  boolean isMultilines() {
    // either detected or configured (at table or datasource level)
    return ((JdbcVariableEntityProvider) getVariableEntityProvider()).isMultilines() || isSettingsMultilines();
  }

  String getEntityIdentifierColumn() {
    return getSettings().getEntityIdentifierColumn();
  }

  DataType getEntityIdentifierColumnType() {
    return tableOrView.getColumn(getEntityIdentifierColumn()).getType();
  }

  private boolean isSettingsMultilines() {
    return getSettings().isMultilines() || getDatasource().getSettings().isMultilines();
  }

  static String getEntityIdentifierColumn(Table table) {
    List<String> entityIdentifierColumns = new ArrayList<>();
    PrimaryKey pk = table.getPrimaryKey();

    entityIdentifierColumns.addAll(table.getColumns().stream() //
        .filter(column -> pk != null && pk.getColumns().contains(column)) //
        .map(Column::getName) //
        .collect(Collectors.toList()));

    return entityIdentifierColumns.isEmpty() ? "" : entityIdentifierColumns.get(0);
  }

  static boolean hasEntityIdentifierColumn(Relation tableOrView, String entityIdColumnName) {
    return tableOrView.getColumn(entityIdColumnName) != null;
  }

  private void initialiseVariableValueSources() {
    clearSources();

    if (getDatasource().getSettings().isUseMetadataTables()) {
      initialiseVariableValueSourcesFromMetaData();
    } else {
      initialiseVariableValueSourcesFromColumns();
    }
  }

  private void initialiseVariableValueSourcesFromMetaData() {
    if (!metadataTablesExist()) {
      throw new MagmaRuntimeException("metadata tables not found");
    }

    String sql = getDatasource().getSettings().isMultipleDatasources()
        ? String
        .format("SELECT * FROM %s WHERE %s = ? AND %s = ?", ESC_VARIABLES_TABLE, ESC_DATASOURCE_COLUMN, ESC_VALUE_TABLE_COLUMN)
        : String.format("SELECT * FROM %s WHERE %s = ?", ESC_VARIABLES_TABLE, ESC_VALUE_TABLE_COLUMN);
    Object[] params = getDatasource().getSettings().isMultipleDatasources() ? new Object[]{
        getDatasource().getName(), getName()} : new Object[]{getName()};

    List<Variable> results = getDatasource().getJdbcTemplate().query(sql, params, new VariableRowMapper());

    for (Variable variable : results) {
      addVariableValueSource(new JdbcVariableValueSource(this, variable));
    }
  }

  private void initialiseVariableValueSourcesFromColumns() {
    List<String> reserved = Lists.newArrayList(getSettings().getEntityIdentifierColumn());
    if (getCreatedTimestampColumnName() != null) reserved.add(getCreatedTimestampColumnName());
    if (getCreatedTimestampColumnName() != null) reserved.add(getUpdatedTimestampColumnName());

    Pattern exclusion = Pattern.compile(getSettings().hasExcludedColumns() ? getSettings().getExcludedColumns() : "^$");
    Pattern inclusion = Pattern.compile(getSettings().hasIncludedColumns() ? getSettings().getIncludedColumns() : ".*");

    int idx = 1;
    for (Column column : tableOrView.getColumns().stream() //
        .filter(column -> isColumnIncluded(column, reserved, exclusion, inclusion)).collect(Collectors.toList())) {
      addVariableValueSource(new JdbcVariableValueSource(this, column, idx++));
    }
  }

  /**
   * Check if the column name is neither reserved, nor excluded and is included.
   *
   * @param column
   * @param reserved
   * @param exclusion
   * @param inclusion
   * @return
   */
  private boolean isColumnIncluded(Column column, List<String> reserved, Pattern exclusion, Pattern inclusion) {
    String name = column.getName();
    return !reserved.contains(name) && !reserved.contains(name.toLowerCase())
        && !exclusion.matcher(name).find() && inclusion.matcher(name).find();
  }

  private class VariableRowMapper implements RowMapper<Variable> {

    @Override
    public Variable mapRow(ResultSet rs, int rowNum) throws SQLException {
      return buildVariableFromResultSet(rs);
    }

    private Variable buildVariableFromResultSet(ResultSet rs) throws SQLException {
      String variableName = rs.getString("name");
      ValueType valueType = ValueType.Factory.forName(rs.getString("value_type"));
      String refEntityType = rs.getString("ref_entity_type");
      String mimeType = rs.getString("mime_type");
      String units = rs.getString("units");
      boolean isRepeatable = rs.getBoolean("is_repeatable");
      String occurrenceGroup = rs.getString("occurrence_group");
      int index = rs.getInt("index");
      Variable.Builder builder = Variable.Builder.newVariable(variableName, valueType, getEntityType())
          .referencedEntityType(refEntityType).mimeType(mimeType).unit(units).index(index);

      if (isRepeatable) {
        builder.repeatable();
        builder.occurrenceGroup(occurrenceGroup);
      }

      addVariableAttributes(variableName, builder);
      addVariableCategories(variableName, builder);

      return builder.build();
    }

    private void addVariableAttributes(String variableName, Variable.Builder builder) {
      String sql = getDatasource().getSettings().isMultipleDatasources()
          ? String
          .format("SELECT * FROM %s WHERE %s = ? AND %s = ? AND %s = ? ", ESC_VARIABLE_ATTRIBUTES_TABLE, ESC_DATASOURCE_COLUMN,
              ESC_VALUE_TABLE_COLUMN, ESC_VARIABLE_COLUMN)
          : String.format("SELECT * FROM %s WHERE %s = ? AND %s = ? ", ESC_VARIABLE_ATTRIBUTES_TABLE, ESC_VALUE_TABLE_COLUMN,
          ESC_VARIABLE_COLUMN);
      Object[] params = getDatasource().getSettings().isMultipleDatasources() ? new Object[]{
          getDatasource().getName(), getName(), variableName} : new Object[]{getName(), variableName};
      builder.addAttributes(getDatasource().getJdbcTemplate().query(sql, params, new AttributeRowMapper()));
    }

    private void addVariableCategories(final String variableName, Variable.Builder builder) {
      String sql = getDatasource().getSettings().isMultipleDatasources()
          ? String.format("SELECT %s, %s FROM %s WHERE %s = ? AND %s= ? AND %s = ?", ESC_NAME_COLUMN, ESC_MISSING_COLUMN,
          ESC_CATEGORIES_TABLE, ESC_DATASOURCE_COLUMN, ESC_VALUE_TABLE_COLUMN, ESC_VARIABLE_COLUMN)
          : String.format("SELECT %s, %s FROM %s WHERE %s= ? AND %s = ?", ESC_NAME_COLUMN, ESC_MISSING_COLUMN, ESC_CATEGORIES_TABLE,
          ESC_VALUE_TABLE_COLUMN, ESC_VARIABLE_COLUMN);
      Object[] params = getDatasource().getSettings().isMultipleDatasources() ? new Object[]{
          getDatasource().getName(), getName(), variableName} : new Object[]{getName(), variableName};

      builder.addCategories(getDatasource().getJdbcTemplate().query(sql, params, new RowMapper<Category>() {

        @Override
        public Category mapRow(ResultSet rs, int rowNum) throws SQLException {
          String categoryName = rs.getString(NAME_COLUMN);
          Category.Builder catBuilder = Category.Builder.newCategory(categoryName)
              .missing(rs.getBoolean(MISSING_COLUMN));
          addVariableCategoryAtributes(variableName, categoryName, catBuilder);
          return catBuilder.build();
        }
      }));
    }

    private void addVariableCategoryAtributes(String variableName, String categoryName, Category.Builder builder) {
      String sql = getDatasource().getSettings().isMultipleDatasources()
          ? String.format("SELECT * FROM %s WHERE %s = ? AND %s = ? AND %s = ? AND %s = ?", ESC_CATEGORY_ATTRIBUTES_TABLE,
          ESC_DATASOURCE_COLUMN, ESC_VALUE_TABLE_COLUMN, ESC_VARIABLE_COLUMN, ESC_CATEGORY_COLUMN)
          : String.format("SELECT * FROM %s WHERE %s = ? AND %s = ? AND %s = ?", ESC_CATEGORY_ATTRIBUTES_TABLE,
          ESC_VALUE_TABLE_COLUMN, ESC_VARIABLE_COLUMN, ESC_CATEGORY_COLUMN);
      Object[] params = getDatasource().getSettings().isMultipleDatasources()
          ? new Object[]{getDatasource().getName(), getName(), variableName, categoryName}
          : new Object[]{getName(), variableName, categoryName};

      builder.addAttributes(getDatasource().getJdbcTemplate().query(sql, params, new AttributeRowMapper()));
    }
  }

  private static class AttributeRowMapper implements RowMapper<Attribute> {
    @Override
    public Attribute mapRow(ResultSet rs, int rowNum) throws SQLException {
      String attributeName = rs.getString(NAME_COLUMN);
      String attributeNamespace = mayNotHaveColumn(rs, NAMESPACE_COLUMN);
      String attributeValue = rs.getString(VALUE_COLUMN);
      String attributeLocale = rs.getString(LOCALE_COLUMN);

      Attribute.Builder attr = Attribute.Builder.newAttribute(attributeName).withNamespace(attributeNamespace);
      if (attributeLocale != null && attributeLocale.length() > 0) {
        attr.withValue(new Locale(attributeLocale), attributeValue);
      } else {
        attr.withValue(attributeValue);
      }
      return attr.build();
    }

    @Nullable
    private String mayNotHaveColumn(ResultSet rs, String column) {
      try {
        return rs.getString(column);
      } catch (SQLException e) {
        return null;
      }
    }
  }

  private boolean metadataTablesExist() {
    DatabaseSnapshot snapshot = getDatasource().getDatabaseSnapshot();

    return snapshot.get(newTable(VARIABLES_TABLE)) != null &&
        snapshot.get(newTable(VARIABLE_ATTRIBUTES_TABLE)) != null &&
        snapshot.get(newTable(CATEGORIES_TABLE)) != null;
  }

  private void createSqlTable(String sqlTableName) {
    CreateTableChangeBuilder ctc = CreateTableChangeBuilder.newBuilder().tableName(sqlTableName);
    ctc.withColumn(getSettings().getEntityIdentifierColumn(), "VARCHAR(255)").primaryKey(!isSettingsMultilines());

    createTimestampColumns(ctc);
    List<Change> changes = Lists.<Change>newArrayList(ctc.build());

    if (hasCreatedTimestampColumn()) {
      changes.add(CreateIndexChangeBuilder.newBuilder().name(String.format("idx_%s_created", sqlTableName)).table(sqlTableName)
          .withColumn(getCreatedTimestampColumnName()).build());
    }

    if (hasUpdatedTimestampColumn()) {
      changes.add(CreateIndexChangeBuilder.newBuilder().name(String.format("idx_%s_updated", sqlTableName)).table(sqlTableName)
          .withColumn(getUpdatedTimestampColumnName()).build());
    }

    getDatasource().doWithDatabase(new ChangeDatabaseCallback(changes));
  }

  private void createTimestampColumns(CreateTableChangeBuilder changeWithColumns) {
    if (hasCreatedTimestampColumn()) {
      changeWithColumns.withColumn(getCreatedTimestampColumnName(), "TIMESTAMP", JdbcDatasource.EPOCH).notNull();
    }

    if (hasUpdatedTimestampColumn()) {
      changeWithColumns.withColumn(getUpdatedTimestampColumnName(), "TIMESTAMP", JdbcDatasource.EPOCH).notNull();
    }
  }

  String getEntityIdentifierColumnSql() {
    return getDatasource().escapeColumnName(getSettings().getEntityIdentifierColumn());
  }

  String extractEntityIdentifier(ResultSet rs) throws SQLException {
    return rs.getObject(1).toString();
  }

  String getVariableSqlName(String variableName) {
    if (getVariablesMap().containsKey(variableName)) return getVariablesMap().get(variableName);

    String normVariableName = String.format("%s", TableUtils.normalize(variableName, 64));

    if (!getVariablesMap().isEmpty()) {
      // make sure the sql variable name has not been already assigned
      int i = 0;
      while (getVariablesMap().containsValue(normVariableName)) {
        normVariableName = normVariableName + i;
        i++;
      }
    }

    getVariablesMap().put(variableName, normVariableName);

    return normVariableName;
  }

  String getVariableName(String variableSqlName) {
    BiMap<String, String> tmp = getVariablesMap().inverse();

    if (tmp.containsKey(variableSqlName.toLowerCase())) return tmp.get(variableSqlName.toLowerCase());

    if (tmp.containsKey(variableSqlName)) return tmp.get(variableSqlName);

    return variableSqlName;
  }

  public void refreshTable() {
    clearTimestamps();
    getDatasource().databaseChanged();
    // no need to refresh a view
    if (tableOrView instanceof Table) {
      tableOrView = getDatasource().getDatabaseSnapshot().get(newTable(settings.getSqlTableName()));
    }
  }

  public synchronized void refreshVariablesMap() {
    variableMap = null;
    getVariablesMap();
  }

  private BiMap<String, String> getVariablesMap() {
    if (variableMap != null) return variableMap;

    variableMap = HashBiMap.create();

    if (getDatasource().getSettings().isUseMetadataTables()) {
      String sql = getDatasource().getSettings().isMultipleDatasources()
          ? String
          .format("SELECT %s, %s FROM %s WHERE %s = ? AND %s = ?", ESC_NAME_COLUMN, ESC_SQL_NAME_COLUMN, ESC_VARIABLES_TABLE,
              ESC_DATASOURCE_COLUMN, ESC_VALUE_TABLE_COLUMN)
          : String.format("SELECT %s, %s FROM %s WHERE %s = ?", ESC_NAME_COLUMN, ESC_SQL_NAME_COLUMN, ESC_VARIABLES_TABLE,
          ESC_VALUE_TABLE_COLUMN);
      Object[] params = getDatasource().getSettings().isMultipleDatasources() ? new Object[]{
          getDatasource().getName(), getName()} : new Object[]{getName()};

      List<Map.Entry<String, String>> res = getDatasource().getJdbcTemplate()
          .query(sql, params, (rs, rowNum) -> Maps.immutableEntry(rs.getString(NAME_COLUMN), rs.getString(SQL_NAME_COLUMN)));

      for (Map.Entry<String, String> e : res) variableMap.put(e.getKey(), e.getValue());
    }

    return variableMap;
  }

  //
  // Inner Classes
  //

  private class ValueSetTimestamps implements Timestamps {

    private final VariableEntity entity;

    private final String createdTimestampColumnName;

    private final String updatedTimestampColumnName;

    private ValueSetTimestamps(VariableEntity entity, String createdTimestampColumnName, String updatedTimestampColumnName) {
      this.entity = entity;
      this.createdTimestampColumnName = createdTimestampColumnName;
      this.updatedTimestampColumnName = updatedTimestampColumnName;
    }

    @NotNull
    @Override
    public Value getLastUpdate() {
      if (Strings.isNullOrEmpty(updatedTimestampColumnName)) return DateTimeType.get().nullValue();

      String sql = appendIdentifierColumns(
          String.format("SELECT %s FROM %s", getDatasource().escapeColumnName(updatedTimestampColumnName), getDatasource().escapeTableName(getSqlName())));
      return DateTimeType.get().valueOf(executeQuery(sql));
    }

    @NotNull
    @Override
    public Value getCreated() {
      if (Strings.isNullOrEmpty(createdTimestampColumnName)) return DateTimeType.get().nullValue();

      String sql = appendIdentifierColumns(
          String.format("SELECT %s FROM %s", getDatasource().escapeColumnName(createdTimestampColumnName), getDatasource().escapeTableName(getSqlName())));

      return DateTimeType.get().valueOf(executeQuery(sql));
    }

    private String appendIdentifierColumns(String sql) {
      StringBuilder sb = new StringBuilder(sql);
      sb.append(" WHERE ");
      sb.append(getDatasource().escapeColumnName(getSettings().getEntityIdentifierColumn())).append(" = ?");
      return sb.toString();
    }

    private Date executeQuery(String sql) {
      String[] entityIdentifierColumnValues = entity.getIdentifier().split("-");
      try {
        return getDatasource().getJdbcTemplate().queryForObject(sql, entityIdentifierColumnValues, Date.class);
      } catch (Exception e) {
        return null;
      }
    }
  }

}
