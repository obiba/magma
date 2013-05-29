package org.obiba.magma.datasource.generated;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.obiba.magma.Value;
import org.obiba.magma.ValueSet;
import org.obiba.magma.ValueSource;
import org.obiba.magma.ValueType;
import org.obiba.magma.Variable;
import org.obiba.magma.VariableValueSource;
import org.obiba.magma.VectorSource;
import org.obiba.magma.js.JavascriptValueSource;
import org.obiba.magma.support.Initialisables;
import org.obiba.magma.type.BooleanType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

abstract class GeneratedVariableValueSource implements VariableValueSource {

  private final static Logger log = LoggerFactory.getLogger(GeneratedVariableValueSource.class);

  private final Variable variable;

  private final ValueSource condition;

  protected GeneratedVariableValueSource(Variable variable) {
    this.variable = variable;
    if(variable.hasAttribute("condition")) {
      JavascriptValueSource src = new JavascriptValueSource(BooleanType.get(),
          variable.getAttributeStringValue("condition"));
      try {
        Initialisables.initialise(src);
      } catch(RuntimeException e) {
        log.warn("Cannot compile condition for variable {}", variable.getName());
        //noinspection AssignmentToNull
        src = null;
      }
      condition = src;
    } else {
      condition = null;
    }
  }

  @Override
  public Variable getVariable() {
    return variable;
  }

  @Nullable
  @Override
  public VectorSource asVectorSource() {
    return null;
  }

  @Nonnull
  @Override
  public Value getValue(ValueSet valueSet) {
    if(shouldGenerate(valueSet)) {
      GeneratedValueSet gvs = (GeneratedValueSet) valueSet;
      try {
        Value existingValue = gvs.getExistingValue(getVariable().getName());
        return existingValue == null ? gvs.put(getVariable().getName(), nextValue(getVariable(), gvs)) : existingValue;
      } catch(RuntimeException e) {
        //noinspection StringConcatenationArgumentToLogCall
        log.warn("Error generating data for variable " + getVariable().getName(), e);
        return getValueType().nullValue();
      }
    }
    return getValueType().nullValue();
  }

  @Nonnull
  @Override
  public ValueType getValueType() {
    return getVariable().getValueType();
  }

  protected boolean shouldGenerate(ValueSet valueSet) {
    if(condition != null) {
      try {
        return (Boolean) condition.getValue(valueSet).getValue();
      } catch(RuntimeException e) {
        log.warn("Error evaluating condition for variable {}", getVariable().getName());
      }
    }
    return true;
  }

  abstract protected Value nextValue(Variable variable, GeneratedValueSet gvs);

}
