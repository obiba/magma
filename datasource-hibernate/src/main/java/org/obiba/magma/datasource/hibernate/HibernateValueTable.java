package org.obiba.magma.datasource.hibernate;

import java.io.Serializable;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import org.obiba.core.service.impl.hibernate.AssociationCriteria;
import org.obiba.core.service.impl.hibernate.AssociationCriteria.Operation;
import org.obiba.magma.Initialisable;
import org.obiba.magma.NoSuchValueSetException;
import org.obiba.magma.ValueSet;
import org.obiba.magma.VariableEntity;
import org.obiba.magma.datasource.hibernate.domain.ValueSetState;
import org.obiba.magma.datasource.hibernate.domain.ValueTableState;
import org.obiba.magma.support.AbstractValueTable;
import org.obiba.magma.support.AbstractVariableEntityProvider;
import org.obiba.magma.support.ValueSetBean;
import org.obiba.magma.support.VariableEntityBean;

public class HibernateValueTable extends AbstractValueTable {

  private Serializable valueTableId;

  private String entityType;

  public HibernateValueTable(HibernateDatasource datasource, ValueTableState state) {
    super(datasource, state.getName());
    this.valueTableId = state.getId();
    this.entityType = state.getEntityType();
  }

  public HibernateValueTable(HibernateDatasource datasource, String tableName, String entityType) {
    super(datasource, tableName);
    this.entityType = entityType;
  }

  @Override
  public void initialise() {
    super.initialise();
    HibernateVariableEntityProvider provider = new HibernateVariableEntityProvider();
    provider.initialise();
    setVariableEntityProvider(provider);
  }

  @Override
  public HibernateDatasource getDatasource() {
    return (HibernateDatasource) super.getDatasource();
  }

  @Override
  public ValueSet getValueSet(VariableEntity entity) throws NoSuchValueSetException {
    AssociationCriteria criteria = AssociationCriteria.create(ValueSetState.class, getDatasource().getSessionFactory().getCurrentSession())
    // 
    .add("valueTable", Operation.eq, getValueTableState())
    // 
    .add("variableEntity.identifier", Operation.eq, entity.getIdentifier()).add("variableEntity.type", Operation.eq, entity.getType());

    ValueSetState state = (ValueSetState) criteria.getCriteria().uniqueResult();
    if(state != null) {
      return new HibernateValueSet(entity, state);
    }
    throw new NoSuchValueSetException(this, entity);
  }

  ValueTableState getValueTableState() {
    return (ValueTableState) this.getDatasource().getSessionFactory().getCurrentSession().get(ValueTableState.class, valueTableId);
  }

  class HibernateValueSet extends ValueSetBean {

    private ValueSetState valueSetState;

    public HibernateValueSet(VariableEntity entity, ValueSetState state) {
      super(HibernateValueTable.this, entity);
    }

    ValueSetState getValueSetState() {
      return valueSetState;
    }
  }

  public class HibernateVariableEntityProvider extends AbstractVariableEntityProvider implements Initialisable {

    private Set<VariableEntity> entities;

    public HibernateVariableEntityProvider() {
      super(entityType);
    }

    @Override
    public void initialise() {
      entities = new LinkedHashSet<VariableEntity>();
      // get the variable entities that have a value set in the table
      AssociationCriteria criteria = AssociationCriteria.create(ValueSetState.class, getDatasource().getSessionFactory().getCurrentSession()).add("valueTable.id", Operation.eq, valueTableId);
      for(Object obj : criteria.list()) {
        VariableEntity entity = ((ValueSetState) obj).getVariableEntity();
        entities.add(new VariableEntityBean(entity.getType(), entity.getIdentifier()));
      }
    }

    @Override
    public Set<VariableEntity> getVariableEntities() {
      return Collections.unmodifiableSet(entities);
    }

  }

}
