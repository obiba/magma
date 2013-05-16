package org.obiba.magma.datasource.hibernate.converter;

public interface HibernateConverter<TJpaObject, TMagmaObject> {

  TJpaObject marshal(TMagmaObject magmaObject, HibernateMarshallingContext context);

  TMagmaObject unmarshal(TJpaObject jpaObject, HibernateMarshallingContext context);

}
