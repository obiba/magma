package org.obiba.magma.support;

import java.util.Collections;
import java.util.SortedSet;

import javax.annotation.Nonnull;

import org.obiba.magma.Value;
import org.obiba.magma.ValueSet;
import org.obiba.magma.ValueSource;
import org.obiba.magma.ValueType;
import org.obiba.magma.VariableEntity;
import org.obiba.magma.VectorSource;

public final class NullValueSource implements ValueSource, VectorSource {

  private final ValueType valueType;

  public NullValueSource(ValueType valueType) {
    this.valueType = valueType;
  }

  @Nonnull
  @Override
  public ValueType getValueType() {
    return valueType;
  }

  @Nonnull
  @Override
  public Value getValue(ValueSet valueSet) {
    return valueType.nullValue();
  }

  @Override
  public VectorSource asVectorSource() {
    return this;
  }

  @Override
  public Iterable<Value> getValues(SortedSet<VariableEntity> entities) {
    return Collections.nCopies(entities.size(), valueType.nullValue());
  }

}