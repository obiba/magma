package org.obiba.magma.datasource.jdbc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
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
import org.obiba.magma.datasource.jdbc.support.NameConverter;
import org.obiba.magma.support.AbstractValueTable;
import org.obiba.magma.support.AbstractVariableEntityProvider;
import org.obiba.magma.support.ValueSetBean;

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
    super.setVariableEntityProvider(new AbstractVariableEntityProvider(settings.getEntityType()) {

      @Override
      public Set<VariableEntity> getVariableEntities() {
        return Collections.emptySet();
      }
    });
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

  String getSqlName() {
    return getSqlName(getName());
  }

  String getSqlName(Variable variable) {
    return getSqlName(variable.getName());
  }

  String getSqlName(String name) {
    return name.replace('-', '_').replace(' ', '_').replace('.', '_');
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

  public class JPAVariableEntityProvider extends AbstractVariableEntityProvider {

    private Set<VariableEntity> entities;

    public JPAVariableEntityProvider() {
      super(settings.getEntityType());
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
