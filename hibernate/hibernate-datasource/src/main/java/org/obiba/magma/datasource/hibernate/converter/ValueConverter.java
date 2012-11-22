package org.obiba.magma.datasource.hibernate.converter;

import org.obiba.core.service.impl.hibernate.AssociationCriteria;
import org.obiba.core.service.impl.hibernate.AssociationCriteria.Operation;
import org.obiba.magma.Value;
import org.obiba.magma.datasource.hibernate.domain.ValueSetState;
import org.obiba.magma.datasource.hibernate.domain.ValueSetValue;
import org.obiba.magma.datasource.hibernate.domain.VariableState;

public class ValueConverter implements HibernateConverter<ValueSetValue, Value> {
  //
  // HibernateConverter Methods
  //

  @Override
  public ValueSetValue marshal(Value magmaObject, HibernateMarshallingContext context) {
    ValueSetState valueSetState = context.getValueSet();
    VariableState variableState = context.getVariable();

    AssociationCriteria criteria = AssociationCriteria
        .create(ValueSetValue.class, context.getSessionFactory().getCurrentSession()) //
        .add("valueSet", Operation.eq, context.getValueSet()) //
        .add("variable", Operation.eq, context.getVariable());
    ValueSetValue valueSetValue = (ValueSetValue) criteria.getCriteria().uniqueResult();
    if(valueSetValue == null) {
      // Only persist non-null values
      if(magmaObject.isNull() == false) {
        valueSetValue = new ValueSetValue(variableState, valueSetState);
        valueSetValue.setValue(magmaObject);
        context.getSessionFactory().getCurrentSession().save(valueSetValue);
      }
    } else if(magmaObject.isNull()) {
      // Delete existing value since we are writing a null
      context.getSessionFactory().getCurrentSession().delete(valueSetValue);
    } else {
      // Hibernate will persist this modification upon flushing the session. No need to issue a save or update here.
      valueSetValue.setValue(magmaObject);
    }

    return valueSetValue;
  }

  @Override
  public Value unmarshal(ValueSetValue jpaObject, HibernateMarshallingContext context) {
    return jpaObject.getValue();
  }

  //
  // Methods
  //

  public static ValueConverter getInstance() {
    return new ValueConverter();
  }

}
