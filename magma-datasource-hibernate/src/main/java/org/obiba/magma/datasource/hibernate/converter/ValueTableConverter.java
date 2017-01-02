/*
 * Copyright (c) 2017 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.magma.datasource.hibernate.converter;

import org.obiba.core.service.impl.hibernate.AssociationCriteria;
import org.obiba.core.service.impl.hibernate.AssociationCriteria.Operation;
import org.obiba.magma.ValueTable;
import org.obiba.magma.datasource.hibernate.domain.ValueTableState;

public class ValueTableConverter implements HibernateConverter<ValueTableState, ValueTable> {
  //
  // HibernateConverter Methods
  //

  private ValueTableConverter() {
  }

  @Override
  public ValueTableState marshal(ValueTable valueTable, HibernateMarshallingContext context) {
    AssociationCriteria criteria = AssociationCriteria
        .create(ValueTableState.class, context.getSessionFactory().getCurrentSession()) //
        .add("name", Operation.eq, valueTable.getName()) //
        .add("datasource", Operation.eq, context.getDatasourceState());
    ValueTableState valueTableState = (ValueTableState) criteria.getCriteria().uniqueResult();
    if(valueTableState == null) {
      valueTableState = new ValueTableState(valueTable.getName(), valueTable.getEntityType(),
          context.getDatasourceState());
      context.getSessionFactory().getCurrentSession().save(valueTableState);
      context.getSessionFactory().getCurrentSession().refresh(valueTableState); //OPAL-2635
    }

    return valueTableState;
  }

  @Override
  public ValueTable unmarshal(ValueTableState valueTableState, HibernateMarshallingContext context) {
    // TODO: Implement ValueTableConverter unmarshal method.
    throw new UnsupportedOperationException("ValueTableConverter unmarshal method not supported");
  }

  //
  // Methods
  //

  public static ValueTableConverter getInstance() {
    return new ValueTableConverter();
  }
}
