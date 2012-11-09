/**
 * 
 */
package org.obiba.magma.datasource.hibernate;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.hibernate.FlushMode;
import org.hibernate.LockMode;
import org.hibernate.Session;
import org.json.JSONException;
import org.json.JSONObject;
import org.obiba.core.service.impl.hibernate.AssociationCriteria;
import org.obiba.core.service.impl.hibernate.AssociationCriteria.Operation;
import org.obiba.magma.MagmaRuntimeException;
import org.obiba.magma.NoSuchVariableException;
import org.obiba.magma.Value;
import org.obiba.magma.ValueSequence;
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
import org.obiba.magma.support.BinaryValueFileHelper;
import org.obiba.magma.type.BinaryType;
import org.obiba.magma.type.TextType;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

class HibernateValueTableWriter implements ValueTableWriter {

  private final HibernateValueTable valueTable;

  private final HibernateValueTableTransaction transaction;

  private final VariableConverter variableConverter = new VariableConverter();

  private final Session session;

  private final HibernateVariableValueSourceFactory valueSourceFactory;

  private boolean errorOccurred = false;

  HibernateValueTableWriter(HibernateValueTableTransaction transaction) {
    super();
    if(transaction == null) throw new IllegalArgumentException("transaction cannot be null");
    this.transaction = transaction;
    this.valueTable = transaction.getValueTable();

    this.session = valueTable.getDatasource().getSessionFactory().getCurrentSession();
    this.valueSourceFactory = new HibernateVariableValueSourceFactory(valueTable);
    if(this.session.getFlushMode() != FlushMode.MANUAL) {
      this.session.setFlushMode(FlushMode.MANUAL);
    }
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

    private HibernateMarshallingContext context = valueTable.createContext();

    private HibernateVariableWriter() {
    }

    @Override
    public void writeVariable(Variable variable) {
      if(variable == null) throw new IllegalArgumentException("variable cannot be null");
      if(!valueTable.isForEntityType(variable.getEntityType())) {
        throw new IllegalArgumentException("Wrong entity type for variable '" + variable.getName() + "': " + valueTable.getEntityType() + " expected, " + variable.getEntityType() + " received.");
      }

      // add or update variable
      errorOccurred = true;
      VariableState state = variableConverter.marshal(variable, context);
      transaction.addSource(valueSourceFactory.createSource(state));
      errorOccurred = false;
    }

    @Override
    public void close() throws IOException {
      if(errorOccurred == false) {
        session.flush();
        session.clear();
      }
    }
  }

  private class HibernateValueSetWriter implements ValueSetWriter {

    private final VariableEntityConverter entityConverter = new VariableEntityConverter();

    private final ValueSetState valueSetState;

    private final VariableEntity entity;

    private final boolean isNewValueSet;

    private final Map<String, ValueSetValue> values;

    public HibernateValueSetWriter(VariableEntity entity) {
      if(entity == null) throw new IllegalArgumentException("entity cannot be null");
      this.entity = entity;
      // find entity or create it
      VariableEntityState variableEntityState = entityConverter.marshal(entity, valueTable.createContext());

      AssociationCriteria criteria = AssociationCriteria.create(ValueSetState.class, session).add("valueTable", Operation.eq, valueTable.getValueTableState()).add("variableEntity", Operation.eq, variableEntityState);

      // Will update version timestamp if it exists
      ValueSetState state = (ValueSetState) criteria.getCriteria().setLockMode(LockMode.PESSIMISTIC_FORCE_INCREMENT).uniqueResult();
      if(state == null) {
        state = new ValueSetState(valueTable.getValueTableState(), variableEntityState);
        // Persists the ValueSet
        session.save(state);
        values = Maps.newHashMap();
        isNewValueSet = true;
      } else {
        values = Maps.newHashMap(state.getValueMap());
        isNewValueSet = false;
      }
      valueSetState = state;
    }

    @Override
    public void writeValue(Variable variable, Value value) {
      if(variable == null) throw new IllegalArgumentException("variable cannot be null");
      if(value == null) throw new IllegalArgumentException("value cannot be null");

      try {
        ValueSetValue vsv = values.get(variable.getName());
        if(vsv != null) {
          if(value.isNull()) {
            valueSetState.getValues().remove(vsv);
            values.remove(variable.getName());
            // TODO remove binary file
          } else {
            writeValue(vsv, variable, value);
          }
        } else {
          if(value.isNull() == false) {
            VariableState variableState = variableConverter.getStateForVariable(variable, valueTable.createContext());
            if(variableState == null) {
              throw new NoSuchVariableException(valueTable.getName(), variable.getName());
            }
            vsv = new ValueSetValue(variableState, valueSetState);
            writeValue(vsv, variable, value);
            valueSetState.getValues().add(vsv);
            values.put(variable.getName(), vsv);
          }
        }
      } catch(RuntimeException e) {
        errorOccurred = true;
        throw e;
      } catch(Error e) {
        errorOccurred = true;
        throw e;
      }
    }

    private void writeValue(ValueSetValue vsv, Variable variable, Value value) {
      if(value.getValueType().equals(BinaryType.get()) && valueTable.getDatasource().hasDatasourceRoot()) {
        vsv.setValue(writeBinaryValue(variable, value));
      } else {
        vsv.setValue(value);
      }
    }

    /**
     * Write the byte array in a file and return the value reference.
     * @param variable
     * @param value
     * @return
     */
    private Value writeBinaryValue(Variable variable, Value value) {
      Value path = BinaryValueFileHelper.writeValue(getTableRoot(), variable, entity, value);
      return path;
      // return toPropertiesValueRef(value, path);
    }

    /**
     * Add to path other properties of the byte array.
     * @param value
     * @param path
     * @return
     */
    private Value toPropertiesValueRef(Value value, Value path) {
      Value valueRef;
      if(value.isSequence()) {
        List<Value> valueRefs = Lists.newArrayList();
        ValueSequence valueSequence = value.asSequence();
        for(int i = 0; i < valueSequence.getValues().size(); i++) {
          valueRefs.add(getPropertiesValueRef(valueSequence.get(i), path.asSequence().get(i)));
        }
        valueRef = TextType.get().sequenceOf(valueRefs);
      } else {
        valueRef = getPropertiesValueRef(value, path);
      }
      return valueRef;
    }

    private Value getPropertiesValueRef(Value value, Value path) {
      if(value.isNull()) {
        // TODO remove a file
        return TextType.get().nullValue();
      }

      try {
        JSONObject properties = new JSONObject();
        byte[] array = (byte[]) value.getValue();
        properties.put("length", array.length);
        properties.put("path", path.toString());
        return TextType.get().valueOf(properties.toString());
      } catch(JSONException e) {
        throw new MagmaRuntimeException(e);
      }
    }

    private File getTableRoot() {
      File tableRoot = valueTable.getTableRoot();
      if(tableRoot.exists() == false) {
        if(tableRoot.mkdirs() == false) {
          throw new MagmaRuntimeException("Unable to create directory: " + tableRoot.getAbsolutePath());
        }
      }
      return tableRoot;
    }

    @Override
    public void close() throws IOException {
      if(errorOccurred == false) {
        if(isNewValueSet) {
          // Make the entity visible within this transaction
          transaction.addEntity(entity);
        }
        // Persists valueSetState
        session.flush();
        // Empty the Session so we don't fill it up
        session.evict(valueSetState);
      }
    }

  }
}