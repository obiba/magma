package org.obiba.magma.datasource.generated;

import java.util.List;

import org.obiba.magma.Category;
import org.obiba.magma.Value;
import org.obiba.magma.Variable;
import org.obiba.magma.type.TextType;

import com.google.common.collect.Lists;

class CategoricalVariableValueGenerator extends GeneratedVariableValueSource {

  private final int percentMissing;

  private final List<Category> dataCategories = Lists.newArrayList();

  private final List<Category> missingCategories = Lists.newArrayList();

  CategoricalVariableValueGenerator(Variable variable) {
    super(variable);
    this.percentMissing = 1;
    for(Category c : variable.getCategories()) {
      if(c.isMissing()) {
        missingCategories.add(c);
      } else {
        dataCategories.add(c);
      }
    }
  }

  @Override
  protected Value nextValue(Variable variable, GeneratedValueSet gvs) {
    boolean isMissing = missingCategories.size() > 0 && gvs.valueGenerator.nextInt(100) <= percentMissing;

    List<Category> random = isMissing ? missingCategories : dataCategories;
    if(random.size() == 0) return TextType.get().nullValue();
    int c = gvs.valueGenerator.nextInt(random.size());
    return TextType.get().valueOf(random.get(c).getName());
  }
}
