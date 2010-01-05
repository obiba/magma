/**
 * 
 */
package org.obiba.magma.datasource.hibernate;

import java.io.IOException;
import java.security.InvalidParameterException;

import org.hibernate.SessionFactory;
import org.obiba.core.service.impl.hibernate.AssociationCriteria;
import org.obiba.core.service.impl.hibernate.AssociationCriteria.Operation;
import org.obiba.magma.NoSuchVariableException;
import org.obiba.magma.Value;
import org.obiba.magma.ValueTableWriter;
import org.obiba.magma.Variable;
import org.obiba.magma.VariableEntity;
import org.obiba.magma.datasource.hibernate.converter.HibernateMarshallingContext;
import org.obiba.magma.datasource.hibernate.converter.VariableConverter;
import org.obiba.magma.datasource.hibernate.converter.VariableEntityConverter;
import org.obiba.magma.datasource.hibernate.domain.ValueSetState;
import org.obiba.magma.datasource.hibernate.domain.ValueSetValue;
import org.obiba.magma.datasource.hibernate.domain.VariableEntityState;
import org.obiba.magma.datasource.hibernate.domain.VariableState;

public class HibernateValueTableWriter implements ValueTableWriter {

  private final HibernateValueTable valueTable;

  private final SessionFactory sessionFactory;

  HibernateValueTableWriter(HibernateValueTable valueTable) {
    super();
    this.valueTable = valueTable;
    this.sessionFactory = valueTable.getDatasource().getSessionFactory();
  }

  @Override
  public ValueSetWriter writeValueSet(VariableEntity entity) {
    return new HibernateValueSetWriter(entity);
  }

  @Override
  public VariableWriter writeVariables() {
    return new HibernateVariableWriter();
  }

  @Override
  public void close() throws IOException {
  }

  private class HibernateVariableWriter implements VariableWriter {

    @Override
    public void writeVariable(Variable variable) {
      if(!valueTable.isForEntityType(variable.getEntityType())) {
        throw new InvalidParameterException("Wrong entity type for variable '" + variable.getName() + "': " + valueTable.getEntityType() + " expected, " + variable.getEntityType() + " received.");
      }

      // add or update variable
      VariableConverter.getInstance().marshal(variable, HibernateMarshallingContext.create(sessionFactory, valueTable.getValueTableState()));
    }

    @Override
    public void close() throws IOException {
    }
  }

  private class HibernateValueSetWriter implements ValueSetWriter {

    private ValueSetState valueSetState;

    public HibernateValueSetWriter(VariableEntity entity) {
      // find entity or create it
      VariableEntityState variableEntityState = VariableEntityConverter.getInstance().marshal(entity, HibernateMarshallingContext.create(sessionFactory, valueTable.getValueTableState()));

      AssociationCriteria criteria = AssociationCriteria.create(ValueSetState.class, sessionFactory.getCurrentSession()).add("valueTable", Operation.eq, valueTable.getValueTableState()).add("variableEntity", Operation.eq, variableEntityState);
      valueSetState = (ValueSetState) criteria.getCriteria().uniqueResult();
      if(valueSetState == null) {
        valueSetState = new ValueSetState(valueTable.getValueTableState(), variableEntityState);
        sessionFactory.getCurrentSession().save(valueSetState);
      }
    }

    @Override
    public void writeValue(Variable variable, Value value) {
      AssociationCriteria criteria = AssociationCriteria.create(VariableState.class, sessionFactory.getCurrentSession()).add("valueTable", Operation.eq, valueSetState.getValueTable()).add("name", Operation.eq, variable.getName());
      VariableState variableState = (VariableState) criteria.getCriteria().uniqueResult();

      if(variableState == null) {
        throw new NoSuchVariableException(valueTable.getName(), variable.getName());
      } else {
        // TODO use converters

        // find value for this value set or create it
        criteria = AssociationCriteria.create(ValueSetValue.class, sessionFactory.getCurrentSession()).add("valueSet", Operation.eq, valueSetState).add("variable", Operation.eq, variableState);
        ValueSetValue valueSetValue = (ValueSetValue) criteria.getCriteria().uniqueResult();
        if(valueSetValue == null) {
          // Only persist non-null values
          if(value.isNull() == false) {
            valueSetValue = new ValueSetValue(variableState, valueSetState);
            valueSetValue.setValue(value);
            sessionFactory.getCurrentSession().save(valueSetValue);
          }
        } else if(valueSetValue != null && value.isNull()) {
          // Delete existing value since we are writing a null
          sessionFactory.getCurrentSession().delete(valueSetValue);
        } else {
          // Hibernate will persist this modification upon flushing the session. No need to issue a save or update here.
          valueSetValue.setValue(value);
        }
      }
    }

    @Override
    public void close() throws IOException {

    }

  }
}