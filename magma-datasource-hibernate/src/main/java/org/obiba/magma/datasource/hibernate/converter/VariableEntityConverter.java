/*
 * Copyright (c) 2016 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.magma.datasource.hibernate.converter;

import org.hibernate.criterion.Restrictions;
import org.obiba.magma.VariableEntity;
import org.obiba.magma.datasource.hibernate.domain.VariableEntityState;
import org.obiba.magma.support.VariableEntityBean;

public class VariableEntityConverter implements HibernateConverter<VariableEntityState, VariableEntity> {
  //
  // HibernateConverter Methods
  //

  @Override
  public VariableEntityState marshal(VariableEntity variableEntity, HibernateMarshallingContext context) {
    if(variableEntity == null) throw new IllegalArgumentException("variableEntity cannot be null");
    if(context == null) throw new IllegalArgumentException("context cannot be null");

    VariableEntityState variableEntityState = (VariableEntityState) context.getSessionFactory().getCurrentSession()
        .createCriteria(VariableEntityState.class).add(Restrictions.eq("identifier", variableEntity.getIdentifier()))
        .add(Restrictions.eq("type", variableEntity.getType())).uniqueResult();
    if(variableEntityState == null) {
      variableEntityState = new VariableEntityState(variableEntity.getIdentifier(), variableEntity.getType());
      context.getSessionFactory().getCurrentSession().save(variableEntityState);
      context.getSessionFactory().getCurrentSession().refresh(variableEntityState); //OPAL-2635
    }

    return variableEntityState;
  }

  @Override
  public VariableEntity unmarshal(VariableEntityState variableEntityState, HibernateMarshallingContext context) {
    return new VariableEntityBean(variableEntityState.getType(), variableEntityState.getIdentifier());
  }

}
