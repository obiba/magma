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

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;

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
    public Iterable<Value> getValues(SortedSet<VariableEntity> entities) {
      if(entities.size() == 0) {
        return ImmutableList.of();
      }
      final Query valuesQuery;
      valuesQuery = getCurrentSession().getNamedQuery("valuesWithEntities");
      valuesQuery.setParameter("entityType", valueTable.getEntityType())//
      .setParameterList("identifiers", ImmutableList.copyOf(Iterables.transform(entities, new Function<VariableEntity, String>() {

        @Override
        public String apply(VariableEntity from) {
          return from.getIdentifier();
        }
      })));

      valuesQuery//
      .setParameter("valueTableId", valueTable.getValueTableState().getId())//
      .setParameter("variableId", valueTable.getVariableId(getVariable()));

      return new Iterable<Value>() {

        @Override
        public Iterator<Value> iterator() {

          return new Iterator<Value>() {

            private final ScrollableResults results;

            private boolean hasNext;

            {
              results = valuesQuery.scroll(ScrollMode.FORWARD_ONLY);
              hasNext = results.next();
            }

            @Override
            public boolean hasNext() {
              return hasNext;
            }

            @Override
            public Value next() {
              if(hasNext == false) {
                throw new NoSuchElementException();
              }
              Value value = (Value) results.get(0);
              hasNext = results.next();
              if(hasNext == false) {
                results.close();
              }
              return value != null ? value : (getVariable().isRepeatable() ? getValueType().nullSequence() : getValueType().nullValue());
            }

            @Override
            public void remove() {
              throw new UnsupportedOperationException();
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
