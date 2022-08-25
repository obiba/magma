/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/**
 *
 */
package org.obiba.magma.datasource.hibernate;

import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

import org.hibernate.FlushMode;
import org.hibernate.LockMode;
import org.hibernate.LockOptions;
import org.hibernate.Session;
import org.json.JSONException;
import org.json.JSONObject;
import org.obiba.core.domain.IEntity;
import org.obiba.core.service.impl.hibernate.AssociationCriteria;
import org.obiba.core.service.impl.hibernate.AssociationCriteria.Operation;
import org.obiba.magma.MagmaRuntimeException;
import org.obiba.magma.NoSuchVariableException;
import org.obiba.magma.Value;
import org.obiba.magma.ValueTableWriter;
import org.obiba.magma.Variable;
import org.obiba.magma.VariableEntity;
import org.obiba.magma.datasource.hibernate.converter.HibernateMarshallingContext;
import org.obiba.magma.datasource.hibernate.converter.VariableConverter;
import org.obiba.magma.datasource.hibernate.converter.VariableEntityConverter;
import org.obiba.magma.datasource.hibernate.domain.ValueSetBinaryValue;
import org.obiba.magma.datasource.hibernate.domain.ValueSetState;
import org.obiba.magma.datasource.hibernate.domain.ValueSetValue;
import org.obiba.magma.datasource.hibernate.domain.VariableEntityState;
import org.obiba.magma.datasource.hibernate.domain.VariableState;
import org.obiba.magma.support.VariableHelper;
import org.obiba.magma.type.BinaryType;
import org.obiba.magma.type.TextType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import static com.google.common.base.Preconditions.checkArgument;

class HibernateValueTableWriter implements ValueTableWriter {

  private static final Logger log = LoggerFactory.getLogger(HibernateValueTableWriter.class);

  private final HibernateValueTable valueTable;

  private final HibernateValueTableTransaction transaction;

  private final VariableConverter variableConverter = VariableConverter.getInstance();

  private final Session session;

  private final HibernateVariableValueSourceFactory valueSourceFactory;

  private boolean errorOccurred = false;

  private boolean dirty = false;

  private final HibernateMarshallingContext context;

  HibernateValueTableWriter(HibernateValueTableTransaction transaction) {
    if(transaction == null) throw new IllegalArgumentException("transaction cannot be null");
    this.transaction = transaction;
    valueTable = transaction.getValueTable();

    session = valueTable.getDatasource().getSessionFactory().getCurrentSession();
    valueSourceFactory = new HibernateVariableValueSourceFactory(valueTable);
    if(session.getHibernateFlushMode() != FlushMode.MANUAL) {
      session.setHibernateFlushMode(FlushMode.MANUAL);
    }

    context = valueTable.createContext();
  }

  @NotNull
  @Override
  public ValueSetWriter writeValueSet(@NotNull VariableEntity entity) {
    return new HibernateValueSetWriter(entity);
  }

  @Override
  public VariableWriter writeVariables() {
    return new HibernateVariableWriter();
  }

  @Override
  public void close() {
  }

  private void updateTableLastUpdate() {
    valueTable.getValueTableState().setUpdated(new Date());
  }

  private class HibernateVariableWriter implements VariableWriter {

    private HibernateVariableWriter() {
    }

    @Override
    public void writeVariable(@NotNull Variable variable) {
      //noinspection ConstantConditions
      checkArgument(variable != null, "variable cannot be null");
      checkArgument(valueTable.isForEntityType(variable.getEntityType()),
          "Wrong entity type for variable '" + variable.getName() + "': " + valueTable.getEntityType() +
              " expected, " + variable.getEntityType() + " received.");

      // add or update variable
      boolean insertOrUpdate = true;
      if (valueTable.hasVariable(variable.getName())) {
        Variable existingVariable = valueTable.getVariable(variable.getName());
        insertOrUpdate = VariableHelper.isModified(existingVariable, variable);
      }
      if (insertOrUpdate) {
        errorOccurred = true;
        VariableState state = variableConverter.marshal(variable, context);
        transaction.addSource(valueSourceFactory.createSource(state));
        errorOccurred = false;

      }
      dirty = true;
    }

    @Override
    public void removeVariable(@NotNull Variable variable) {

      errorOccurred = true;

      VariableState variableState = valueTable.getVariableState(variable);

      deleteVariableValues(variable, variableState);

      transaction.removeSource(valueSourceFactory.createSource(variableState));

      // I don't know why variable delete does not work but I had to manually delete categories (with attributes)
      // and variable (with attributes) using native SQL and HQL...
      deleteVariableCategories(variableState);
      deleteVariable(variableState);

      if (valueTable.getVariableCount() == 0) {
        valueTable.refreshEntityProvider();
      }

      errorOccurred = false;

      dirty = true;
    }

