/**
 * 
 */
package org.obiba.magma.datasource.hibernate;

import java.io.IOException;
import java.security.InvalidParameterException;
import java.util.Map;

import org.hibernate.FlushMode;
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

import com.google.common.base.Function;
import com.google.common.collect.Maps;

class HibernateValueTableWriter implements ValueTableWriter {

  private final HibernateValueTable valueTable;

  private final HibernateValueTableTransaction transaction;

  private final VariableConverter variableConverter = new VariableConverter();

  private final SessionFactory sessionFactory;

  private final HibernateVariableValueSourceFactory valueSourceFactory;

  private final FlushMode initialFlushMode;

  private boolean errorOccurred = false;

  HibernateValueTableWriter(HibernateValueTableTransaction transaction) {
    super();
    if(transaction == null) throw new IllegalArgumentException("transaction cannot be null");
    this.transaction = transaction;
    this.valueTable = transaction.getValueTable();

    this.sessionFactory = valueTable.getDatasource().getSessionFactory();
    this.valueSourceFactory = new HibernateVariableValueSourceFactory(valueTable);
    this.initialFlushMode = this.sessionFactory.getCurrentSession().getFlushMode();

    this.sessionFactory.getCurrentSession().setFlushMode(FlushMode.MANUAL);

    // Synchronize with the transaction. This allows us to add new variables and entities only if the transaction
    // succeeds.
    new HibernateDatasourceSynchronization(valueTable.getDatasource()) {

      @Override
      public void beforeCompletion() {
        sessionFactory.getCurrentSession().setFlushMode(initialFlushMode);
      }

    };
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

    private HibernateVariableWriter() {
    }

    @Override
    public void writeVariable(Variable variable) {
      if(variable == null) throw new IllegalArgumentException("variable cannot be null");
      if(!valueTable.isForEntityType(variable.getEntityType())) {
        throw new InvalidParameterException("Wrong entity type for variable '" + variable.getName() + "': " + valueTable.getEntityType() + " expected, " + variable.getEntityType() + " received.");
      }

      // add or update variable
      errorOccurred = true;
      HibernateMarshallingContext context = valueTable.createContext();
      VariableState state = variableConverter.marshal(variable, context);
      transaction.addSource(valueSourceFactory.createSource(state));
      errorOccurred = false;
    }

    @Override
    public void close() throws IOException {
      if(errorOccurred == false) {
        // TODO: We mustn't flush the session if an exception has occurred within Hibernate.
        sessionFactory.getCurrentSession().flush();
      }
      sessionFactory.getCurrentSession().clear();
    }
  }

  private class HibernateValueSetWriter implements ValueSetWriter {

    private final ValueSetState valueSetState;

    private Map<String, ValueSetValue> values;

    public HibernateValueSetWriter(VariableEntity entity) {
      if(entity == null) throw new IllegalArgumentException("entity cannot be null");
      // find entity or create it
      VariableEntityState variableEntityState = VariableEntityConverter.getInstance().marshal(entity, valueTable.createContext());

      AssociationCriteria criteria = AssociationCriteria.create(ValueSetState.class, sessionFactory.getCurrentSession()).add("valueTable", Operation.eq, valueTable.getValueTableState()).add("variableEntity", Operation.eq, variableEntityState);
      ValueSetState state = (ValueSetState) criteria.getCriteria().uniqueResult();
      if(state == null) {
        state = new ValueSetState(valueTable.getValueTableState(), variableEntityState);
        transaction.addEntity(entity);
      }
      valueSetState = state;

      values = Maps.newHashMap(Maps.uniqueIndex(state.getValues(), new Function<ValueSetValue, String>() {
        @Override
        public String apply(ValueSetValue from) {
          return from.getVariable().getName();
        }
      }));
    }

    @Override
    public void writeValue(Variable variable, Value value) {
      if(variable == null) throw new IllegalArgumentException("variable cannot be null");
      if(value == null) throw new IllegalArgumentException("value cannot be null");

      ValueSetValue vsv = values.get(variable.getName());
      if(vsv != null) {
        if(value.isNull()) {
          valueSetState.getValues().remove(vsv);
          values.remove(variable.getName());
        } else {
          vsv.setValue(value);
        }
      } else {
        if(value.isNull() == false) {
          VariableState variableState = variableConverter.getStateForVariable(variable, valueTable.createContext());
          if(variableState == null) {
            throw new NoSuchVariableException(valueTable.getName(), variable.getName());
          }
          vsv = new ValueSetValue(variableState, valueSetState);
          vsv.setValue(value);
          valueSetState.getValues().add(vsv);
          values.put(variable.getName(), vsv);
        }
      }
    }

    @Override
    public void close() throws IOException {
      if(errorOccurred == false) {
        sessionFactory.getCurrentSession().save(valueSetState);
        // TODO: We mustn't flush the session if an exception has occurred within Hibernate.
        sessionFactory.getCurrentSession().flush();
        // Empty the Session so we don't fill it up
        sessionFactory.getCurrentSession().evict(valueSetState);
      }
    }

  }
}