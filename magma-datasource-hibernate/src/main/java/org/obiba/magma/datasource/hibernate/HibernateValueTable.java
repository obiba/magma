package org.obiba.magma.datasource.hibernate;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.SortedSet;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

import org.hibernate.Criteria;
import org.hibernate.FetchMode;
import org.hibernate.LockMode;
import org.hibernate.LockOptions;
import org.hibernate.Query;
import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;
import org.hibernate.Session;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.obiba.core.service.impl.hibernate.AssociationCriteria;
import org.obiba.core.service.impl.hibernate.AssociationCriteria.Operation;
import org.obiba.magma.Datasource;
import org.obiba.magma.Initialisable;
import org.obiba.magma.NoSuchValueSetException;
import org.obiba.magma.NoSuchVariableException;
import org.obiba.magma.Timestamps;
import org.obiba.magma.TimestampsBean;
import org.obiba.magma.Value;
import org.obiba.magma.ValueSet;
import org.obiba.magma.Variable;
import org.obiba.magma.VariableEntity;
import org.obiba.magma.VariableValueSource;
import org.obiba.magma.VariableValueSourceFactory;
import org.obiba.magma.datasource.hibernate.HibernateVariableValueSourceFactory.HibernateVariableValueSource;
import org.obiba.magma.datasource.hibernate.converter.HibernateMarshallingContext;
import org.obiba.magma.datasource.hibernate.domain.Timestamped;
import org.obiba.magma.datasource.hibernate.domain.ValueSetState;
import org.obiba.magma.datasource.hibernate.domain.ValueTableState;
import org.obiba.magma.datasource.hibernate.domain.VariableState;
import org.obiba.magma.support.AbstractValueTable;
import org.obiba.magma.support.AbstractVariableEntityProvider;
import org.obiba.magma.support.NullTimestamps;
import org.obiba.magma.support.ValueSetBean;
import org.obiba.magma.support.VariableEntityBean;
import org.obiba.magma.type.DateTimeType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;

@SuppressWarnings("OverlyCoupledClass")
class HibernateValueTable extends AbstractValueTable {

  private static final Logger log = LoggerFactory.getLogger(HibernateValueTable.class);

  private final Serializable valueTableId;

  private final HibernateVariableEntityProvider variableEntityProvider;

  private Map<String, Timestamps> valueSetTimestamps;

  HibernateValueTable(Datasource datasource, ValueTableState state) {
    super(datasource, state.getName());
    valueTableId = state.getId();
    setVariableEntityProvider(variableEntityProvider = new HibernateVariableEntityProvider(state.getEntityType()));
  }

  @Override
  public void initialise() {
    super.initialise();
    variableEntityProvider.initialise();
    readVariables();
  }

  @NotNull
  @Override
  public HibernateDatasource getDatasource() {
    return (HibernateDatasource) super.getDatasource();
  }

  @Override
  public ValueSet getValueSet(VariableEntity entity) throws NoSuchValueSetException {
    if(!hasValueSet(entity)) {
      throw new NoSuchValueSetException(this, entity);
    }
    AssociationCriteria criteria = AssociationCriteria
        .create(ValueSetState.class, getDatasource().getSessionFactory().getCurrentSession())
        .add("valueTable.id", Operation.eq, valueTableId)
        .add("variableEntity.identifier", Operation.eq, entity.getIdentifier())
        .add("variableEntity.type", Operation.eq, entity.getType());

    return new HibernateValueSet(entity, criteria.getCriteria().setFetchMode("values", FetchMode.JOIN));
  }

  @Override
  public boolean canDropValueSets() {
    return true;
  }

  @Override
  public void dropValueSets() {
    Session session = getDatasource().getSessionFactory().getCurrentSession();

    getDatasource().deleteValueSets(getDatasource().getName() + "." + getName(), session,
        session.getNamedQuery("findValueSetIdsByTableId").setParameter("valueTableId", getValueTableState().getId())
            .list());

    session.buildLockRequest(new LockOptions(LockMode.PESSIMISTIC_FORCE_INCREMENT)).lock(getValueTableState());

    variableEntityProvider.initialise();
  }