    private void deleteVariableValues(Variable variable, IEntity variableState) {
      int nbDeletedValues = session.getNamedQuery("deleteVariableValueSetValues") //
          .setParameter("variableId", variableState.getId()) //
          .executeUpdate();
      log.debug("Deleted {} value from {}", nbDeletedValues, valueTable.getName());

      if(variable.getValueType().equals(BinaryType.get())) {
        List<?> valueSetIds = session.getNamedQuery("findValueSetIdsByTableId") //
            .setParameter("valueTableId", valueTable.getValueTableId()) //
            .list();

        if (valueSetIds != null && !valueSetIds.isEmpty()) {
          int nbBinariesDeleted = session.getNamedQuery("deleteVariableBinaryValues") //
              .setParameterList("valueSetIds", valueSetIds) //
              .setParameter("variableId", variableState.getId()) //
              .executeUpdate();
          log.debug("Deleted {} binaries from {}", nbBinariesDeleted, valueTable.getName());
        }
      }

      // delete empty value sets
      int nbEmptyValueSets = session.getNamedQuery("deleteEmptyValueSets").executeUpdate();
      log.debug("Deleted {} empty value sets from {}", nbEmptyValueSets, valueTable.getName());

      // update all value sets last update
      int updated = session.getNamedQuery("setLastUpdateForTableId") //
          .setParameter("updated", new Date()) //
          .setParameter("valueTableId", valueTable.getValueTableId()) //
          .executeUpdate();
      log.debug("Updated lastUpdate for {} value sets for {}", updated, valueTable.getName());
    }

    private void deleteVariableCategories(IEntity variableState) {
      session.createSQLQuery("delete from category_attributes where category_id in " + //
          "(select c.id from category c where c.variable_id = " + variableState.getId() + ")") //
          .executeUpdate();

      int deletedCategories = session.createQuery("delete CategoryState where variable = :variable")
          .setEntity("variable", variableState).executeUpdate();
      log.debug("Deleted {} categories", deletedCategories);
    }

    private void deleteVariable(VariableState variableState) {
      session.createSQLQuery("delete from variable_attributes where variable_id = " + variableState.getId())
          .executeUpdate();

      int deletedVariables = session.createQuery("delete VariableState where valueTable = :table and name = :name")
          .setEntity("table", variableState.getValueTable()).setString("name", variableState.getName()).executeUpdate();
      log.debug("Deleted {} variables", deletedVariables);
    }

    @Override
    public void close() {
      if(!errorOccurred) {

        if(dirty) {
          updateTableLastUpdate();
          dirty = false;
        }

        // persists data and empty the Session so we don't fill it up
        session.flush();
        session.clear();
      }
    }
  }

  private class HibernateValueSetWriter implements ValueSetWriter {

    @SuppressWarnings("TypeMayBeWeakened")
    private final VariableEntityConverter entityConverter = new VariableEntityConverter();

    private final ValueSetState valueSetState;

    @NotNull
    private final VariableEntity entity;

    private final boolean isNewValueSet;

    private final Map<String, ValueSetValue> values;

    private HibernateValueSetWriter(@NotNull VariableEntity entity) {
      //noinspection ConstantConditions
      if(entity == null) throw new IllegalArgumentException("entity cannot be null");
      this.entity = entity;

      // find entity or create it
      VariableEntityState variableEntityState = entityConverter.marshal(entity, context);

      // Will update version timestamp if it exists
      ValueSetState state = (ValueSetState) AssociationCriteria.create(ValueSetState.class, session) //
          .add("valueTable", Operation.eq, valueTable.getValueTableState()) //
          .add("variableEntity", Operation.eq, variableEntityState) //
          .getCriteria() //
          .uniqueResult();

      if(state == null) {
        state = new ValueSetState(valueTable.getValueTableState(), variableEntityState);
        // Persists the ValueSet
        session.save(state);
        session.flush();
        session.refresh(state); //OPAL-2635
        values = Maps.newHashMap();
        isNewValueSet = true;
      } else {
        values = Maps.newHashMap(state.getValueMap());
        isNewValueSet = false;
      }

      session.buildLockRequest(new LockOptions(LockMode.PESSIMISTIC_FORCE_INCREMENT).setScope(false)).lock(state);
      valueSetState = state;
    }

