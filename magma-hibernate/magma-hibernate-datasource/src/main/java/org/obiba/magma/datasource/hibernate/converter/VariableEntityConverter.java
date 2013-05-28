package org.obiba.magma.datasource.hibernate.converter;

import javax.annotation.Nonnull;

import org.hibernate.criterion.Restrictions;
import org.obiba.magma.VariableEntity;
import org.obiba.magma.datasource.hibernate.domain.VariableEntityState;
import org.obiba.magma.support.VariableEntityBean;

import static org.springframework.util.Assert.notNull;

public class VariableEntityConverter implements HibernateConverter<VariableEntityState, VariableEntity> {
  //
  // HibernateConverter Methods
  //

  @Override
  public VariableEntityState marshal(@Nonnull VariableEntity variableEntity,
      @Nonnull HibernateMarshallingContext context) {
    notNull(variableEntity, "variableEntity cannot be null");
    notNull(context, "context cannot be null");

    VariableEntityState variableEntityState = (VariableEntityState) context.getSessionFactory().getCurrentSession()
        .createCriteria(VariableEntityState.class).add(Restrictions.eq("identifier", variableEntity.getIdentifier()))
        .add(Restrictions.eq("type", variableEntity.getType())).uniqueResult();
    if(variableEntityState == null) {
      variableEntityState = new VariableEntityState(variableEntity.getIdentifier(), variableEntity.getType());
      context.getSessionFactory().getCurrentSession().save(variableEntityState);
    }

    return variableEntityState;
  }

  @Override
  public VariableEntity unmarshal(VariableEntityState variableEntityState, HibernateMarshallingContext context) {
    return new VariableEntityBean(variableEntityState.getType(), variableEntityState.getIdentifier());
  }

}
