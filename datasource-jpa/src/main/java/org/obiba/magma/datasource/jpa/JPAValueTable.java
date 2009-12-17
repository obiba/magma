package org.obiba.magma.datasource.jpa;

import java.io.Serializable;
import java.util.LinkedHashSet;
import java.util.Set;

import org.obiba.core.service.impl.hibernate.AssociationCriteria;
import org.obiba.core.service.impl.hibernate.AssociationCriteria.Operation;
import org.obiba.magma.Initialisable;
import org.obiba.magma.NoSuchValueSetException;
import org.obiba.magma.ValueSet;
import org.obiba.magma.VariableEntity;
import org.obiba.magma.datasource.jpa.domain.ValueSetState;
import org.obiba.magma.datasource.jpa.domain.ValueTableState;
import org.obiba.magma.support.AbstractValueTable;
import org.obiba.magma.support.AbstractVariableEntityProvider;
import org.obiba.magma.support.ValueSetBean;
import org.obiba.magma.support.VariableEntityBean;

public class JPAValueTable extends AbstractValueTable {

  private Serializable valueTableId;

  private String entityType;

  public JPAValueTable(JPADatasource datasource, ValueTableState state) {
    super(datasource, state.getName());
    this.valueTableId = state.getId();
    this.entityType = state.getEntityType();
  }

  @Override
  public void initialise() {
    super.initialise();
    JPAVariableEntityProvider provider = new JPAVariableEntityProvider();
    provider.initialise();
    setVariableEntityProvider(provider);
  }

  @Override
  public JPADatasource getDatasource() {
    return (JPADatasource) super.getDatasource();
  }

  @Override
  public ValueSet getValueSet(VariableEntity entity) throws NoSuchValueSetException {
    AssociationCriteria criteria = AssociationCriteria.create(ValueSetState.class, getDatasource().getSessionFactory().getCurrentSession())
    // 
    .add("valueTable.id", Operation.eq, valueTableId)
    // 
    .add("variableEntity.identifier", Operation.eq, entity.getIdentifier()).add("variableEntity.type", Operation.eq, entity.getType());

    ValueSetState state = (ValueSetState) criteria.getCriteria().uniqueResult();
    if(state != null) {
      return new JPAValueSet(entity, state);
    }
    throw new NoSuchValueSetException(this, entity);
  }

  ValueTableState getValueTableState() {
    return (ValueTableState) this.getDatasource().getSessionFactory().getCurrentSession().get(ValueTableState.class, valueTableId);
  }

  class JPAValueSet extends ValueSetBean {
    public JPAValueSet(VariableEntity entity, ValueSetState state) {
      super(JPAValueTable.this, entity);
    }
  }

  public class JPAVariableEntityProvider extends AbstractVariableEntityProvider implements Initialisable {

    private Set<VariableEntity> entities;

    public JPAVariableEntityProvider() {
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
      return entities;
    }

  }

}
