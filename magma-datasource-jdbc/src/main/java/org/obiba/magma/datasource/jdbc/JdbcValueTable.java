package org.obiba.magma.datasource.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.SortedSet;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

import org.obiba.magma.AbstractVariableValueSource;
import org.obiba.magma.Attribute;
import org.obiba.magma.Category;
import org.obiba.magma.Datasource;
import org.obiba.magma.Initialisable;
import org.obiba.magma.MagmaRuntimeException;
import org.obiba.magma.NoSuchValueSetException;
import org.obiba.magma.Timestamps;
import org.obiba.magma.Value;
import org.obiba.magma.ValueSet;
import org.obiba.magma.ValueType;
import org.obiba.magma.Variable;
import org.obiba.magma.VariableEntity;
import org.obiba.magma.VariableValueSource;
import org.obiba.magma.VectorSource;
import org.obiba.magma.datasource.jdbc.JdbcDatasource.ChangeDatabaseCallback;
import org.obiba.magma.datasource.jdbc.support.NameConverter;
import org.obiba.magma.support.AbstractValueTable;
import org.obiba.magma.support.AbstractVariableEntityProvider;
import org.obiba.magma.support.Initialisables;
import org.obiba.magma.support.NullTimestamps;
import org.obiba.magma.support.VariableEntityBean;
import org.obiba.magma.type.DateTimeType;
import org.springframework.jdbc.core.RowMapper;

import com.google.common.collect.Lists;

import liquibase.change.ChangeWithColumns;
import liquibase.change.ColumnConfig;
import liquibase.change.ConstraintsConfig;
import liquibase.change.core.CreateTableChange;
import liquibase.change.core.DropTableChange;
import liquibase.structure.core.Column;
import liquibase.snapshot.DatabaseSnapshot;
import liquibase.structure.core.PrimaryKey;
import liquibase.structure.core.Table;

@SuppressWarnings("OverlyCoupledClass")
class JdbcValueTable extends AbstractValueTable {

  private final JdbcValueTableSettings settings;

  private Table table;

  private String escapedSqlTableName;

  private String escapedVariablesSqlTableName;

  private String escapedVariableAttributesSqlTableName;

  private String escapedCategoriesSqlTableName;

  JdbcValueTable(Datasource datasource, JdbcValueTableSettings settings) {
    super(datasource, settings.getMagmaTableName());
    this.settings = settings;

    if(getDatasource().getDatabaseSnapshot().get(new Table(null, null, settings.getSqlTableName())) == null) {
      createSqlTable(settings.getSqlTableName());
      getDatasource().databaseChanged();
    }

    table = getDatasource().getDatabaseSnapshot().get(new Table(null, null, settings.getSqlTableName()));
    setVariableEntityProvider(new JdbcVariableEntityProvider(getEntityType()));
  }

  JdbcValueTable(Datasource datasource, Table table, String entityType) {
    this(datasource, new JdbcValueTableSettings(table.getName(), NameConverter.toMagmaName(table.getName()), entityType,
        getEntityIdentifierColumns(table)));
  }

  //
  // AbstractValueTable Methods
  //

  @Override
  public void initialise() {
    super.initialise();
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
    if(hasCreatedTimestampColumn() && hasUpdatedTimestampColumn()) {
      return new Timestamps() {

        @NotNull
        @Override
        public Value getLastUpdate() {
          String sql = "SELECT MAX(" + getUpdatedTimestampColumnName() + ") FROM " + escapedSqlTableName;
          return DateTimeType.get().valueOf(getDatasource().getJdbcTemplate().queryForObject(sql, Date.class));
        }

        @NotNull
        @Override
        public Value getCreated() {
          String sql = "SELECT MIN(" + getCreatedTimestampColumnName() + ") FROM " + escapedSqlTableName;
          return DateTimeType.get().valueOf(getDatasource().getJdbcTemplate().queryForObject(sql, Date.class));
        }

      };
    }
    return NullTimestamps.get();
  }

  //
  // Methods
  //

  public void drop() {
    DropTableChange dtt = new DropTableChange();
    dtt.setTableName(getSqlName());

    getDatasource().doWithDatabase(new ChangeDatabaseCallback(dtt));
  }

  public JdbcValueTableSettings getSettings() {
    return settings;
  }

