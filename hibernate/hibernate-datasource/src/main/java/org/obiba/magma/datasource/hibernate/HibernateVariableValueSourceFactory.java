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
import org.obiba.core.service.SortingClause;
import org.obiba.core.service.impl.hibernate.AssociationCriteria;
import org.obiba.core.service.impl.hibernate.AssociationCriteria.Operation;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableList;

class HibernateVariableValueSourceFactory implements VariableValueSourceFactory {

  private static final Logger log = LoggerFactory.getLogger(HibernateVariableValueSourceFactory.class);

  private final HibernateValueTable valueTable;

  HibernateVariableValueSourceFactory(HibernateValueTable valueTable) {
    super();
    if(valueTable == null) throw new IllegalArgumentException("valueTable cannot be null");
    this.valueTable = valueTable;
  }

  @Override
  public Set<VariableValueSource> createSources() {
    Set<VariableValueSource> sources = new LinkedHashSet<VariableValueSource>();
    AssociationCriteria criteria = AssociationCriteria.create(VariableState.class, getCurrentSession()).add("valueTable", Operation.eq, valueTable.getValueTableState()).addSortingClauses(SortingClause.create("id"));
    for(Object obj : criteria.getCriteria().setFetchMode("categories", FetchMode.JOIN).list()) {
      VariableState state = (VariableState) obj;
      sources.add(createSource(state));
    }
    return sources;
  }

  VariableValueSource createSource(VariableState variableState) {
    return createSource(variableState, true);
  }

  VariableValueSource createSource(VariableState variableState, boolean b) {
    return new HibernateVariableValueSource(variableState, b);
  }

  private Session getCurrentSession() {
    return valueTable.getDatasource().getSessionFactory().getCurrentSession();
  }

  class HibernateVariableValueSource implements VariableValueSource, VectorSource {

    private final String name;

    private Serializable variableId;

    private Variable variable;

    public HibernateVariableValueSource(VariableState state, boolean unmarshall) {
      if(state == null) throw new IllegalArgumentException("state cannot be null");

      this.name = state.getName();
      this.variableId = state.getId();

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
        VariableState state = (VariableState) getCurrentSession().createCriteria(VariableState.class).add(Restrictions.idEq(ensureVariableId())).setFetchMode("categories", FetchMode.JOIN).uniqueResult();
        unmarshall(state);
      }
      return variable;
    }

    @Override
    public Value getValue(ValueSet valueSet) {
      HibernateValueSet hibernateValueSet = (HibernateValueSet) valueSet;
      ValueSetValue vsv = hibernateValueSet.getValueSetState().getValueMap().get(name);
      if(vsv == null) return (getVariable().isRepeatable() ? getValueType().nullSequence() : getValueType().nullValue());
      if(getVariable().getValueType().equals(BinaryType.get())) {
        // build a value loader
        return getBinaryValue(valueSet, vsv);
      } else {
        return vsv.getValue();
      }
    }

    private Value getBinaryValue(ValueSet valueSet, ValueSetValue vsv) {
      Value val = vsv.getValue();
      ValueLoaderFactory factory = new HibernateValueLoaderFactory(valueTable.getTableRoot());
      if(getVariable().isRepeatable()) {
        return BinaryType.get().sequenceOfReferences(factory, val);
      } else {
        return BinaryType.get().valueOfReference(factory, val);
      }
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
      .setParameter("variableId", ensureVariableId());

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

    private Serializable ensureVariableId() {
      if(variableId == null) {
        VariableState state = (VariableState) getCurrentSession().createCriteria(VariableState.class)//
        .add(Restrictions.eq("name", name))//
        .add(Restrictions.eq("valueTable", valueTable.getValueTableState())).uniqueResult();
        if(state == null) throw new IllegalStateException("variable '" + name + "' not persisted yet.");
        this.variableId = state.getId();
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