    @Override
    @SuppressWarnings("PMD.NcssMethodCount")
    public void writeValue(@NotNull Variable variable, @NotNull Value value) {
      //noinspection ConstantConditions
      if(variable == null) throw new IllegalArgumentException("variable cannot be null");
      //noinspection ConstantConditions
      if(value == null) throw new IllegalArgumentException("value cannot be null");

      try {
        VariableState variableState = variableConverter.getStateForVariable(variable, valueTable.createContext());
        if(variableState == null) {
          throw new NoSuchVariableException(valueTable.getName(), variable.getName());
        }
        ValueSetValue valueSetValue = values.get(variable.getName());
        if(valueSetValue == null) {
          createValue(variable, value, variableState);
        } else {
          updateValue(variable, value, valueSetValue);
        }

        dirty = true;

      } catch(RuntimeException | Error e) {
        errorOccurred = true;
        throw e;
      }
    }

    @Override
    public void remove() {
      valueTable.dropValueSet(entity, valueSetState.getId());
    }

    private void createValue(Variable variable, Value value, VariableState variableState) {
      if(value.isNull()) return;

      ValueSetValue valueSetValue = new ValueSetValue(variableState, valueSetState);

      if(BinaryType.get().equals(value.getValueType())) {
        writeBinaryValue(valueSetValue, value, false);
      } else {
        valueSetValue.setValue(value);
      }

      addValue(variable, valueSetValue);
    }

    private void updateValue(Variable variable, Value value, ValueSetValue valueSetValue) {
      if(value.isNull()) {
        removeValue(variable, valueSetValue);
      } else {
        if(BinaryType.get().equals(value.getValueType())) {
          writeBinaryValue(valueSetValue, value, true);
        } else {
          valueSetValue.setValue(value);
        }
      }
    }

    private void addValue(Variable variable, ValueSetValue valueSetValue) {
      valueSetState.getValues().add(valueSetValue);
      values.put(variable.getName(), valueSetValue);
    }

    private void removeValue(Variable variable, ValueSetValue valueSetValue) {
      valueSetState.getValues().remove(valueSetValue);
      values.remove(variable.getName());
    }

    @SuppressWarnings("ConstantConditions")
    private void writeBinaryValue(ValueSetValue valueSetValue, Value value, boolean isUpdate) {
      if(value.isSequence()) {
        List<Value> sequenceValues = Lists.newArrayList();
        if(!value.isNull()) {
          int occurrence = 0;
          for(Value valueOccurrence : value.asSequence().getValue()) {
            sequenceValues.add(createBinaryValue(valueSetValue, valueOccurrence, occurrence++, isUpdate));
          }
        }
        valueSetValue.setValue(TextType.get().sequenceOf(sequenceValues));
      } else {
        valueSetValue.setValue(createBinaryValue(valueSetValue, value, 0, isUpdate));
      }
    }

    private Value createBinaryValue(ValueSetValue valueSetValue, Value inputValue, int occurrence, boolean isUpdate) {
      ValueSetBinaryValue binaryValue = isUpdate ? findBinaryValue(valueSetValue, occurrence) : null;
      if(binaryValue == null) {
        binaryValue = createBinaryValue(valueSetValue, inputValue, occurrence);
      } else if(inputValue.isNull()) {
        session.delete(binaryValue);
      } else {
        // update
        binaryValue.setValue((byte[]) inputValue.getValue());
      }
      if(binaryValue == null) {
        // can be null if empty byte[]
        return TextType.get().nullValue();
      }
      session.save(binaryValue);
      return getBinaryMetadata(binaryValue);
    }

    private ValueSetBinaryValue findBinaryValue(ValueSetValue valueSetValue, int occurrence) {
      return (ValueSetBinaryValue) session.getNamedQuery("findBinaryByValueSetValueAndOccurrence") //
          .setParameter("variableId", valueSetValue.getVariable().getId()) //
          .setParameter("valueSetId", valueSetValue.getValueSet().getId()) //
          .setParameter("occurrence", occurrence) //
          .uniqueResult();
    }

    @Nullable
    private ValueSetBinaryValue createBinaryValue(ValueSetValue valueSetValue, Value value, int occurrence) {
      if(value.isNull()) return null;
      ValueSetBinaryValue binaryValue = new ValueSetBinaryValue(valueSetValue, occurrence);
      binaryValue.setValue((byte[]) value.getValue());
      return binaryValue;
    }

    private Value getBinaryMetadata(ValueSetBinaryValue binaryValue) {
      try {
        JSONObject properties = new JSONObject();
        properties.put("size", binaryValue == null ? 0 : binaryValue.getSize());
        return TextType.get().valueOf(properties.toString());
      } catch(JSONException e) {
        throw new MagmaRuntimeException(e);
      }
    }

    @Override
    public void close() {
      if(!errorOccurred) {
        if(isNewValueSet) {
          // Make the entity visible within this transaction
          transaction.addEntity(entity);
        }

        if(dirty) {
          updateTableLastUpdate();
          dirty = false;
        }

        // persists valueSetState and empty the Session so we don't fill it up
        session.flush();
        session.clear();
      }
    }

  }

}