/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.magma.datasource.hibernate;

import org.hibernate.FetchMode;
import org.obiba.core.service.impl.hibernate.AssociationCriteria;
import org.obiba.magma.NoSuchValueSetException;
import org.obiba.magma.VariableEntity;
import org.obiba.magma.datasource.hibernate.domain.ValueSetState;

import java.util.List;

public class HibernateValueSetFetcher {

  private final HibernateValueTable table;

  public HibernateValueSetFetcher(HibernateValueTable table) {
    this.table = table;
  }

  ValueSetState getValueSetState(VariableEntity entity) {
    AssociationCriteria criteria = AssociationCriteria
        .create(ValueSetState.class, table.getDatasource().getSessionFactory().getCurrentSession())
        .add("valueTable.id", AssociationCriteria.Operation.eq, table.getValueTableId())
        .add("variableEntity.identifier", AssociationCriteria.Operation.eq, entity.getIdentifier())
        .add("variableEntity.type", AssociationCriteria.Operation.eq, table.getEntityType());
    ValueSetState valueSetState = (ValueSetState) criteria.getCriteria().setFetchMode("values", FetchMode.JOIN).uniqueResult();
    if (valueSetState != null) {
      // this is important when copying from a HibernateDatasource. Otherwise, they accumulate in the session and
      // make flushing longer and longer.
      table.getDatasource().getSessionFactory().getCurrentSession().evict(valueSetState);
    } else {
      throw new NoSuchValueSetException(table, entity);
    }
    return valueSetState;
  }

  List<ValueSetState> getValueSetStates(List<VariableEntity> entities) {
    AssociationCriteria criteria = AssociationCriteria
        .create(ValueSetState.class, table.getDatasource().getSessionFactory().getCurrentSession())
        .add("valueTable.id", AssociationCriteria.Operation.eq, table.getValueTableId())
        .add("variableEntity.identifier", AssociationCriteria.Operation.in, entities.stream().map(VariableEntity::getIdentifier).toArray())
        .add("variableEntity.type", AssociationCriteria.Operation.eq, table.getEntityType());
    List<ValueSetState> valueSetStates = criteria.getCriteria().setFetchMode("values", FetchMode.JOIN).list();
    valueSetStates.forEach(vss -> table.getDatasource().getSessionFactory().getCurrentSession().evict(vss));
    return valueSetStates;
  }
}