  @Override
  public Timestamps getValueSetTimestamps(VariableEntity entity) throws NoSuchValueSetException {
    if(valueSetTimestamps == null) {
      cacheValueSetTimestamps();
    }
    Timestamps cachedTimestamps = valueSetTimestamps.get(entity.getIdentifier());
    if(cachedTimestamps == null) {
      if(hasValueSet(entity)) {
        // not in the cache because cache is outdated
        cacheValueSetTimestamps();
        cachedTimestamps = valueSetTimestamps.get(entity.getIdentifier());
      } else {
        // not in the cache because not a valid value set
        throw new NoSuchValueSetException(this, entity);
      }
    }

    return cachedTimestamps == null ? NullTimestamps.get() : cachedTimestamps;
  }

  @Override
  public Iterable<Timestamps> getValueSetTimestamps(final SortedSet<VariableEntity> entities) {
    if(entities.isEmpty()) {
      return ImmutableList.of();
    }
    return new Iterable<Timestamps>() {
      @Override
      public Iterator<Timestamps> iterator() {
        return new TimestampsIterator(entities.iterator());
      }
    };
  }

  void dropValueSet(VariableEntity entity, Serializable valueSetId) {
    Session session = getDatasource().getSessionFactory().getCurrentSession();

    getDatasource()
        .deleteValueSets(getDatasource().getName() + "." + getName(), session, Collections.singleton(valueSetId));

    session.buildLockRequest(new LockOptions(LockMode.PESSIMISTIC_FORCE_INCREMENT)).lock(getValueTableState());

    variableEntityProvider.remove(entity);
  }

  @SuppressWarnings("unchecked")
  private void cacheValueSetTimestamps() {
    valueSetTimestamps = Maps.newHashMap();
    Query query = getDatasource().getSessionFactory().getCurrentSession().createSQLQuery(
        "SELECT ve.identifier, vs.created, vs.updated FROM value_set vs, variable_entity ve " +
            "WHERE ve.id = vs.variable_entity_id AND vs.value_table_id = :value_table_id AND ve.type = :entity_type").setParameter(
        "value_table_id", valueTableId) //
        .setParameter("entity_type", getEntityType());
    for(final Object[] row : (List<Object[]>) query.list()) {
      valueSetTimestamps.put((String) row[0], new Timestamps() {

        @NotNull
        @Override
        public Value getCreated() {
          return DateTimeType.get().valueOf(row[1]);
        }

        @NotNull
        @Override
        public Value getLastUpdate() {
          return DateTimeType.get().valueOf(row[2]);
        }
      });
    }
  }

  @NotNull
  @Override
  public Timestamps getTimestamps() {
    return createTimestamps(getValueTableState());
  }

  @Override
  public int getVariableCount() {
    return ((Number) getDatasource().getSessionFactory().getCurrentSession() //
        .createCriteria(VariableState.class) //
        .setProjection(Projections.rowCount())  //
        .add(Restrictions.eq("valueTable", getValueTableState())) //
        .uniqueResult()).intValue();
  }

  @Override
  public int getValueSetCount() {
    return ((Number) getDatasource().getSessionFactory().getCurrentSession() //
        .createCriteria(ValueSetState.class) //
        .setProjection(Projections.rowCount())  //
        .add(Restrictions.eq("valueTable", getValueTableState())) //
        .uniqueResult()).intValue();
  }

  @Override
  public int getVariableEntityCount() {
    return getValueSetCount();
  }

  public void setName(String name) {
    ValueTableState tableState = getValueTableState();
    tableState.setName(name);
    getDatasource().getSessionFactory().getCurrentSession().save(tableState);
    this.name = name;
  }

