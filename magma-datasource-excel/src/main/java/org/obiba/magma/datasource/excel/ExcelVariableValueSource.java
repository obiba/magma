package org.obiba.magma.datasource.excel;

import javax.validation.constraints.NotNull;

import org.obiba.magma.AbstractVariableValueSource;
import org.obiba.magma.Value;
import org.obiba.magma.ValueSet;
import org.obiba.magma.ValueType;
import org.obiba.magma.Variable;
import org.obiba.magma.VariableValueSource;
import org.obiba.magma.VectorSource;
import org.obiba.magma.VectorSourceNotSupportedException;

public class ExcelVariableValueSource extends AbstractVariableValueSource implements VariableValueSource {

  private final Variable variable;

  public ExcelVariableValueSource(Variable variable) {
    this.variable = variable;
  }

  @NotNull
  @Override
  public Variable getVariable() {
    return variable;
  }

  @NotNull
  @Override
  public ValueType getValueType() {
    return variable.getValueType();
  }

  @Override
  public boolean supportVectorSource() {
    return false;
  }

  @NotNull
  @Override
  public VectorSource asVectorSource() {
    throw new VectorSourceNotSupportedException(getClass());
  }

  @NotNull
  @Override
  public Value getValue(ValueSet valueSet) {
    throw new UnsupportedOperationException();
  }

}
