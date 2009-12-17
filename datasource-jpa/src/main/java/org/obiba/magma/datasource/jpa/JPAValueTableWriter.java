/**
 * 
 */
package org.obiba.magma.datasource.jpa;

import java.io.IOException;
import java.security.InvalidParameterException;

import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;
import org.obiba.core.service.impl.hibernate.AssociationCriteria;
import org.obiba.core.service.impl.hibernate.AssociationCriteria.Operation;
import org.obiba.magma.NoSuchVariableException;
import org.obiba.magma.Value;
import org.obiba.magma.ValueTableWriter;
import org.obiba.magma.Variable;
import org.obiba.magma.VariableEntity;
import org.obiba.magma.datasource.jpa.converter.JPAMarshallingContext;
import org.obiba.magma.datasource.jpa.converter.VariableConverter;
import org.obiba.magma.datasource.jpa.domain.ValueSetState;
import org.obiba.magma.datasource.jpa.domain.ValueSetValue;
import org.obiba.magma.datasource.jpa.domain.VariableEntityState;
import org.obiba.magma.datasource.jpa.domain.VariableState;

public class JPAValueTableWriter implements ValueTableWriter {

  private final JPAValueTable valueTable;

  private final SessionFactory sessionFactory;

  JPAValueTableWriter(JPAValueTable valueTable) {
    super();
    this.valueTable = valueTable;
    this.sessionFactory = valueTable.getDatasource().getSessionFactory();
  }

  @Override
  public ValueSetWriter writeValueSet(VariableEntity entity) {
    return new JPAValueSetWriter(entity);
  }

  @Override
  public VariableWriter writeVariables() {
    return new JPAVariableWriter();
  }

  @Override
  public void close() throws IOException {
  }

  private class JPAVariableWriter implements VariableWriter {

    @Override
    public void writeVariable(Variable variable) {
      if(!valueTable.isForEntityType(variable.getEntityType())) {
        throw new InvalidParameterException("Wrong entity type for variable '" + variable.getName() + "': " + valueTable.getEntityType() + " expected, " + variable.getEntityType() + " received.");
      }

      // add or update variable
      VariableConverter.getInstance().marshal(variable, JPAMarshallingContext.create(sessionFactory, valueTable.getValueTableState()));
    }

    @Override
    public void close() throws IOException {
    }
  }

  private class JPAValueSetWriter implements ValueSetWriter {

    private ValueSetState valueSetState;

    public JPAValueSetWriter(VariableEntity entity) {
      // find entity or create it
      VariableEntityState variableEntity = (VariableEntityState) sessionFactory.getCurrentSession().createCriteria(VariableEntityState.class).add(Restrictions.eq("identifier", entity.getIdentifier())).add(Restrictions.eq("type", entity.getType())).uniqueResult();
      if(variableEntity == null) {
        variableEntity = new VariableEntityState(entity.getIdentifier(), entity.getType());
        sessionFactory.getCurrentSession().save(variableEntity);
      }

      AssociationCriteria criteria = AssociationCriteria.create(ValueSetState.class, sessionFactory.getCurrentSession()).add("valueTable", Operation.eq, valueTable.getValueTableState()).add("variableEntity", Operation.eq, variableEntity);
      valueSetState = (ValueSetState) criteria.getCriteria().uniqueResult();
      if(valueSetState == null) {
        valueSetState = new ValueSetState(valueTable.getValueTableState(), variableEntity);
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