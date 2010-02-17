package org.obiba.magma.datasource.jdbc;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import liquibase.change.ColumnConfig;
import liquibase.change.ConstraintsConfig;
import liquibase.change.CreateTableChange;
import liquibase.database.Database;
import liquibase.database.sql.visitor.SqlVisitor;
import liquibase.database.structure.Column;
import liquibase.database.structure.Table;

import org.obiba.magma.Initialisable;
import org.obiba.magma.MagmaRuntimeException;
import org.obiba.magma.NoSuchValueSetException;
import org.obiba.magma.Value;
import org.obiba.magma.ValueSet;
import org.obiba.magma.ValueType;
import org.obiba.magma.Variable;
import org.obiba.magma.VariableEntity;
import org.obiba.magma.VariableValueSource;
import org.obiba.magma.datasource.jdbc.support.NameConverter;
import org.obiba.magma.support.AbstractValueTable;
import org.obiba.magma.support.AbstractVariableEntityProvider;
import org.obiba.magma.support.VariableEntityBean;
import org.springframework.jdbc.core.RowMapper;

public class JdbcValueTable extends AbstractValueTable {
  //
  // Instance Variables
  //

  private JdbcValueTableSettings settings;

  private Table table;

  //
  // Constructors
  //

  public JdbcValueTable(JdbcDatasource datasource, JdbcValueTableSettings settings) {
    super(datasource, settings.getMagmaTableName());
    this.settings = settings;

    if(getDatasource().getDatabaseSnapshot().getTable(settings.getSqlTableName()) == null) {
      createSqlTable(settings.getSqlTableName());
      getDatasource().databaseChanged();
    }
    this.table = getDatasource().getDatabaseSnapshot().getTable(settings.getSqlTableName());
  }

  public JdbcValueTable(JdbcDatasource datasource, Table table, String entityType) {
    this(datasource, new JdbcValueTableSettings(table.getName(), NameConverter.toMagmaName(table.getName()), entityType, getEntityIdentifierColumns(table)));
  }

  //
  // AbstractValueTable Methods
  //

  @Override
  public void initialise() {
    super.initialise();

    initialiseVariableValueSources();

    JdbcVariableEntityProvider variableEntityProvider = new JdbcVariableEntityProvider();
    variableEntityProvider.initialise();
    super.setVariableEntityProvider(variableEntityProvider);

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
    return getSqlName(getName());
  }

  String getSqlName(Variable variable) {
    return getSqlName(variable.getName());
  }

  String getSqlName(String name) {
    return NameConverter.toSqlName(name);
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
    if(getDatasource().getSettings().useMetadataTables()) {
      if(getDatasource().getDatabaseSnapshot().getTable("variables") == null) {
        throw new MagmaRuntimeException("metadata tables not found");
      }

      StringBuilder sql = new StringBuilder();
      sql.append("SELECT *");
      sql.append(" FROM variables ");
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

  private Variable buildVariableFromResultSet(ResultSet rs) throws SQLException {
    String variableName = rs.getString("name");
    ValueType valueType = ValueType.Factory.forName(rs.getString("value_type"));
    final Variable.Builder builder = Variable.Builder.newVariable(variableName, valueType, getEntityType());

    // Add attributes, if any.
    StringBuilder sql = new StringBuilder();
    sql.append("SELECT *");
    sql.append(" FROM variable_attributes ");
    sql.append(" WHERE value_table = ? AND variable_name = ?");

    getDatasource().getJdbcTemplate().query(sql.toString(), new Object[] { getSqlName(), variableName }, new RowMapper() {
      public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
        String attributeName = rs.getString("attribute_name");
        String attributeValue = rs.getString("attribute_value");
        builder.addAttribute(attributeName, attributeValue);
        return attributeName;
      }
    });

    return builder.build();
  }

  private void createSqlTable(String sqlTableName) {
    Database database = getDatasource().getDatabase();

    CreateTableChange ctc = new CreateTableChange();
    ctc.setTableName(sqlTableName);

    // Create the table initially with just one column -- entity_id -- the primary key.
    ColumnConfig column = new ColumnConfig();
    column.setName("entity_id");
    column.setType("VARCHAR");
    ConstraintsConfig constraints = new ConstraintsConfig();
    constraints.setPrimaryKey(true);
    column.setConstraints(constraints);

    ctc.addColumn(column);

    try {
      List<SqlVisitor> sqlVisitors = Collections.emptyList();
      ctc.executeStatements(database, sqlVisitors);
      database.commit();
    } catch(Exception ex) {
      throw new MagmaRuntimeException("could not create sql table: " + ex.getMessage());
    }
  }

  //
  // Inner Classes
  //

  class JdbcVariableEntityProvider extends AbstractVariableEntityProvider implements Initialisable {

    private Set<VariableEntity> entities;

    public JdbcVariableEntityProvider() {
      super(settings.getEntityType());
    }

    @SuppressWarnings("unchecked")
    public void initialise() {
      entities = new LinkedHashSet<VariableEntity>();

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
      sql.append(getSqlName());

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

}
