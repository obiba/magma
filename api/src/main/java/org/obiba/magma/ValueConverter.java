package org.obiba.magma;

public interface ValueConverter {

  public boolean converts(ValueType from, ValueType to);

  public Value convert(Value value, ValueType to);

}
