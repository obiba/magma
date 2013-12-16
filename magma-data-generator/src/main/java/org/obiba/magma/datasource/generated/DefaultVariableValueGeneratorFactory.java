package org.obiba.magma.datasource.generated;

import javax.validation.constraints.NotNull;

import org.obiba.magma.Variable;
import org.obiba.magma.type.BinaryType;
import org.obiba.magma.type.TextType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultVariableValueGeneratorFactory implements VariableValueGeneratorFactory {

  private static final Logger log = LoggerFactory.getLogger(DefaultVariableValueGeneratorFactory.class);

  @Override
  public GeneratedVariableValueSource newGenerator(@NotNull Variable variable) {
    boolean isAllMissing = variable.areAllCategoriesMissing();
    if(variable.hasCategories() && !isAllMissing) {
      return new CategoricalValueGenerator(variable);
    }
    if(variable.getValueType().isNumeric() && isAllMissing) {
      return new NumericValueGenerator(variable);
    }
    if(variable.getValueType().isDateTime()) {
      return new DateValueGenerator(variable);
    }
    if(variable.getValueType().equals(BinaryType.get())) {
      return new BinaryValueGenerator(variable);
    }
    if(variable.getValueType().equals(TextType.get())) {
      return new TextValueGenerator(variable);
    }
    log.warn("{} is not supported by data generator for variable {}", variable.getValueType(), variable.getName());
    return new NullValueGenerator(variable);
  }

}