  private static Timestamps createTimestamps(@Nullable final Timestamped timestamped) {
    return timestamped == null ? NullTimestamps.get() : new Timestamps() {

      @NotNull
      @Override
      public Value getLastUpdate() {
        return DateTimeType.get().valueOf(timestamped.getUpdated());
      }

      @NotNull
      @Override
      public Value getCreated() {
        return DateTimeType.get().valueOf(timestamped.getCreated());
      }
    };
  }

  @Override
  public VariableValueSource getVariableValueSource(final String variableName) throws NoSuchVariableException {
    try {
      return Iterables.find(getSources(), new Predicate<VariableValueSource>() {
        @Override
        public boolean apply(VariableValueSource variableValueSource) {
          return variableValueSource.getVariable().getName().equals(variableName);
        }
      });
    } catch(NoSuchElementException e) {
      throw new NoSuchVariableException(getName(), variableName);
    }
  }

  @Override
  public boolean hasVariable(String variableName) {
    for(VariableValueSource source : getSources()) {
      if(source.getVariable().getName().equals(variableName)) {
        return true;
      }
    }
    return false;
  }

  /**
   * Overridden to include uncommitted sources when a transaction exists on this table and is visible in the current
   * session.
   */
  @Override
  protected Set<VariableValueSource> getSources() {
    if(getDatasource().hasTableTransaction(getName())) {
      return new ImmutableSet.Builder<VariableValueSource>() //
          .addAll(super.getSources()) //
          .addAll(getDatasource().getTableTransaction(getName()).getUncommittedSources()) //
          .build();
    }
    return Collections.unmodifiableSet(super.getSources());
  }

  ValueTableState getValueTableState() {
    return (ValueTableState) getDatasource().getSessionFactory().getCurrentSession()
        .get(ValueTableState.class, valueTableId);
  }

  ValueTableState getValueTableState(LockMode lock) {
    return (ValueTableState) getDatasource().getSessionFactory().getCurrentSession()
        .get(ValueTableState.class, valueTableId, new LockOptions(lock));
  }

  HibernateMarshallingContext createContext() {
    return getDatasource().createContext(getValueTableState());
  }

  void commitEntities(Collection<VariableEntity> newEntities) {
    variableEntityProvider.entities.addAll(newEntities);
  }

  void commitSources(Collection<VariableValueSource> uncommittedSources) {
    addVariableValueSources(uncommittedSources);
  }

  void commitRemovedSources(Iterable<VariableValueSource> uncommittedRemovedSources) {
    removeVariableValueSources(uncommittedRemovedSources);
  }

  VariableState getVariableState(Variable variable) {
    HibernateVariableValueSource variableValueSource = (HibernateVariableValueSource) getVariableValueSource(
        variable.getName());
    return variableValueSource.getVariableState();
  }

  private void readVariables() {
    log.debug("Populating variable cache for table {}", getName());
    VariableValueSourceFactory factory = new HibernateVariableValueSourceFactory(this);
    addVariableValueSources(factory.createSources());
    log.debug("Populating variable cache - done. {} variables loaded", super.getSources().size());
  }

  Serializable getValueTableId() {
    return valueTableId;
  }

  class HibernateValueSet extends ValueSetBean {

    private final Criteria valueSetCriteria;

    private ValueSetState valueSetState;

    HibernateValueSet(VariableEntity entity, Criteria valueSetCriteria) {
      super(HibernateValueTable.this, entity);
      this.valueSetCriteria = valueSetCriteria;
    }

    synchronized ValueSetState getValueSetState() {
      if(valueSetState == null) {
        valueSetState = (ValueSetState) valueSetCriteria.uniqueResult();
        if(valueSetState != null) {
          // this is important when copying from a HibernateDatasource. Otherwise, they accumulate in the session and
          // make flushing longer and longer.
          getDatasource().getSessionFactory().getCurrentSession().evict(valueSetState);
        } else {
          throw new NoSuchValueSetException(getValueTable(), getVariableEntity());
        }
      }
      return valueSetState;
    }

