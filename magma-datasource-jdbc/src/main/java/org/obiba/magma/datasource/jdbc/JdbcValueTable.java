package org.obiba.magma.datasource.jdbc;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

import org.obiba.magma.Attribute;
import org.obiba.magma.Category;
import org.obiba.magma.Datasource;
import org.obiba.magma.MagmaRuntimeException;
import org.obiba.magma.NoSuchValueSetException;
import org.obiba.magma.Timestamps;
import org.obiba.magma.Value;
import org.obiba.magma.ValueSet;
import org.obiba.magma.ValueType;
import org.obiba.magma.Variable;
import org.obiba.magma.VariableEntity;
import org.obiba.magma.datasource.jdbc.JdbcDatasource.ChangeDatabaseCallback;
import org.obiba.magma.datasource.jdbc.support.CreateIndexChangeBuilder;
import org.obiba.magma.datasource.jdbc.support.CreateTableChangeBuilder;
import org.obiba.magma.support.AbstractValueTable;
import org.obiba.magma.support.Initialisables;
import org.obiba.magma.support.NullTimestamps;
import org.obiba.magma.type.DateTimeType;
import org.springframework.jdbc.core.RowMapper;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import liquibase.change.Change;
import liquibase.change.core.DropTableChange;
import liquibase.snapshot.DatabaseSnapshot;
import liquibase.structure.core.Column;
import liquibase.structure.core.PrimaryKey;
import liquibase.structure.core.Table;

import static org.obiba.magma.datasource.jdbc.JdbcValueTableWriter.*;
import static org.obiba.magma.datasource.jdbc.support.TableUtils.newTable;

@SuppressWarnings("OverlyCoupledClass")
class JdbcValueTable extends AbstractValueTable {

  private final JdbcValueTableSettings settings;

  private Table table;

  private BiMap<String, String> variableMap;

  JdbcValueTable(Datasource datasource, JdbcValueTableSettings settings) {
    super(datasource, settings.getMagmaTableName());
    this.settings = settings;

    if(getDatasource().getDatabaseSnapshot().get(newTable(settings.getSqlTableName())) == null) {
      createSqlTable(settings.getSqlTableName());
      getDatasource().databaseChanged();
    }

    table = getDatasource().getDatabaseSnapshot().get(newTable(settings.getSqlTableName()));
    setVariableEntityProvider(new JdbcVariableEntityProvider(this));
  }

  JdbcValueTable(Datasource datasource, String tableName, Table table, String entityType) {
    this(datasource,
        new JdbcValueTableSettings(table.getName(), tableName, entityType, getEntityIdentifierColumns(table)));
  }

  //
  // AbstractValueTable Methods
  //

