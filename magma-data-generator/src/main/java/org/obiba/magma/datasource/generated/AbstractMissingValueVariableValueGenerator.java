package org.obiba.magma.datasource.generated;

import java.util.List;

import org.obiba.magma.Category;
import org.obiba.magma.Value;
import org.obiba.magma.Variable;
import org.obiba.magma.type.TextType;

import com.google.common.collect.Lists;

abstract class AbstractMissingValueVariableValueGenerator extends GeneratedVariableValueSource {

  private final int percentMissing;

  private final List<Category> missingCategories = Lists.newArrayList();

  AbstractMissingValueVariableValueGenerator(Variable variable) {
    super(variable);
    percentMissing = 1;
    for(Category c : variable.getCategories()) {
      if(c.isMissing()) {
        missingCategories.add(c);
      }
    }
  }

  @Override
  protected Value nextValue(Variable variable, GeneratedValueSet gvs) {
    boolean isMissing = missingCategories.size() > 0 && gvs.valueGenerator.nextInt(100) <= percentMissing;

    if(isMissing) {
      if(missingCategories.isEmpty()) return TextType.get().nullValue();
      int c = gvs.valueGenerator.nextInt(missingCategories.size());
      return variable.getValueType().valueOf(missingCategories.get(c).getName());
    }
    return nonMissingValue(variable, gvs);
  }

  protected abstract Value nonMissingValue(Variable variable, GeneratedValueSet gvs);
}
