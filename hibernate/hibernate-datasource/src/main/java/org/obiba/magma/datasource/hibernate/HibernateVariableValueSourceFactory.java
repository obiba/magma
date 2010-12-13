package org.obiba.magma.datasource.hibernate;

import java.io.Serializable;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.SortedSet;

import org.hibernate.FetchMode;
import org.hibernate.Query;
import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.obiba.core.service.impl.hibernate.AssociationCriteria;
import org.obiba.core.service.impl.hibernate.AssociationCriteria.Operation;
import org.obiba.magma.Value;
import org.obiba.magma.ValueSet;
import org.obiba.magma.ValueType;
import org.obiba.magma.Variable;
import org.obiba.magma.VariableEntity;
import org.obiba.magma.VariableValueSource;
import org.obiba.magma.VariableValueSourceFactory;
import org.obiba.magma.VectorSource;
import org.obiba.magma.datasource.hibernate.HibernateValueTable.HibernateValueSet;
import org.obiba.magma.datasource.hibernate.converter.VariableConverter;
import org.obiba.magma.datasource.hibernate.domain.ValueSetValue;
import org.obiba.magma.datasource.hibernate.domain.VariableState;

import com.google.common.collect.ImmutableList;

class HibernateVariableValueSourceFactory implements VariableValueSourceFactory {

  private final HibernateValueTable valueTable;

  HibernateVariableValueSourceFactory(HibernateValueTable valueTable) {
    super();
    if(valueTable == null) throw new IllegalArgumentException("valueTable cannot be null");
    this.valueTable = valueTable;
  }

  @Override
  public Set<VariableValueSource> createSources() {
    Set<VariableValueSource> sources = new LinkedHashSet<VariableValueSource>();
    AssociationCriteria criteria = AssociationCriteria.create(VariableState.class, getCurrentSession()).add("valueTable", Operation.eq, valueTable.getValueTableState());
    for(Object obj : criteria.getCriteria().setFetchMode("categories", FetchMode.JOIN).list()) {
      VariableState state = (VariableState) obj;
      sources.add(new HibernateVariableValueSource(state, true));
    }
    return sources;
  }

  VariableValueSource createSource(VariableState variableState) {
    return new HibernateVariableValueSource(variableState, true);
  }

  private Session getCurrentSession() {
    return valueTable.getDatasource().getSessionFactory().getCurrentSession();
  }

  class HibernateVariableValueSource implements VariableValueSource, VectorSource {

    private final Serializable variableId;

    private final String name;

    private Variable variable;

    public HibernateVariableValueSource(VariableState state, boolean unmarshall) {
      if(state == null) throw new IllegalArgumentException("state cannot be null");
      if(state.getId() == null) throw new IllegalArgumentException("state must be persisted");
      this.variableId = state.getId();
      this.name = state.getName();

      if(unmarshall) {
        unmarshall(state);
      }
    }

    public Serializable getVariableId() {
      return variableId;
    }

    @Override
    public synchronized Variable getVariable() {
      if(variable == null) {
        VariableState state = (VariableState) getCurrentSession().createCriteria(VariableState.class).add(Restrictions.idEq(this.variableId)).setFetchMode("categories", FetchMode.JOIN).uniqueResult();
        unmarshall(state);
      }
      return variable;
    }

    @Override
    public Value getValue(ValueSet valueSet) {
      HibernateValueSet hibernateValueSet = (HibernateValueSet) valueSet;
      ValueSetValue vsv = hibernateValueSet.getValueSetState().getValueMap().get(name);
      if(vsv != null) return vsv.getValue();
      return (getVariable().isRepeatable() ? getValueType().nullSequence() : getValueType().nullValue());
    }

    @Override
    public VectorSource asVectorSource() {
      return this;
    }

    @Override
    public Iterable<Value> getValues(final SortedSet<VariableEntity> entities) {
      if(entities.size() == 0) {
        return ImmutableList.of();
      }

      // This will returns one row per value set in the value table (so it includes nulls)
      final Query valuesQuery = getCurrentSession().getNamedQuery("allValues")//
      .setParameter("valueTableId", valueTable.getValueTableState().getId())//
      .setParameter("variableId", valueTable.getVariableId(getVariable()));

      return new Iterable<Value>() {

        @Override
        public Iterator<Value> iterator() {

          return new Iterator<Value>() {

            private final ScrollableResults results;

            private final Iterator<VariableEntity> resultEntities;

            private boolean hasNextResults;

            private boolean closed = false;

            {
              resultEntities = entities.iterator();
              results = valuesQuery.scroll(ScrollMode.FORWARD_ONLY);
              hasNextResults = results.next();
            }

            @Override
            public boolean hasNext() {
              return resultEntities.hasNext();
            }

            @Override
            public Value next() {
              if(hasNext() == false) {
                throw new NoSuchElementException();
              }

              String nextEntity = resultEntities.next().getIdentifier();

              // Scroll until we find the required entity or reach the end of the results
              while(hasNextResults && results.getString(0).equals(nextEntity) == false) {
                hasNextResults = results.next();
              }

              Value value = null;
              if(hasNextResults) {
                value = (Value) results.get(1);
              }
              closeCursorIfNecessary();

              return value != null ? value : (getVariable().isRepeatable() ? getValueType().nullSequence() : getValueType().nullValue());
            }

            @Override
            public void remove() {
              throw new UnsupportedOperationException();
            }

            private void closeCursorIfNecessary() {
              if(closed == false) {
                // Close the cursor if we don't have any more results or no more entities to return
                if(hasNextResults == false || hasNext() == false) {
                  closed = true;
                  results.close();
                }
              }
            }

          };
        }

      };

    }

    @Override
    public ValueType getValueType() {
      return getVariable().getValueType();
    }

    /**
     * Initialises the {@code variable} attribute from the provided state
     * @param state
     */
    private void unmarshall(VariableState state) {
      variable = VariableConverter.getInstance().unmarshal(state, null);
    }

    @Override
    public boolean equals(Object obj) {
      if(this == obj) {
        return true;
      }
      if(obj == null) {
        return false;
      }
      if(obj instanceof HibernateVariableValueSource == false) {
        return super.equals(obj);
      }
      HibernateVariableValueSource rhs = (HibernateVariableValueSource) obj;
      return this.name.equals(rhs.name);
    }

    @Override
    public int hashCode() {
      return this.name.hashCode();
    }
  }

}
