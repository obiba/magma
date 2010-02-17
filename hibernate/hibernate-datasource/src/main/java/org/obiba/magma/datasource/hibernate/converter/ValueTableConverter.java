package org.obiba.magma.datasource.hibernate.converter;

import org.obiba.core.service.impl.hibernate.AssociationCriteria;
import org.obiba.core.service.impl.hibernate.AssociationCriteria.Operation;
import org.obiba.magma.ValueTable;
import org.obiba.magma.datasource.hibernate.domain.ValueTableState;

public class ValueTableConverter implements HibernateConverter<ValueTableState, ValueTable> {
  //
  // HibernateConverter Methods
  //

  @Override
  public ValueTableState marshal(ValueTable valueTable, HibernateMarshallingContext context) {
    AssociationCriteria criteria = AssociationCriteria.create(ValueTableState.class, context.getSessionFactory().getCurrentSession()).add("name", Operation.eq, valueTable.getName()).add("datasource", Operation.eq, context.getDatasourceState());
    ValueTableState valueTableState = (ValueTableState) criteria.getCriteria().uniqueResult();
    if(valueTableState == null) {
      valueTableState = new ValueTableState(valueTable.getName(), valueTable.getEntityType(), context.getDatasourceState());
      context.getSessionFactory().getCurrentSession().save(valueTableState);
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
