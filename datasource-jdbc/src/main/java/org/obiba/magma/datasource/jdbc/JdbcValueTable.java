package org.obiba.magma.datasource.jdbc;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import liquibase.change.ColumnConfig;
import liquibase.change.ConstraintsConfig;
import liquibase.change.CreateTableChange;
import liquibase.database.structure.Column;
import liquibase.database.structure.DatabaseSnapshot;
import liquibase.database.structure.Table;

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
import org.obiba.magma.datasource.jdbc.JdbcDatasource.ChangeDatabaseCallback;
import org.obiba.magma.datasource.jdbc.support.NameConverter;
import org.obiba.magma.support.AbstractValueTable;
import org.obiba.magma.support.AbstractVariableEntityProvider;
import org.obiba.magma.support.Initialisables;
import org.obiba.magma.support.VariableEntityBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.RowMapper;

class JdbcValueTable extends AbstractValueTable {
  //
  // Constants
  //

  private static final Logger log = LoggerFactory.getLogger(JdbcValueTable.class);

  //
  // Instance Variables
  //

  private JdbcValueTableSettings settings;

  private Table table;

  private String escapedSqlTableName;

  private String escapedVariablesSqlTableName;

  private String escapedVariableAttributesSqlTableName;

  private String escapedCategoriesSqlTableName;

  //
  // Constructors
  //

  JdbcValueTable(JdbcDatasource datasource, JdbcValueTableSettings settings) {
    super(datasource, settings.getMagmaTableName());
    this.settings = settings;

    if(getDatasource().getDatabaseSnapshot().getTable(settings.getSqlTableName()) == null) {
      createSqlTable(settings.getSqlTableName());
      getDatasource().databaseChanged();
    }
    this.table = getDatasource().getDatabaseSnapshot().getTable(settings.getSqlTableName());
    super.setVariableEntityProvider(new JdbcVariableEntityProvider());
  }

  JdbcValueTable(JdbcDatasource datasource, Table table, String entityType) {
    this(datasource, new JdbcValueTableSettings(table.getName(), NameConverter.toMagmaName(table.getName()), entityType, getEntityIdentifierColumns(table)));
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
    return settings.getEntityType();
  }

  @Override
  public JdbcDatasource getDatasource() {
    return (JdbcDatasource) super.getDatasource();
  }

  @Override
  public ValueSet getValueSet(VariableEntity entity) throws NoSuchValueSetException {
    return new JdbcValueSet(this, entity);
  }

  //
  // Methods
  //

  public JdbcValueTableSettings getSettings() {
    return settings;
  }

  String getSqlName() {
    return NameConverter.toSqlName(getName());
  }

  void tableChanged() {
    table = getDatasource().getDatabaseSnapshot().getTable((settings.getSqlTableName()));
    initialise();
  }

  private static List<String> getEntityIdentifierColumns(Table table) {
    List<String> entityIdentifierColumns = new ArrayList<String>();
    for(Column column : table.getColumns()) {
      if(column.isPrimaryKey()) {
        entityIdentifierColumns.add(column.getName());
      }
    }
    return entityIdentifierColumns;
  }

