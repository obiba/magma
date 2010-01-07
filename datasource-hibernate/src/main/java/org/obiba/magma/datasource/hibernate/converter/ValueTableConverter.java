package org.obiba.magma.datasource.hibernate.converter;

import org.obiba.core.service.impl.hibernate.AssociationCriteria;
import org.obiba.core.service.impl.hibernate.AssociationCriteria.Operation;
import org.obiba.magma.datasource.hibernate.HibernateValueTable;
import org.obiba.magma.datasource.hibernate.domain.DatasourceState;
import org.obiba.magma.datasource.hibernate.domain.ValueTableState;

public class ValueTableConverter implements HibernateConverter<ValueTableState, HibernateValueTable> {
  //
  // HibernateConverter Methods
  //

  @Override
  public ValueTableState marshal(HibernateValueTable valueTable, HibernateMarshallingContext context) {
    valueTable.initialise();

    AssociationCriteria criteria = AssociationCriteria.create(ValueTableState.class, context.getSessionFactory().getCurrentSession()).add("name", Operation.eq, valueTable.getName()).add("entityType", Operation.eq, valueTable.getEntityType());
    ValueTableState valueTableState = (ValueTableState) criteria.getCriteria().uniqueResult();
    if(valueTableState == null) {
      DatasourceState datasourceState = (DatasourceState) context.getAttributeAwareEntity();
      valueTableState = new ValueTableState(valueTable.getName(), valueTable.getEntityType(), datasourceState);
      context.getSessionFactory().getCurrentSession().save(valueTableState);
    }

    return valueTableState;
  }

  @Override
  public HibernateValueTable unmarshal(ValueTableState valueTableState, HibernateMarshallingContext context) {
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