  String getSqlName() {
    return NameConverter.toSqlName(getName());
  }

  void tableChanged() {
    table = getDatasource().getDatabaseSnapshot().get(new Table(null, null, settings.getSqlTableName()));
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
    addVariableValueSource(new JdbcVariableValueSource(source));
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

      // MAGMA-100
      if(escapedVariablesSqlTableName == null) {
        escapedVariablesSqlTableName = getDatasource().escapeSqlTableName(JdbcValueTableWriter.VARIABLE_METADATA_TABLE);
      }

      List<Variable> results = getDatasource().getJdbcTemplate()
          .query("SELECT * FROM " + escapedVariablesSqlTableName + " WHERE value_table = ?",
              new Object[] { getSqlName() }, new VariableRowMapper());

      for(Variable variable : results) {
        addVariableValueSource(new JdbcVariableValueSource(variable));
      }
    } else {
      List<String> reserved = Lists.newArrayList(getSettings().getEntityIdentifierColumns());

      if(getCreatedTimestampColumnName() != null) reserved.add(getCreatedTimestampColumnName());

      if(getCreatedTimestampColumnName() != null) reserved.add(getUpdatedTimestampColumnName());

      for(Column column : table.getColumns()) {
        if(!reserved.contains(column.getName()) && !reserved.contains(column.getName().toLowerCase())) {
          addVariableValueSource(new JdbcVariableValueSource(getEntityType(), column));
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
      Variable.Builder builder = Variable.Builder.newVariable(variableName, valueType, getEntityType())
          .mimeType(mimeType).unit(units);

      if(isRepeatable) {
        builder.repeatable();
        builder.occurrenceGroup(occurrenceGroup);
      }

      addVariableAttributes(variableName, builder);
      addVariableCategories(variableName, builder);

      return builder.build();
    }

    private void addVariableAttributes(String variableName, Variable.Builder builder) {
      // MAGMA-100
      if(escapedVariableAttributesSqlTableName == null) {
        escapedVariableAttributesSqlTableName = getDatasource()
            .escapeSqlTableName(JdbcValueTableWriter.ATTRIBUTE_METADATA_TABLE);
      }

      builder.addAttributes(
          getDatasource().getJdbcTemplate().query("SELECT * FROM " + escapedVariableAttributesSqlTableName +
                  " WHERE value_table = ? AND variable_name = ?", new Object[] { getSqlName(), variableName },
              new AttributeRowMapper()));
    }

    private void addVariableCategories(String variableName, Variable.Builder builder) {
      // MAGMA-100
      if(escapedCategoriesSqlTableName == null) {
        escapedCategoriesSqlTableName = getDatasource()
            .escapeSqlTableName(JdbcValueTableWriter.CATEGORY_METADATA_TABLE);
      }

      builder.addCategories(getDatasource().getJdbcTemplate()
          .query("SELECT * FROM " + escapedCategoriesSqlTableName + " WHERE value_table = ? AND variable_name = ?",
              new Object[] { getSqlName(), variableName }, new RowMapper<Category>() {
                @Override
                public Category mapRow(ResultSet rs, int rowNum) throws SQLException {
                  String categoryName = rs.getString("name");
                  String categoryCode = rs.getString("code");
                  return Category.Builder.newCategory(categoryName).withCode(categoryCode).build();
                }
              }));
    }
  }

  private static class AttributeRowMapper implements RowMapper<Attribute> {
    @Override
    public Attribute mapRow(ResultSet rs, int rowNum) throws SQLException {
      String attributeName = rs.getString(JdbcValueTableWriter.ATTRIBUTE_NAME_COLUMN);
      String attributeNamespace = mayNotHaveColumn(rs, JdbcValueTableWriter.ATTRIBUTE_NAMESPACE_COLUMN);
      String attributeValue = rs.getString(JdbcValueTableWriter.ATTRIBUTE_VALUE_COLUMN);
      String attributeLocale = rs.getString(JdbcValueTableWriter.ATTRIBUTE_LOCALE_COLUMN);

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
    return snapshot.get(new Table(null, null, JdbcValueTableWriter.VARIABLE_METADATA_TABLE)) != null &&
        snapshot.get(new Table(null, null, JdbcValueTableWriter.ATTRIBUTE_METADATA_TABLE)) != null &&
        snapshot.get(new Table(null, null, JdbcValueTableWriter.CATEGORY_METADATA_TABLE)) != null;
  }

  private void createSqlTable(String sqlTableName) {
    CreateTableChange ctc = new CreateTableChange();
    ctc.setTableName(sqlTableName);

    // Create the table initially with just one column -- entity_id -- the primary key.
    ColumnConfig column = new ColumnConfig();
    column.setName(JdbcValueTableWriter.ENTITY_ID_COLUMN);
    column.setType("VARCHAR(255)");
    ConstraintsConfig constraints = new ConstraintsConfig();
    constraints.setPrimaryKey(true);
    column.setConstraints(constraints);

    ctc.addColumn(column);
    createTimestampColumns(ctc);

    getDatasource().doWithDatabase(new ChangeDatabaseCallback(ctc));
  }

  private void createTimestampColumns(ChangeWithColumns changeWithColumns) {
    if(hasCreatedTimestampColumn()) {
      changeWithColumns.addColumn(createTimestampColumn(getCreatedTimestampColumnName()));
    }
    if(hasUpdatedTimestampColumn()) {
      changeWithColumns.addColumn(createTimestampColumn(getUpdatedTimestampColumnName()));
    }
  }

  private ColumnConfig createTimestampColumn(String columnName) {
    ColumnConfig column = new ColumnConfig();
    column.setName(columnName);
    column.setType("DATETIME");

    ConstraintsConfig constraints = new ConstraintsConfig();
    constraints.setNullable(false);
    column.setConstraints(constraints);
    return column;
  }

  private String getEntityIdentifierColumnsSql() {
    StringBuilder sql = new StringBuilder();
    List<String> entityIdentifierColumns = getSettings().getEntityIdentifierColumns();
    for(int i = 0; i < entityIdentifierColumns.size(); i++) {
      if(i > 0) sql.append(",");
      sql.append(entityIdentifierColumns.get(i));
    }
    return sql.toString();
  }

  private String buildEntityIdentifier(ResultSet rs) throws SQLException {
    StringBuilder entityIdentifier = new StringBuilder();
    for(int i = 1; i <= getSettings().getEntityIdentifierColumns().size(); i++) {
      if(i > 1) {
        entityIdentifier.append('-');
      }
      entityIdentifier.append(rs.getObject(i));
    }

    return entityIdentifier.toString();
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
          "SELECT MAX(" + updatedTimestampColumnName + ") FROM " + escapedSqlTableName);
      return DateTimeType.get().valueOf(executeQuery(sql));
    }

    @NotNull
    @Override
    public Value getCreated() {
      String sql = appendIdentifierColumns(
          "SELECT MIN(" + updatedTimestampColumnName + ") FROM " + escapedSqlTableName);
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

  class JdbcVariableEntityProvider extends AbstractVariableEntityProvider implements Initialisable {

    private Set<VariableEntity> entities = new LinkedHashSet<>();

    JdbcVariableEntityProvider(String entityType) {
      super(entityType);
    }

    @Override
    public void initialise() {
      entities = new LinkedHashSet<>();

      // MAGMA-100
      if(escapedSqlTableName == null) {
        escapedSqlTableName = getDatasource().escapeSqlTableName(getSqlName());
      }

      List<VariableEntity> results = getDatasource().getJdbcTemplate()
          .query("SELECT " + getEntityIdentifierColumnsSql() + " FROM " + escapedSqlTableName,
              new RowMapper<VariableEntity>() {
                @Override
                public VariableEntity mapRow(ResultSet rs, int rowNum) throws SQLException {
                  return new VariableEntityBean(JdbcValueTable.this.getEntityType(), buildEntityIdentifier(rs));
                }
              });

      entities.addAll(results);
    }

    @NotNull
    @Override
    public Set<VariableEntity> getVariableEntities() {
      return Collections.unmodifiableSet(entities);
    }

  }

  class JdbcVariableValueSource extends AbstractVariableValueSource implements VariableValueSource, VectorSource {
    //
    // Instance Variables
    //

    private final Variable variable;

    private final String columnName;

    //
    // Constructors
    //

    JdbcVariableValueSource(String entityType, Column column) {
      columnName = column.getName();
      variable = Variable.Builder
          .newVariable(columnName, SqlTypes.valueTypeFor(column.getType().getDataTypeId()), entityType).build();
    }

    JdbcVariableValueSource(String entityType, ColumnConfig columnConfig) {
      columnName = columnConfig.getName();
      variable = Variable.Builder
          .newVariable(columnName, SqlTypes.valueTypeFor(columnConfig.getType()), entityType).build();
    }

    JdbcVariableValueSource(Variable variable) {
      this.variable = variable;
      columnName = NameConverter.toSqlName(variable.getName());
    }

    //
    // VariableValueSource Methods
    //

    @NotNull
    @Override
    public Variable getVariable() {
      return variable;
    }

    @NotNull
    @Override
    public Value getValue(ValueSet valueSet) {
      JdbcValueSet jdbcValueSet = (JdbcValueSet) valueSet;
      return jdbcValueSet.getValue(variable);
    }

    @NotNull
    @Override
    public ValueType getValueType() {
      return variable.getValueType();
    }

    @Override
    public boolean supportVectorSource() {
      return true;
    }

    @NotNull
    @Override
    public VectorSource asVectorSource() {
      return this;
    }

    @Override
    public Iterable<Value> getValues(final SortedSet<VariableEntity> entities) {

      return new Iterable<Value>() {

        @Override
        public Iterator<Value> iterator() {
          try {
            return new ValueIterator(getDatasource().getJdbcTemplate().getDataSource().getConnection(), entities);
          } catch(SQLException e) {
            throw new RuntimeException(e);
          }
        }

      };
    }

    private class ValueIterator implements Iterator<Value> {

      private final Connection connection;

      private final PreparedStatement statement;

      private final ResultSet rs;

      private final Iterator<VariableEntity> resultEntities;

      private boolean hasNextResults;

      private boolean closed = false;

      @edu.umd.cs.findbugs.annotations.SuppressWarnings("SQL_PREPARED_STATEMENT_GENERATED_FROM_NONCONSTANT_STRING")
      private ValueIterator(Connection connection, Iterable<VariableEntity> entities) throws SQLException {
        this.connection = connection;
        String column = getEntityIdentifierColumnsSql();
        statement = connection.prepareStatement("SELECT " + column + "," + columnName +
            " FROM " + escapedSqlTableName + " ORDER BY " + column);
        rs = statement.executeQuery();
        hasNextResults = rs.next();
        resultEntities = entities.iterator();
        closeCursorIfNecessary();
      }

      @Override
      public boolean hasNext() {
        return resultEntities.hasNext();
      }

      @Override
      public Value next() {
        if(!hasNext()) {
          throw new NoSuchElementException();
        }

        String nextEntity = resultEntities.next().getIdentifier();
        try {
          // Scroll until we find the required entity or reach the end of the results
          while(hasNextResults && !buildEntityIdentifier(rs).equals(nextEntity)) {
            hasNextResults = rs.next();
          }

          Value value = null;
          if(hasNextResults) {
            value = variable.getValueType().valueOf(rs.getObject(columnName));
          }
          closeCursorIfNecessary();
          return value == null //
              ? getVariable().isRepeatable() ? getValueType().nullSequence() : getValueType().nullValue() //
              : value;
        } catch(SQLException e) {
          throw new RuntimeException(e);
        }
      }

      @Override
      public void remove() {
        throw new UnsupportedOperationException();
      }

      private void closeCursorIfNecessary() {
        if(!closed) {
          // Close the cursor if we don't have any more results or no more entities to return
          if(!hasNextResults || !hasNext()) {
            closed = true;
            closeQuietly(rs, statement, connection);
          }
        }
      }

      @SuppressWarnings({ "OverlyStrongTypeCast", "ChainOfInstanceofChecks" })
      private void closeQuietly(Object... objs) {
        if(objs != null) {
          for(Object o : objs) {
            try {
              if(o instanceof ResultSet) {
                ((ResultSet) o).close();
              }
              if(o instanceof Statement) {
                ((Statement) o).close();
              }
              if(o instanceof Connection) {
                ((Connection) o).close();
              }
            } catch(SQLException e) {
              // ignored
            }
          }
        }
      }
    }
  }

}
