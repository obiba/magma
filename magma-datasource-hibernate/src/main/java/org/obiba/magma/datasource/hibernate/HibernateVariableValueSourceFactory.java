package org.obiba.magma.datasource.hibernate;

import java.io.Serializable;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;

import javax.validation.constraints.NotNull;

import org.hibernate.FetchMode;
import org.hibernate.Query;
import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.obiba.core.domain.IEntity;
import org.obiba.core.service.SortingClause;
import org.obiba.core.service.impl.hibernate.AssociationCriteria;
import org.obiba.core.service.impl.hibernate.AssociationCriteria.Operation;
import org.obiba.magma.AbstractVariableValueSource;
import org.obiba.magma.Value;
import org.obiba.magma.ValueLoaderFactory;
import org.obiba.magma.ValueSet;
import org.obiba.magma.ValueType;
import org.obiba.magma.Variable;
import org.obiba.magma.VariableEntity;
import org.obiba.magma.VariableValueSource;
import org.obiba.magma.VariableValueSourceFactory;
import org.obiba.magma.VectorSource;
import org.obiba.magma.datasource.hibernate.HibernateValueTable.HibernateValueSet;
import org.obiba.magma.datasource.hibernate.converter.HibernateValueLoaderFactory;
import org.obiba.magma.datasource.hibernate.converter.VariableConverter;
import org.obiba.magma.datasource.hibernate.domain.ValueSetValue;
import org.obiba.magma.datasource.hibernate.domain.VariableState;
import org.obiba.magma.type.BinaryType;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;

class HibernateVariableValueSourceFactory implements VariableValueSourceFactory {

//  private static final Logger log = LoggerFactory.getLogger(HibernateVariableValueSourceFactory.class);

  private final HibernateValueTable valueTable;

  HibernateVariableValueSourceFactory(HibernateValueTable valueTable) {
    if(valueTable == null) throw new IllegalArgumentException("valueTable cannot be null");
    this.valueTable = valueTable;
  }

  @Override
  public Set<VariableValueSource> createSources() {
    Set<VariableValueSource> sources = new LinkedHashSet<>();
    @SuppressWarnings("unchecked")
    Iterable<VariableState> variables = (List<VariableState>) AssociationCriteria
        .create(VariableState.class, getCurrentSession())
        .add("valueTable", Operation.eq, valueTable.getValueTableState()) //
        .addSortingClauses(SortingClause.create("id")) //
        .getCriteria().setFetchMode("categories", FetchMode.JOIN).list();
    for(VariableState v : variables) {
      sources.add(createSource(v));
    }
    return sources;
  }

  VariableValueSource createSource(VariableState variableState) {
    return createSource(variableState, true);
  }

  VariableValueSource createSource(VariableState variableState, boolean unmarshall) {
    return new HibernateVariableValueSource(variableState, unmarshall);
  }

  private Session getCurrentSession() {
    return valueTable.getDatasource().getSessionFactory().getCurrentSession();
  }

  class HibernateVariableValueSource extends AbstractVariableValueSource implements VariableValueSource, VectorSource {

    private final String name;

    private Serializable variableId;

    private Variable variable;

    HibernateVariableValueSource(@NotNull VariableState state, boolean unmarshall) {
      //noinspection ConstantConditions
      if(state == null) throw new IllegalArgumentException("state cannot be null");

      name = state.getName();
      variableId = state.getId();

      if(unmarshall) {
        unmarshall(state);
      }
    }

    public VariableState getVariableState() {
      return (VariableState) getCurrentSession().get(VariableState.class, ensureVariableId());
    }

    @NotNull
    @Override
    public synchronized Variable getVariable() {
      if(variable == null) {
        VariableState state = (VariableState) getCurrentSession().createCriteria(VariableState.class)
            .add(Restrictions.idEq(ensureVariableId())).setFetchMode("categories", FetchMode.JOIN) //
            .uniqueResult();
        unmarshall(state);
      }
      return variable;
    }

    @NotNull
    @Override
    public Value getValue(ValueSet valueSet) {
      HibernateValueSet hibernateValueSet = (HibernateValueSet) valueSet;
      ValueSetValue vsv = hibernateValueSet.getValueSetState().getValueMap().get(name);
      if(vsv == null) {
        return getVariable().isRepeatable() ? getValueType().nullSequence() : getValueType().nullValue();
      }
      return getVariable().getValueType().equals(BinaryType.get()) //
          ? getBinaryValue(vsv) //
          : vsv.getValue();
    }

