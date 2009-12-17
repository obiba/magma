package org.obiba.magma.datasource.jpa.converter;

import org.hibernate.SessionFactory;
import org.obiba.magma.AttributeAwareBuilder;
import org.obiba.magma.datasource.jpa.domain.ValueTableState;
import org.obiba.magma.datasource.jpa.domain.VariableState;
import org.obiba.magma.datasource.jpa.domain.adaptable.AbstractAdaptableEntity;

public class JPAMarshallingContext {

  private ValueTableState valueTable;

  private VariableState variable;

  private AttributeAwareBuilder<?> attributeAwareBuilder;

  private AbstractAdaptableEntity adaptable;

  private SessionFactory sessionFactory;

  public static JPAMarshallingContext create(SessionFactory sessionFactory) {
    return create(sessionFactory, null);
  }

  public static JPAMarshallingContext create(SessionFactory sessionFactory, ValueTableState valueTable) {
    JPAMarshallingContext context = new JPAMarshallingContext();
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

  public VariableState getVariable() {
    return variable;
  }

  public void setVariable(VariableState variable) {
    this.variable = variable;
    this.adaptable = variable;
  }

  public void setAdaptable(AbstractAdaptableEntity adaptable) {
    this.adaptable = adaptable;
  }

  public AbstractAdaptableEntity getAdaptable() {
    return adaptable;
  }

  public AttributeAwareBuilder<?> getAttributeAwareBuilder() {
    return attributeAwareBuilder;
  }

  public void setAttributeAwareBuilder(AttributeAwareBuilder<?> attributeAwareBuilder) {
    this.attributeAwareBuilder = attributeAwareBuilder;
  }

}
