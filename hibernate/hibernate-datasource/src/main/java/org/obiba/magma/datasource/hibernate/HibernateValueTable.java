package org.obiba.magma.datasource.hibernate;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.annotation.Nullable;

import org.hibernate.Criteria;
import org.hibernate.FetchMode;
import org.hibernate.LockMode;
import org.hibernate.LockOptions;
import org.obiba.core.service.impl.hibernate.AssociationCriteria;
import org.obiba.core.service.impl.hibernate.AssociationCriteria.Operation;
import org.obiba.magma.Datasource;
import org.obiba.magma.Initialisable;
import org.obiba.magma.NoSuchValueSetException;
import org.obiba.magma.Timestamps;
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
import org.obiba.magma.support.AbstractValueTable;
import org.obiba.magma.support.AbstractVariableEntityProvider;
import org.obiba.magma.support.NullTimestamps;
import org.obiba.magma.support.ValueSetBean;
import org.obiba.magma.support.VariableEntityBean;
import org.obiba.magma.type.DateTimeType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;

@SuppressWarnings("OverlyCoupledClass")
class HibernateValueTable extends AbstractValueTable {

  private static final Logger log = LoggerFactory.getLogger(HibernateValueTable.class);

  private final Serializable valueTableId;

  private final HibernateVariableEntityProvider variableEntityProvider;

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
  public Timestamps getValueSetTimestamps(VariableEntity entity) throws NoSuchValueSetException {
    if(!hasValueSet(entity)) {
      throw new NoSuchValueSetException(this, entity);
    }

    Timestamped valueSetState = (Timestamped) AssociationCriteria
        .create(ValueSetState.class, getDatasource().getSessionFactory().getCurrentSession()) //
        .add("valueTable.id", Operation.eq, valueTableId) //
        .add("variableEntity.identifier", Operation.eq, entity.getIdentifier()) //
        .add("variableEntity.type", Operation.eq, entity.getType()) //
        .getCriteria() //
        .uniqueResult();

    return createTimestamps(valueSetState);
  }

  @Override
  public Timestamps getTimestamps() {
    return createTimestamps(getValueTableState());
  }

  private static Timestamps createTimestamps(@Nullable final Timestamped timestamped) {
    return timestamped == null ? NullTimestamps.get() : new Timestamps() {

      @Override
      public Value getLastUpdate() {
        return DateTimeType.get().valueOf(timestamped.getUpdated());
      }

      @Override
      public Value getCreated() {
        return DateTimeType.get().valueOf(timestamped.getCreated());
      }
    };
  }

  /**
   * Overridden to include uncommitted sources when a transaction exists on this table and is visible in the current
   * session.
   */
  @SuppressWarnings("IfMayBeConditional")
  @Override
  protected Set<VariableValueSource> getSources() {
    if(getDatasource().hasTableTransaction(getName())) {
      return new ImmutableSet.Builder<VariableValueSource>().addAll(super.getSources())
          .addAll(getDatasource().getTableTransaction(getName()).getUncommittedSources()).build();
    } else {
      return Collections.unmodifiableSet(super.getSources());
    }
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

  @SuppressWarnings("UnusedDeclaration")
  Serializable getVariableId(Variable variable) {
    return ((HibernateVariableValueSource) getVariableValueSource(variable.getName())).getVariableId();
  }

  private void readVariables() {
    log.debug("Populating variable cache for table {}", getName());
    VariableValueSourceFactory factory = new HibernateVariableValueSourceFactory(this);
    addVariableValueSources(factory.createSources());
    log.debug("Populating variable cache - done. {} variables loaded", super.getSources().size());
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

    @Override
    public Timestamps getTimestamps() {
      return createTimestamps(getValueSetState());
    }
  }

  public class HibernateVariableEntityProvider extends AbstractVariableEntityProvider implements Initialisable {

    private final Set<VariableEntity> entities = new LinkedHashSet<VariableEntity>();

    public HibernateVariableEntityProvider(String entityType) {
      super(entityType);
    }

    @Override
    public void initialise() {
      log.debug("Populating entity cache for table {}", getName());
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
    @SuppressWarnings("IfMayBeConditional")
    @Override
    public Set<VariableEntity> getVariableEntities() {
      if(getDatasource().hasTableTransaction(getName())) {
        return ImmutableSet.copyOf(
            Iterables.concat(entities, getDatasource().getTableTransaction(getName()).getUncommittedEntities()));
      }
      return Collections.unmodifiableSet(entities);
    }
  }

}
