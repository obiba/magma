package org.obiba.magma.datasource.hibernate.converter;

import org.hibernate.classic.Session;
import org.obiba.core.service.impl.hibernate.AssociationCriteria;
import org.obiba.core.service.impl.hibernate.AssociationCriteria.Operation;
import org.obiba.magma.Value;
import org.obiba.magma.datasource.hibernate.domain.ValueSetState;
import org.obiba.magma.datasource.hibernate.domain.ValueSetValue;
import org.obiba.magma.datasource.hibernate.domain.VariableState;

public class ValueConverter implements HibernateConverter<ValueSetValue, Value> {

  public static ValueConverter getInstance() {
    return new ValueConverter();
  }

  private ValueConverter() {
  }

  @Override
  public ValueSetValue marshal(Value value, HibernateMarshallingContext context) {
    ValueSetState valueSetState = context.getValueSet();
    VariableState variableState = context.getVariable();

    Session currentSession = context.getSessionFactory().getCurrentSession();
    ValueSetValue valueSetValue = (ValueSetValue) AssociationCriteria.create(ValueSetValue.class, currentSession) //
        .add("valueSet", Operation.eq, context.getValueSet()) //
        .add("variable", Operation.eq, context.getVariable()).getCriteria().uniqueResult();
    if(valueSetValue == null) {
      // Only persist non-null values
      if(!value.isNull()) {
        valueSetValue = new ValueSetValue(variableState, valueSetState);
        valueSetValue.setValue(value);
        currentSession.save(valueSetValue);
      }
    } else if(value.isNull()) {
      // Delete existing value since we are writing a null
      currentSession.delete(valueSetValue);
    } else {
      // Hibernate will persist this modification upon flushing the session. No need to issue a save or update here.
      valueSetValue.setValue(value);
    }

    return valueSetValue;
  }

  @Override
  public Value unmarshal(ValueSetValue valueSetValue, HibernateMarshallingContext context) {
    return valueSetValue.getValue();
  }

}