    private Value getBinaryValue(ValueSetValue vsv) {
      Value val = vsv.getValue();
      ensureVariableId();
      ValueLoaderFactory factory = new HibernateValueLoaderFactory(valueTable.getDatasource().getSessionFactory(), vsv);
      return getVariable().isRepeatable() //
          ? BinaryType.get().sequenceOfReferences(factory, val) //
          : BinaryType.get().valueOfReference(factory, val);
    }

    @Override
    public boolean supportVectorSource() {
      return true;
    }

    @NotNull
    @Override
    public VectorSource asVectorSource() {
      return this;
    }

    @Override
    public Iterable<Value> getValues(final SortedSet<VariableEntity> entities) {
      if(entities.isEmpty()) {
        return ImmutableList.of();
      }
      return new Iterable<Value>() {
        @Override
        public Iterator<Value> iterator() {
          return new ValueIterator(entities.iterator());
        }
      };
    }

    @NotNull
    @Override
    public ValueType getValueType() {
      return getVariable().getValueType();
    }

    /**
     * Initialises the {@code variable} attribute from the provided state
     *
     * @param state
     */
    private void unmarshall(VariableState state) {
      variable = VariableConverter.getInstance().unmarshal(state, null);
    }

    private Serializable ensureVariableId() {
      if(variableId == null) {
        IEntity state = (IEntity) getCurrentSession().createCriteria(VariableState.class) //
            .add(Restrictions.eq("name", name))//
            .add(Restrictions.eq("valueTable", valueTable.getValueTableState())).uniqueResult();
        if(state == null) throw new IllegalStateException("variable '" + name + "' not persisted yet.");
        variableId = state.getId();
      }
      return variableId;
    }

    @Override
    public boolean equals(Object obj) {
      if(this == obj) {
        return true;
      }
      if(obj == null) {
        return false;
      }
      if(!(obj instanceof HibernateVariableValueSource)) {
        return super.equals(obj);
      }
      HibernateVariableValueSource rhs = (HibernateVariableValueSource) obj;
      return name.equals(rhs.name);
    }

    @Override
    public int hashCode() {
      return name.hashCode();
    }

    private class ValueIterator implements Iterator<Value> {

      private final ScrollableResults results;

      private boolean hasNextResults;

      private boolean closed;

      private final Iterator<VariableEntity> entities;

      private final Map<String, Value> valueMap = Maps.newHashMap();

      private ValueIterator(Iterator<VariableEntity> entities) {

        this.entities = entities;
        Query query = getCurrentSession().getNamedQuery("allValues") //
            .setParameter("valueTableId", valueTable.getValueTableState().getId()) //
            .setParameter("variableId", ensureVariableId());
        results = query.scroll(ScrollMode.FORWARD_ONLY);
        hasNextResults = results.next();
      }

      @Override
      public boolean hasNext() {
        return entities.hasNext();
      }

      @Override
      public Value next() {
        VariableEntity entity = entities.next();

        if(valueMap.containsKey(entity.getIdentifier())) return getValueFromMap(entity);

        boolean found = false;
        // Scroll until we find the required entity or reach the end of the results
        while(hasNextResults && !found) {
          String id = results.getString(0);
          Value value = getValue((Serializable) results.get(2), (Value) results.get(1));
          valueMap.put(id, value);
          if(entity.getIdentifier().equals(id)) {
            found = true;
          }
          hasNextResults = results.next();
        }

        closeCursorIfNecessary();

        if(valueMap.containsKey(entity.getIdentifier())) return getValueFromMap(entity);
        return getVariable().isRepeatable() ? getValueType().nullSequence() : getValueType().nullValue();
      }

      /**
       * No duplicate of entities, so remove value from map once get.
       * @param entity
       * @return
       */
      private Value getValueFromMap(VariableEntity entity) {
        Value value = valueMap.get(entity.getIdentifier());
        valueMap.remove(entity.getIdentifier());
        return value;
      }

      private Value getValue(Serializable valueSetId, Value value) {
        if(value == null) {
          return getVariable().isRepeatable() ? getValueType().nullSequence() : getValueType().nullValue();
        }

        if(getValueType().equals(BinaryType.get())) {
          ValueLoaderFactory factory = new HibernateValueLoaderFactory(valueTable.getDatasource().getSessionFactory(),
              ensureVariableId(), valueSetId);
          return getVariable().isRepeatable()
              ? BinaryType.get().sequenceOfReferences(factory, value)
              : BinaryType.get().valueOfReference(factory, value);
        }

        return value;
      }

      @Override
      public void remove() {
        throw new UnsupportedOperationException();
      }

      private void closeCursorIfNecessary() {
        if(!closed) {
          // Close the cursor if we don't have any more results or no more entities to return
          if(!hasNextResults || !hasNext()) {
            closed = true;
            results.close();
          }
        }
      }
    }
  }
}