  @SuppressWarnings("unchecked")
  private void initialiseVariableValueSources() {
    getSources().clear();

    if(getDatasource().getSettings().useMetadataTables()) {
      if(!metadataTablesExist()) {
        throw new MagmaRuntimeException("metadata tables not found");
      }

      // MAGMA-100
      if(escapedVariablesSqlTableName == null) {
        escapedVariablesSqlTableName = getDatasource().escapeSqlTableName(JdbcValueTableWriter.VARIABLE_METADATA_TABLE);
      }

      StringBuilder sql = new StringBuilder();
      sql.append("SELECT *");
      sql.append(" FROM ");
      sql.append(escapedVariablesSqlTableName);
      sql.append(" WHERE value_table = ?");

      List<Variable> results = getDatasource().getJdbcTemplate().query(sql.toString(), new Object[] { getSqlName() }, new RowMapper() {
        public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
          return buildVariableFromResultSet(rs);
        }
      });

      for(Variable variable : results) {
        addVariableValueSource(new JdbcVariableValueSource(variable));
      }
    } else {
      for(Column column : table.getColumns()) {
        if(!getSettings().getEntityIdentifierColumns().contains(column.getName())) {
          addVariableValueSource(new JdbcVariableValueSource(settings.getEntityType(), column));
        }
      }
    }
  }

  private boolean isBinaryDataType(Column column) {
    return column.getDataType() == java.sql.Types.BLOB || column.getDataType() == java.sql.Types.VARBINARY || column.getDataType() == java.sql.Types.LONGVARBINARY;
  }

  private boolean metadataTablesExist() {
    DatabaseSnapshot snapshot = getDatasource().getDatabaseSnapshot();
    return (snapshot.getTable(JdbcValueTableWriter.VARIABLE_METADATA_TABLE) != null && snapshot.getTable(JdbcValueTableWriter.ATTRIBUTE_METADATA_TABLE) != null && snapshot.getTable(JdbcValueTableWriter.CATEGORY_METADATA_TABLE) != null);
  }

  private Variable buildVariableFromResultSet(ResultSet rs) throws SQLException {
    String variableName = rs.getString("name");
    ValueType valueType = ValueType.Factory.forName(rs.getString("value_type"));
    String mimeType = rs.getString("mime_type");
    String units = rs.getString("units");
    boolean isRepeatable = rs.getBoolean("is_repeatable");
    String occurrenceGroup = rs.getString("occurrence_group");

    final Variable.Builder builder = Variable.Builder.newVariable(variableName, valueType, getEntityType()).mimeType(mimeType).unit(units);
    if(isRepeatable) {
      builder.repeatable();
      builder.occurrenceGroup(occurrenceGroup);
    }

    addVariableAttributes(variableName, builder);
    addVariableCategories(variableName, builder);

    return builder.build();
  }

  private void addVariableAttributes(String variableName, final Variable.Builder builder) {
    // MAGMA-100
    if(escapedVariableAttributesSqlTableName == null) {
      escapedVariableAttributesSqlTableName = getDatasource().escapeSqlTableName(JdbcValueTableWriter.ATTRIBUTE_METADATA_TABLE);
    }

    StringBuilder sql = new StringBuilder();
    sql.append("SELECT *");
    sql.append(" FROM ");
    sql.append(escapedVariableAttributesSqlTableName);
    sql.append(" WHERE value_table = ? AND variable_name = ?");

    getDatasource().getJdbcTemplate().query(sql.toString(), new Object[] { getSqlName(), variableName }, new RowMapper() {
      public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
        String attributeName = rs.getString("attribute_name");
        String attributeValue = rs.getString("attribute_value");
        String attributeLocale = rs.getString("attribute_locale");
        if(attributeLocale.length() > 0) {
          builder.addAttribute(attributeName, attributeValue, new Locale(attributeLocale));
        } else {
          builder.addAttribute(attributeName, attributeValue);
        }
        return attributeName;
      }
    });
  }

  private void addVariableCategories(String variableName, final Variable.Builder builder) {
    // MAGMA-100
    if(escapedCategoriesSqlTableName == null) {
      escapedCategoriesSqlTableName = getDatasource().escapeSqlTableName(JdbcValueTableWriter.CATEGORY_METADATA_TABLE);
    }

    StringBuilder sql = new StringBuilder();
    sql.append("SELECT *");
    sql.append(" FROM ");
    sql.append(escapedCategoriesSqlTableName);
    sql.append(" WHERE value_table = ? AND variable_name = ?");

    getDatasource().getJdbcTemplate().query(sql.toString(), new Object[] { getSqlName(), variableName }, new RowMapper() {
      public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
        String categoryName = rs.getString("name");
        String categoryCode = rs.getString("code");
        builder.addCategory(categoryName, categoryCode);
        return categoryName;
      }
    });
  }

  private void createSqlTable(String sqlTableName) {
    final CreateTableChange ctc = new CreateTableChange();
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

  private void createTimestampColumns(CreateTableChange ctc) {
    if(getDatasource().getSettings().isCreatedTimestampColumnNameProvided()) {
      ctc.addColumn(createTimestampColumn(getDatasource().getSettings().getCreatedTimestampColumnName()));
    }
    if(getDatasource().getSettings().isUpdatedTimestampColumnNameProvided()) {
      ctc.addColumn(createTimestampColumn(getDatasource().getSettings().getUpdatedTimestampColumnName()));
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

  //
  // Inner Classes
  //

  class JdbcVariableEntityProvider extends AbstractVariableEntityProvider implements Initialisable {

    private Set<VariableEntity> entities = new LinkedHashSet<VariableEntity>();

    public JdbcVariableEntityProvider() {
      super(settings.getEntityType());
    }

    @SuppressWarnings("unchecked")
    public void initialise() {
      entities = new LinkedHashSet<VariableEntity>();

      // MAGMA-100
      if(escapedSqlTableName == null) {
        escapedSqlTableName = getDatasource().escapeSqlTableName(getSqlName());
      }

      // Build the SQL query.
      StringBuilder sql = new StringBuilder();

      // ...select entity identifier columns
      sql.append("SELECT ");
      List<String> entityIdentifierColumns = getSettings().getEntityIdentifierColumns();
      for(int i = 0; i < entityIdentifierColumns.size(); i++) {
        sql.append(entityIdentifierColumns.get(i));
        if(i < entityIdentifierColumns.size() - 1) {
          sql.append(", ");
        }
      }

      // ...from table
      sql.append(" FROM ");
      sql.append(escapedSqlTableName);

      // Execute the query.
      List<VariableEntity> results = getDatasource().getJdbcTemplate().query(sql.toString(), new RowMapper() {
        public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
          return new VariableEntityBean(JdbcValueTable.this.getEntityType(), buildEntityIdentifier(rs));
        }
      });

      entities.addAll(results);
    }

    @Override
    public Set<VariableEntity> getVariableEntities() {
      return Collections.unmodifiableSet(entities);
    }

    private String buildEntityIdentifier(ResultSet rs) throws SQLException {
      StringBuilder entityIdentifier = new StringBuilder();

      for(int i = 1; i <= getSettings().getEntityIdentifierColumns().size(); i++) {
        if(i > 1) {
          entityIdentifier.append('-');
        }
        entityIdentifier.append(rs.getObject(i).toString());
      }

      return entityIdentifier.toString();
    }

  }

  static class JdbcVariableValueSource implements VariableValueSource {
    //
    // Instance Variables
    //

    private Variable variable;

    //
    // Constructors
    //

    JdbcVariableValueSource(String entityType, Column column) {
      this.variable = Variable.Builder.newVariable(column.getName(), SqlTypes.valueTypeFor(column.getDataType()), entityType).build();
    }

    JdbcVariableValueSource(String entityType, ColumnConfig columnConfig) {
      this.variable = Variable.Builder.newVariable(columnConfig.getName(), SqlTypes.valueTypeFor(columnConfig.getType()), entityType).build();
    }

    JdbcVariableValueSource(Variable variable) {
      this.variable = variable;
    }

    //
    // VariableValueSource Methods
    //

    @Override
    public Variable getVariable() {
      return variable;
    }

    @Override
    public Value getValue(ValueSet valueSet) {
      JdbcValueSet jdbcValueSet = (JdbcValueSet) valueSet;
      return jdbcValueSet.getValue(variable);
    }

    @Override
    public ValueType getValueType() {
      return variable.getValueType();
    }
  }

  @Override
  public Timestamps getTimestamps(ValueSet valueSet) {
    return new JdbcTimestamps(valueSet);
  }

}
