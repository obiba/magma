package org.obiba.magma.datasource.hibernate.converter;

public interface HibernateConverter<T, E> {

  T marshal(E magmaObject, HibernateMarshallingContext context);

  E unmarshal(T jpaObject, HibernateMarshallingContext context);

}
