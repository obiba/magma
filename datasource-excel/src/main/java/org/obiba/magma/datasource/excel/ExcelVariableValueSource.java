package org.obiba.magma.datasource.excel;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.obiba.magma.Value;
import org.obiba.magma.ValueSet;
import org.obiba.magma.ValueType;
import org.obiba.magma.Variable;
import org.obiba.magma.VariableValueSource;
import org.obiba.magma.VectorSource;

public class ExcelVariableValueSource implements VariableValueSource {

  private final Variable variable;

  public ExcelVariableValueSource(Variable variable) {
    this.variable = variable;
  }

  @Override
  public Variable getVariable() {
    return variable;
  }

  @Nonnull
  @Override
  public ValueType getValueType() {
    return variable.getValueType();
  }

  @Nullable
  @Override
  public VectorSource asVectorSource() {
    return null;
  }

  @Nonnull
  @Override
  public Value getValue(ValueSet valueSet) {
    throw new UnsupportedOperationException();
  }

}