  @Override
  public void initialise() {
    super.initialise();
    refreshVariablesMap();
    initialiseVariableValueSources();
    Initialisables.initialise(getVariableEntityProvider());
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

  @Override
  public ValueSet getValueSet(VariableEntity entity) throws NoSuchValueSetException {
    return new JdbcValueSet(this, entity);
  }

  @Override
  public Timestamps getValueSetTimestamps(VariableEntity entity) throws NoSuchValueSetException {
    if(hasCreatedTimestampColumn() && hasUpdatedTimestampColumn()) {
      return new ValueSetTimestamps(entity, getUpdatedTimestampColumnName());
    }
    return NullTimestamps.get();
  }

  @NotNull
  @Override
  public Timestamps getTimestamps() {
    return new JdbcValueTableTimestamps(this);
  }

  //
  // Methods
  //

  public void drop() {
    DropTableChange dtt = new DropTableChange();
    dtt.setTableName(getSqlName());
    getDatasource().doWithDatabase(new ChangeDatabaseCallback(dtt));
    getDatasource().databaseChanged();
    if(getDatasource().getSettings().isUseMetadataTables()) {
      dropCategoriesMetaData();
      dropVariablesMetaData();
      dropTableMetaData();
    }
  }

  private void dropCategoriesMetaData() {
    Object[] params = getDatasource().getSettings().isMultipleDatasources() ? new Object[] { getDatasource().getName(),
        getName() } : new Object[] { getName() };

    String sql = getDatasource().getSettings().isMultipleDatasources()
        ? String.format("DELETE FROM %s WHERE %s = ? AND %s = ?", CATEGORY_ATTRIBUTES_TABLE, DATASOURCE_COLUMN,
        VALUE_TABLE_COLUMN)
        : String.format("DELETE FROM %s WHERE %s = ?", CATEGORY_ATTRIBUTES_TABLE, VALUE_TABLE_COLUMN);
    getDatasource().getJdbcTemplate().update(sql, params);

    sql = getDatasource().getSettings().isMultipleDatasources()
        ? String
        .format("DELETE FROM %s WHERE %s = ? AND %s = ?", CATEGORIES_TABLE, DATASOURCE_COLUMN, VALUE_TABLE_COLUMN)
        : String.format("DELETE FROM %s WHERE %s = ?", CATEGORIES_TABLE, VALUE_TABLE_COLUMN);
    getDatasource().getJdbcTemplate().update(sql, params);
  }

  private void dropVariablesMetaData() {
    Object[] params = getDatasource().getSettings().isMultipleDatasources() ? new Object[] { getDatasource().getName(),
        getName() } : new Object[] { getName() };

    String sql = getDatasource().getSettings().isMultipleDatasources()
        ? String.format("DELETE FROM %s WHERE %s = ? AND %s = ?", VARIABLE_ATTRIBUTES_TABLE, DATASOURCE_COLUMN,
        VALUE_TABLE_COLUMN)
        : String.format("DELETE FROM %s WHERE %s = ?", VARIABLE_ATTRIBUTES_TABLE, VALUE_TABLE_COLUMN);
    getDatasource().getJdbcTemplate().update(sql, params);

    sql = getDatasource().getSettings().isMultipleDatasources()
        ? String
        .format("DELETE FROM %s WHERE %s = ? AND %s = ?", VARIABLES_TABLE, DATASOURCE_COLUMN, VALUE_TABLE_COLUMN)
        : String.format("DELETE FROM %s WHERE %s = ?", VARIABLES_TABLE, VALUE_TABLE_COLUMN);
    getDatasource().getJdbcTemplate().update(sql, params);
  }

  private void dropTableMetaData() {
    Object[] params = getDatasource().getSettings().isMultipleDatasources() ? new Object[] { getDatasource().getName(),
        getName() } : new Object[] { getName() };
    String sql = getDatasource().getSettings().isMultipleDatasources()
        ? String.format("DELETE FROM %s WHERE %s = ? AND %s = ?", VALUE_TABLES_TABLE, DATASOURCE_COLUMN, NAME_COLUMN)
        : String.format("DELETE FROM %s WHERE %s = ?", VALUE_TABLES_TABLE, NAME_COLUMN);
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
    return getSettings().isCreatedTimestampColumnNameProvided() ||
        getDatasource().getSettings().isCreatedTimestampColumnNameProvided();
  }

  String getCreatedTimestampColumnName() {
    return getSettings().isCreatedTimestampColumnNameProvided()
        ? getSettings().getCreatedTimestampColumnName()
        : getDatasource().getSettings().getDefaultCreatedTimestampColumnName();
  }

  boolean hasUpdatedTimestampColumn() {
    return getSettings().isUpdatedTimestampColumnNameProvided() ||
        getDatasource().getSettings().isUpdatedTimestampColumnNameProvided();
  }

  String getUpdatedTimestampColumnName() {
    return getSettings().isUpdatedTimestampColumnNameProvided()
        ? getSettings().getUpdatedTimestampColumnName()
        : getDatasource().getSettings().getDefaultUpdatedTimestampColumnName();
  }

  void writeVariableValueSource(Variable source) {
    addVariableValueSource(new JdbcVariableValueSource(this, source));
  }

  static List<String> getEntityIdentifierColumns(Table table) {
    List<String> entityIdentifierColumns = new ArrayList<>();
    PrimaryKey pk = table.getPrimaryKey();

    for(Column column : table.getColumns()) {
      if(pk != null && pk.getColumns().contains(column)) {
        entityIdentifierColumns.add(column.getName());
      }
    }

    return entityIdentifierColumns;
  }

  private void initialiseVariableValueSources() {
    clearSources();

    if(getDatasource().getSettings().isUseMetadataTables()) {
      if(!metadataTablesExist()) {
        throw new MagmaRuntimeException("metadata tables not found");
      }

      String sql = getDatasource().getSettings().isMultipleDatasources()
          ? String
          .format("SELECT * FROM %s WHERE %s = ? AND %s = ?", VARIABLES_TABLE, DATASOURCE_COLUMN, VALUE_TABLE_COLUMN)
          : String.format("SELECT * FROM %s WHERE %s = ?", VARIABLES_TABLE, VALUE_TABLE_COLUMN);
      Object[] params = getDatasource().getSettings().isMultipleDatasources() ? new Object[] {
          getDatasource().getName(), getName() } : new Object[] { getName() };

      List<Variable> results = getDatasource().getJdbcTemplate().query(sql, params, new VariableRowMapper());

      for(Variable variable : results) {
        addVariableValueSource(new JdbcVariableValueSource(this, variable));
      }
    } else {
      List<String> reserved = Lists.newArrayList(getSettings().getEntityIdentifierColumns());

      if(getCreatedTimestampColumnName() != null) reserved.add(getCreatedTimestampColumnName());

      if(getCreatedTimestampColumnName() != null) reserved.add(getUpdatedTimestampColumnName());

      for(Column column : table.getColumns()) {
        if(!reserved.contains(column.getName()) && !reserved.contains(column.getName().toLowerCase())) {
          addVariableValueSource(new JdbcVariableValueSource(this, column));
        }
      }
    }
  }

  private class VariableRowMapper implements RowMapper<Variable> {

    @Override
    public Variable mapRow(ResultSet rs, int rowNum) throws SQLException {
      return buildVariableFromResultSet(rs);
    }

    private Variable buildVariableFromResultSet(ResultSet rs) throws SQLException {
      String variableName = rs.getString("name");
      ValueType valueType = ValueType.Factory.forName(rs.getString("value_type"));
      String mimeType = rs.getString("mime_type");
      String units = rs.getString("units");
      boolean isRepeatable = rs.getBoolean("is_repeatable");
      String occurrenceGroup = rs.getString("occurrence_group");
      int index = rs.getInt("index");
      Variable.Builder builder = Variable.Builder.newVariable(variableName, valueType, getEntityType())
          .mimeType(mimeType).unit(units).index(index);

      if(isRepeatable) {
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
          .format("SELECT * FROM %s WHERE %s = ? AND %s = ? AND %s = ? ", VARIABLE_ATTRIBUTES_TABLE, DATASOURCE_COLUMN,
              VALUE_TABLE_COLUMN, VARIABLE_COLUMN)
          : String.format("SELECT * FROM %s WHERE %s = ? AND %s = ? ", VARIABLE_ATTRIBUTES_TABLE, VALUE_TABLE_COLUMN,
              VARIABLE_COLUMN);
      Object[] params = getDatasource().getSettings().isMultipleDatasources() ? new Object[] {
          getDatasource().getName(), getName(), variableName } : new Object[] { getName(), variableName };
      builder.addAttributes(getDatasource().getJdbcTemplate().query(sql, params, new AttributeRowMapper()));
    }

    private void addVariableCategories(final String variableName, Variable.Builder builder) {
      String sql = getDatasource().getSettings().isMultipleDatasources()
          ? String.format("SELECT %s, %s FROM %s WHERE %s = ? AND %s= ? AND %s = ?", NAME_COLUMN, MISSING_COLUMN,
          CATEGORIES_TABLE, DATASOURCE_COLUMN, VALUE_TABLE_COLUMN, VARIABLE_COLUMN)
          : String.format("SELECT %s, %s FROM %s WHERE %s= ? AND %s = ?", NAME_COLUMN, MISSING_COLUMN, CATEGORIES_TABLE,
              VALUE_TABLE_COLUMN, VARIABLE_COLUMN);
      Object[] params = getDatasource().getSettings().isMultipleDatasources() ? new Object[] {
          getDatasource().getName(), getName(), variableName } : new Object[] { getName(), variableName };

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
          ? String.format("SELECT * FROM %s WHERE %s = ? AND %s = ? AND %s = ? AND %s = ?", CATEGORY_ATTRIBUTES_TABLE,
          DATASOURCE_COLUMN, VALUE_TABLE_COLUMN, VARIABLE_COLUMN, CATEGORY_COLUMN)
          : String.format("SELECT * FROM %s WHERE %s = ? AND %s = ? AND %s = ?", CATEGORY_ATTRIBUTES_TABLE,
              VALUE_TABLE_COLUMN, VARIABLE_COLUMN, CATEGORY_COLUMN);
      Object[] params = getDatasource().getSettings().isMultipleDatasources()
          ? new Object[] { getDatasource().getName(), getName(), variableName, categoryName }
          : new Object[] { getName(), variableName, categoryName };

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
      if(attributeLocale != null && attributeLocale.length() > 0) {
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
      } catch(SQLException e) {
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
    for(String idColumn : getSettings().getEntityIdentifierColumns()) {
      ctc.withColumn(idColumn, "VARCHAR(255)").primaryKey();
    }
    createTimestampColumns(ctc);
    List<Change> changes = Lists.<Change>newArrayList(ctc.build());

    if(hasCreatedTimestampColumn()) {
      changes.add(CreateIndexChangeBuilder.newBuilder().name("idx_created").table(sqlTableName)
          .withColumn(getCreatedTimestampColumnName()).build());
    }

    if(hasUpdatedTimestampColumn()) {
      changes.add(CreateIndexChangeBuilder.newBuilder().name("idx_updated").table(sqlTableName)
          .withColumn(getUpdatedTimestampColumnName()).build());
    }

    getDatasource().doWithDatabase(new ChangeDatabaseCallback(changes));
  }

  private void createTimestampColumns(CreateTableChangeBuilder changeWithColumns) {
    if(hasCreatedTimestampColumn()) {
      changeWithColumns.withColumn(getCreatedTimestampColumnName(), "DATETIME").notNull();
    }

    if(hasUpdatedTimestampColumn()) {
      changeWithColumns.withColumn(getUpdatedTimestampColumnName(), "DATETIME").notNull();
    }
  }

  String getEntityIdentifierColumnsSql() {
    StringBuilder sql = new StringBuilder();
    List<String> entityIdentifierColumns = getSettings().getEntityIdentifierColumns();
    for(int i = 0; i < entityIdentifierColumns.size(); i++) {
      if(i > 0) sql.append(",");
      sql.append(entityIdentifierColumns.get(i));
    }
    return sql.toString();
  }

  String buildEntityIdentifier(ResultSet rs) throws SQLException {
    StringBuilder entityIdentifier = new StringBuilder();
    for(int i = 1; i <= getSettings().getEntityIdentifierColumns().size(); i++) {
      if(i > 1) {
        entityIdentifier.append('-');
      }
      entityIdentifier.append(rs.getObject(i));
    }

    return entityIdentifier.toString();
  }

  String getVariableSqlName(String variableName) {
    if(getVariablesMap().containsKey(variableName)) return getVariablesMap().get(variableName);

    return variableName;
  }

  String getVariableName(String variableName) {
    BiMap<String, String> tmp = getVariablesMap().inverse();

    if(tmp.containsKey(variableName.toLowerCase())) return tmp.get(variableName.toLowerCase());

    if(tmp.containsKey(variableName)) return tmp.get(variableName);

    return variableName;
  }

  public void refreshTable() {
    getDatasource().databaseChanged();
    table = getDatasource().getDatabaseSnapshot().get(newTable(settings.getSqlTableName()));
  }

  public synchronized void refreshVariablesMap() {
    variableMap = null;
    getVariablesMap();
  }

  private BiMap<String, String> getVariablesMap() {
    if(variableMap != null) return variableMap;

    variableMap = HashBiMap.create();

    if(getDatasource().getSettings().isUseMetadataTables()) {
      String sql = getDatasource().getSettings().isMultipleDatasources()
          ? String
          .format("SELECT %s, %s FROM %s WHERE %s = ? AND %s = ?", NAME_COLUMN, SQL_NAME_COLUMN, VARIABLES_TABLE,
              DATASOURCE_COLUMN, VALUE_TABLE_COLUMN)
          : String.format("SELECT %s, %s FROM %s WHERE %s = ?", NAME_COLUMN, SQL_NAME_COLUMN, VARIABLES_TABLE,
              VALUE_TABLE_COLUMN);
      Object[] params = getDatasource().getSettings().isMultipleDatasources() ? new Object[] {
          getDatasource().getName(), getName() } : new Object[] { getName() };

      List<Map.Entry<String, String>> res = getDatasource().getJdbcTemplate()
          .query(sql, params, new RowMapper<Map.Entry<String, String>>() {
            @Override
            public Map.Entry<String, String> mapRow(ResultSet rs, int rowNum) throws SQLException {
              return Maps.immutableEntry(rs.getString(NAME_COLUMN), rs.getString(SQL_NAME_COLUMN));
            }
          });

      for(Map.Entry<String, String> e : res) variableMap.put(e.getKey(), e.getValue());
    }

    return variableMap;
  }

  //
  // Inner Classes
  //

  private class ValueSetTimestamps implements Timestamps {

    private final VariableEntity entity;

    private final String updatedTimestampColumnName;

    private ValueSetTimestamps(VariableEntity entity, String updatedTimestampColumnName) {
      this.entity = entity;
      this.updatedTimestampColumnName = updatedTimestampColumnName;
    }

    @NotNull
    @Override
    public Value getLastUpdate() {
      String sql = appendIdentifierColumns(
          String.format("SELECT MAX(%s) FROM %s", updatedTimestampColumnName, getSqlName()));
      return DateTimeType.get().valueOf(executeQuery(sql));
    }

    @NotNull
    @Override
    public Value getCreated() {
      String sql = appendIdentifierColumns(
          String.format("SELECT MIN(%s) FROM %s", updatedTimestampColumnName, getSqlName()));
      return DateTimeType.get().valueOf(executeQuery(sql));
    }

    private String appendIdentifierColumns(String sql) {
      StringBuilder sb = new StringBuilder(sql);
      sb.append(" WHERE ");
      List<String> entityIdentifierColumns = getSettings().getEntityIdentifierColumns();
      int nbIdentifiers = entityIdentifierColumns.size();
      for(int i = 0; i < nbIdentifiers; i++) {
        sb.append(entityIdentifierColumns.get(i)).append(" = ?");
        if(i < nbIdentifiers - 1) {
          sb.append(" AND ");
        }
      }
      return sb.toString();
    }

    private Date executeQuery(String sql) {
      String[] entityIdentifierColumnValues = entity.getIdentifier().split("-");
      return getDatasource().getJdbcTemplate().queryForObject(sql, entityIdentifierColumnValues, Date.class);
    }
  }

}
