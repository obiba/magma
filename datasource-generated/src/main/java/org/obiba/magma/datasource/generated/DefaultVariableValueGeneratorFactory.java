package org.obiba.magma.datasource.generated;

import org.obiba.magma.Variable;

public class DefaultVariableValueGeneratorFactory implements VariableValueGeneratorFactory {

  @Override
  public GeneratedVariableValueSource newGenerator(Variable variable) {
    if(variable.hasCategories()) {
      return new CategoricalVariableValueGenerator(variable);
    } else if(variable.getValueType().isNumeric()) {
      return new NumericVariableValueGenerator(variable);
    }
    return new NullVariableValueGenerator(variable);
  }

}
