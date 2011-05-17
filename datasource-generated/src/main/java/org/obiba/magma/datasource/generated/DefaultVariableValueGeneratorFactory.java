package org.obiba.magma.datasource.generated;

import org.obiba.magma.Category;
import org.obiba.magma.Variable;

public class DefaultVariableValueGeneratorFactory implements VariableValueGeneratorFactory {

  @Override
  public GeneratedVariableValueSource newGenerator(Variable variable) {
    if(variable.hasCategories() && isAllMissing(variable.getCategories()) == false) {
      return new CategoricalVariableValueGenerator(variable);
    } else if(variable.getValueType().isNumeric() && isAllMissing(variable.getCategories()) == true) {
      return new NumericVariableValueGenerator(variable);
    } else if(variable.getValueType().isDateTime()) {
      return new DateVariableValueGenerator(variable);
    }
    return new NullVariableValueGenerator(variable);
  }

  private boolean isAllMissing(Iterable<Category> categories) {
    for(Category c : categories) {
      if(c.isMissing() == false) return false;
    }
    return true;
  }

}
