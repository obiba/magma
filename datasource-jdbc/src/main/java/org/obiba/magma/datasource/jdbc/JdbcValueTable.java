package org.obiba.magma.datasource.jdbc;

import java.util.Collections;
import java.util.Set;

import liquibase.database.structure.Column;
import liquibase.database.structure.Table;

import org.obiba.magma.NoSuchValueSetException;
import org.obiba.magma.Value;
import org.obiba.magma.ValueSet;
import org.obiba.magma.ValueType;
import org.obiba.magma.Variable;
import org.obiba.magma.VariableEntity;
import org.obiba.magma.VariableValueSource;
import org.obiba.magma.support.AbstractValueTable;
import org.obiba.magma.support.AbstractVariableEntityProvider;
import org.obiba.magma.support.ValueSetBean;

public class JdbcValueTable extends AbstractValueTable {

  private String entityType;

  private Table table;

  public JdbcValueTable(JdbcDatasource datasource, Table table, String entityType) {
    super(datasource, table.getName());
    this.table = table;
    this.entityType = entityType;
  }

  @Override
  public void initialise() {
    super.initialise();
    for(Column column : table.getColumns()) {
      addVariableValueSource(new ColumnVariableValueSource(column));
    }
    super.setVariableEntityProvider(new AbstractVariableEntityProvider(entityType) {

      @Override
      public Set<VariableEntity> getVariableEntities() {
        return Collections.emptySet();
      }
    });
  }

  @Override
  public String getEntityType() {
    return entityType;
  }

  @Override
  public JdbcDatasource getDatasource() {
    return (JdbcDatasource) super.getDatasource();
  }

  @Override
  public ValueSet getValueSet(VariableEntity entity) throws NoSuchValueSetException {
    return new ValueSetBean(this, entity);
  }

  String getSqlName() {
    return getSqlName(getName());
  }

  String getSqlName(Variable variable) {
    return getSqlName(variable.getName());
  }

  String getSqlName(String name) {
    return name.replace('-', '_').replace(' ', '_').replace('.', '_');
  }

  public class JPAVariableEntityProvider extends AbstractVariableEntityProvider {

    private Set<VariableEntity> entities;

    public JPAVariableEntityProvider() {
      super(entityType);
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
      this.variable = Variable.Builder.newVariable(column.getName(), SqlTypes.valueTypeFor(column.getDataType()), entityType).build();
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