    @NotNull
    @Override
    public Timestamps getTimestamps() {
      return createTimestamps(getValueSetState());
    }
  }

  void refreshEntityProvider() {
    ((HibernateVariableEntityProvider)getVariableEntityProvider()).initialise();
  }

  public class HibernateVariableEntityProvider extends AbstractVariableEntityProvider implements Initialisable {

    private final Set<VariableEntity> entities = new LinkedHashSet<>();

    public HibernateVariableEntityProvider(String entityType) {
      super(entityType);
    }

    @Override
    public void initialise() {
      log.debug("Populating entity cache for table {}", getName());
      entities.clear();
      // get the variable entities that have a value set in the table
      AssociationCriteria criteria = AssociationCriteria
          .create(ValueSetState.class, getDatasource().getSessionFactory().getCurrentSession())
          .add("valueTable.id", Operation.eq, valueTableId);
      for(Object obj : criteria.list()) {
        VariableEntity entity = ((ValueSetState) obj).getVariableEntity();
        entities.add(new VariableEntityBean(entity.getType(), entity.getIdentifier()));
      }
      log.debug("Populating entity cache - done. {} entities loaded.", entities.size());
    }

    /**
     * Returns the set of entities in this table. Will also include uncommitted entities when a transaction is active
     * for this table in the current session.
     */
    @NotNull
    @Override
    public Set<VariableEntity> getVariableEntities() {
      //TODO cache these entities instead of recreating an ImmutableSet each time
      if(getDatasource().hasTableTransaction(getName())) {
        return ImmutableSet.copyOf(
            Iterables.concat(entities, getDatasource().getTableTransaction(getName()).getUncommittedEntities()));
      }
      return Collections.unmodifiableSet(entities);
    }

    public void remove(VariableEntity entity) {
      entities.remove(entity);
    }
  }

  private class TimestampsIterator implements Iterator<Timestamps> {
    private final ScrollableResults results;

    private boolean hasNextResults;

    private boolean closed;

    private final Iterator<VariableEntity> entities;

    private final Map<String, Timestamps> timestampsMap = Maps.newHashMap();

    private TimestampsIterator(Iterator<VariableEntity> entities) {
      this.entities = entities;
      Query query = getCurrentSession().getNamedQuery("findValueSetTimestampsByTableId") //
          .setParameter("valueTableId", getValueTableState().getId());
      results = query.scroll(ScrollMode.FORWARD_ONLY);
      hasNextResults = results.next();
    }

    private Session getCurrentSession() {
      return getDatasource().getSessionFactory().getCurrentSession();
    }

    @Override
    public boolean hasNext() {
      return entities.hasNext();
    }

    @Override
    public Timestamps next() {
      VariableEntity entity = entities.next();

      if(timestampsMap.containsKey(entity.getIdentifier())) return getTimestampsFromMap(entity);

      boolean found = false;
      // Scroll until we find the required entity or reach the end of the results
      while(hasNextResults && !found) {
        String id = results.getString(0);
        Value created = DateTimeType.get().valueOf(results.getDate(1));
        Value updated = DateTimeType.get().valueOf(results.getDate(2));

        timestampsMap.put(id, new TimestampsBean(created, updated));
        if(entity.getIdentifier().equals(id)) {
          found = true;
        }
        hasNextResults = results.next();
      }

      closeCursorIfNecessary();

      if(timestampsMap.containsKey(entity.getIdentifier())) return getTimestampsFromMap(entity);
      return NullTimestamps.get();
    }

    @Override
    public void remove() {
      throw new UnsupportedOperationException();
    }

    /**
     * No duplicate of entities, so remove value from map once get.
     *
     * @param entity
     * @return
     */
    private Timestamps getTimestampsFromMap(VariableEntity entity) {
      Timestamps value = timestampsMap.get(entity.getIdentifier());
      timestampsMap.remove(entity.getIdentifier());
      return value;
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
