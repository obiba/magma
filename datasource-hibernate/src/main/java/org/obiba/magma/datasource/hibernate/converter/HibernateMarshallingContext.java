package org.obiba.magma.datasource.hibernate.converter;

import org.hibernate.SessionFactory;
import org.obiba.magma.AttributeAwareBuilder;
import org.obiba.magma.datasource.hibernate.domain.ValueSetState;
import org.obiba.magma.datasource.hibernate.domain.ValueTableState;
import org.obiba.magma.datasource.hibernate.domain.VariableState;
import org.obiba.magma.datasource.hibernate.domain.attribute.AbstractAttributeAwareEntity;

public class HibernateMarshallingContext {

  private ValueTableState valueTable;

  private ValueSetState valueSet;

  private VariableState variable;

  private AttributeAwareBuilder<?> attributeAwareBuilder;

  private AbstractAttributeAwareEntity attributeAwareEntity;

  private SessionFactory sessionFactory;

  public static HibernateMarshallingContext create(SessionFactory sessionFactory) {
    return create(sessionFactory, null);
  }

  public static HibernateMarshallingContext create(SessionFactory sessionFactory, ValueTableState valueTable) {
    HibernateMarshallingContext context = new HibernateMarshallingContext();
    context.sessionFactory = sessionFactory;
    context.valueTable = valueTable;
    return context;
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
