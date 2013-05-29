package org.obiba.magma.datasource.generated;

import javax.annotation.Nonnull;

import org.obiba.magma.Variable;
import org.obiba.magma.type.BinaryType;

public class DefaultVariableValueGeneratorFactory implements VariableValueGeneratorFactory {

  @Override
  public GeneratedVariableValueSource newGenerator(@Nonnull Variable variable) {
    boolean isAllMissing = variable.areAllCategoriesMissing();
    if(variable.hasCategories() && !isAllMissing) {
      return new CategoricalVariableValueGenerator(variable);
    }
    if(variable.getValueType().isNumeric() && isAllMissing) {
      return new NumericVariableValueGenerator(variable);
    }
    if(variable.getValueType().isDateTime()) {
      return new DateVariableValueGenerator(variable);
    }
    if(variable.getValueType().equals(BinaryType.get())) {
      return new BinaryVariableValueGenerator(variable);
    }

    return new NullVariableValueGenerator(variable);
  }

}
