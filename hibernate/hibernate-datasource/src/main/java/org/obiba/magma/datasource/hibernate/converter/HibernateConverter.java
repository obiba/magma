package org.obiba.magma.datasource.hibernate.converter;

public interface HibernateConverter<T, E> {

  public T marshal(E magmaObject, HibernateMarshallingContext context);

  public E unmarshal(T jpaObject, HibernateMarshallingContext context);

}
