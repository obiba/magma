package org.obiba.magma.datasource.generated;

import org.obiba.magma.Category;
import org.obiba.magma.Variable;

public class DefaultVariableValueGeneratorFactory implements VariableValueGeneratorFactory {

  @Override
  public GeneratedVariableValueSource newGenerator(Variable variable) {
    if(variable.hasCategories() && !isAllMissing(variable.getCategories())) {
      return new CategoricalVariableValueGenerator(variable);
    }
    if(variable.getValueType().isNumeric() && isAllMissing(variable.getCategories())) {
      return new NumericVariableValueGenerator(variable);
    }
    if(variable.getValueType().isDateTime()) {
      return new DateVariableValueGenerator(variable);
    }
    return new NullVariableValueGenerator(variable);
  }

  private boolean isAllMissing(Iterable<Category> categories) {
    for(Category c : categories) {
      if(!c.isMissing()) return false;
    }
    return true;
  }

}
