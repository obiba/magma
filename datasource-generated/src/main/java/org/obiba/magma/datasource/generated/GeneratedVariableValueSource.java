package org.obiba.magma.datasource.generated;

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
      condition = new JavascriptValueSource(BooleanType.get(), variable.getAttributeStringValue("condition"));
      Initialisables.initialise(condition);
    } else {
      condition = null;
    }
  }

  @Override
  public Variable getVariable() {
    return variable;
  }

  @Override
  public VectorSource asVectorSource() {
    return null;
  }

  @Override
  public Value getValue(ValueSet valueSet) {
    if(shouldGenerate(valueSet)) {
      GeneratedValueSet gvs = (GeneratedValueSet) valueSet;
      try {
        Value existingValue = gvs.getExistingValue(getVariable().getName());
        return existingValue != null ? existingValue : gvs.putIfAbsent(getVariable().getName(), nextValue(getVariable(), gvs));
      } catch(RuntimeException e) {
        log.info("Error generating data for variable {}: {}", getVariable().getName(), e);
        return getValueType().nullValue();
      }
    }
    return getValueType().nullValue();
  }

  @Override
  public ValueType getValueType() {
    return getVariable().getValueType();
  }

  protected boolean shouldGenerate(ValueSet valueSet) {
    if(condition != null) {
      return (Boolean) condition.getValue(valueSet).getValue();
    }
    return true;
  }

  abstract protected Value nextValue(Variable variable, GeneratedValueSet gvs);

}
