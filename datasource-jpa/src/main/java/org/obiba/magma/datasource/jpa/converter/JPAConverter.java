package org.obiba.magma.datasource.jpa.converter;

public interface JPAConverter<T, E> {

  public T marshal(E magmaObject, JPAMarshallingContext context);

  public E unmarshal(T jpaObject, JPAMarshallingContext context);

}
