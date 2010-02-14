package org.obiba.magma.datasource.jdbc;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import liquibase.database.structure.Column;
import liquibase.database.structure.Table;

import org.obiba.magma.Initialisable;
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
import org.obiba.magma.support.ValueSetBean;
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
    for(Column column : table.getColumns()) {
      addVariableValueSource(new ColumnVariableValueSource(column));
    }
    super.setVariableEntityProvider(new JdbcVariableEntityProvider());
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
    return new ValueSetBean(this, entity);
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

  //
  // Inner Classes
  //

  public class JdbcVariableEntityProvider extends AbstractVariableEntityProvider implements Initialisable {

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
          return new VariableEntityBean(JdbcValueTable.this.getEntityType(), rs.getString(0));
        }
      });

      entities.addAll(results);
    }

    @Override
    public Set<VariableEntity> getVariableEntities() {
      return Collections.unmodifiableSet(entities);
    }

  }

  public class ColumnVariableValueSource implements VariableValueSource {

    private Column column;

    private Variable variable;

    private ColumnVariableValueSource(Column column) {
      this.column = column;
      this.variable = Variable.Builder.newVariable(column.getName(), SqlTypes.valueTypeFor(column.getDataType()), settings.getEntityType()).build();
    }

    @Override
    public Variable getVariable() {
      return variable;
    }

    @Override
    public Value getValue(ValueSet valueSet) {
      throw new UnsupportedOperationException();
    }

    @Override
    public ValueType getValueType() {
      return variable.getValueType();
    }

  }

}
