package org.obiba.magma.datasource.hibernate;

import java.io.Serializable;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.SortedSet;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

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

import com.google.common.collect.ImmutableList;

class HibernateVariableValueSourceFactory implements VariableValueSourceFactory {

//  private static final Logger log = LoggerFactory.getLogger(HibernateVariableValueSourceFactory.class);

  private final HibernateValueTable valueTable;

  HibernateVariableValueSourceFactory(HibernateValueTable valueTable) {
    if(valueTable == null) throw new IllegalArgumentException("valueTable cannot be null");
    this.valueTable = valueTable;
  }

  @Override
  public Set<VariableValueSource> createSources() {
    Set<VariableValueSource> sources = new LinkedHashSet<VariableValueSource>();
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

  class HibernateVariableValueSource implements VariableValueSource, VectorSource {

    private final String name;

    private Serializable variableId;

    private Variable variable;

    HibernateVariableValueSource(VariableState state, boolean unmarshall) {
      if(state == null) throw new IllegalArgumentException("state cannot be null");

      name = state.getName();
      variableId = state.getId();

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
        VariableState state = (VariableState) getCurrentSession().createCriteria(VariableState.class)
            .add(Restrictions.idEq(ensureVariableId())).setFetchMode("categories", FetchMode.JOIN) //
            .uniqueResult();
        unmarshall(state);
      }
      return variable;
    }

    @Nonnull
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

    @Nullable
    @Override
    public VectorSource asVectorSource() {
      return this;
    }

    @Override
    public Iterable<Value> getValues(final SortedSet<VariableEntity> entities) {
      if(entities.isEmpty()) {
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

            private boolean closed;

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
              if(!hasNext()) {
                throw new NoSuchElementException();
              }

              String nextEntity = resultEntities.next().getIdentifier();

              // Scroll until we find the required entity or reach the end of the results
              while(hasNextResults && !results.getString(0).equals(nextEntity)) {
                hasNextResults = results.next();
              }

              Value value = null;
              if(hasNextResults) {
                value = (Value) results.get(1);
              }
              closeCursorIfNecessary();

              return value != null
                  ? value
                  : getVariable().isRepeatable() ? getValueType().nullSequence() : getValueType().nullValue();
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

          };
        }

      };

    }

    @Nonnull
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
        VariableState state = (VariableState) getCurrentSession().createCriteria(VariableState.class)//
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
  }

}
