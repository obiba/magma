package org.obiba.magma.datasource.hibernate.converter;

import org.hibernate.SessionFactory;
import org.obiba.magma.AttributeAwareBuilder;
import org.obiba.magma.datasource.hibernate.domain.DatasourceState;
import org.obiba.magma.datasource.hibernate.domain.ValueSetState;
import org.obiba.magma.datasource.hibernate.domain.ValueTableState;
import org.obiba.magma.datasource.hibernate.domain.VariableState;
import org.obiba.magma.datasource.hibernate.domain.attribute.AbstractAttributeAwareEntity;

public class HibernateMarshallingContext {

  private DatasourceState datasourceState;

  private ValueTableState valueTable;

  private ValueSetState valueSet;

  private VariableState variable;

  private AttributeAwareBuilder<?> attributeAwareBuilder;

  private AbstractAttributeAwareEntity attributeAwareEntity;

  private SessionFactory sessionFactory;

  public static HibernateMarshallingContext create(SessionFactory sessionFactory, DatasourceState datasourceState) {
    return create(sessionFactory, datasourceState, null);
  }

  public static HibernateMarshallingContext create(SessionFactory sessionFactory, DatasourceState datasourceState, ValueTableState valueTable) {
    if(sessionFactory == null) throw new IllegalArgumentException("sessionFactory cannot be null");
    if(datasourceState == null) throw new IllegalArgumentException("datasourceState cannot be null");

    HibernateMarshallingContext context = new HibernateMarshallingContext();
    context.sessionFactory = sessionFactory;
    context.datasourceState = datasourceState;
    context.valueTable = valueTable;
    return context;
  }

  public DatasourceState getDatasourceState() {
    return datasourceState;
  }

  public ValueTableState getValueTable() {
    return valueTable;
  }

  public SessionFactory getSessionFactory() {
    return sessionFactory;
  }

  public ValueSetState getValueSet() {
    return valueSet;
  }

  public void setValueSet(ValueSetState valueSet) {
    this.valueSet = valueSet;
  }

  public VariableState getVariable() {
    return variable;
  }

  public void setVariable(VariableState variable) {
    this.variable = variable;
    this.attributeAwareEntity = variable;
  }

  public void setAttributeAwareEntity(AbstractAttributeAwareEntity adaptable) {
    this.attributeAwareEntity = adaptable;
  }

  public AbstractAttributeAwareEntity getAttributeAwareEntity() {
    return attributeAwareEntity;
  }

  public AttributeAwareBuilder<?> getAttributeAwareBuilder() {
    return attributeAwareBuilder;
  }

  public void setAttributeAwareBuilder(AttributeAwareBuilder<?> attributeAwareBuilder) {
    this.attributeAwareBuilder = attributeAwareBuilder;
  }

}
