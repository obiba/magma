package org.obiba.magma.datasource.generated;

import java.util.List;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

import org.obiba.magma.AttributeAware;
import org.obiba.magma.Category;
import org.obiba.magma.Value;
import org.obiba.magma.ValueSource;
import org.obiba.magma.ValueType;
import org.obiba.magma.Variable;
import org.obiba.magma.js.JavascriptValueSource;
import org.obiba.magma.support.NullValueSource;
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

  @NotNull
  protected ValueSource makeSource(AttributeAware attributeAware, ValueType type, String... scriptAttributes) {
    String script = getAttributeStringValue(attributeAware, scriptAttributes);
    return script == null ? new NullValueSource(type) : new JavascriptValueSource(type, script);
  }

  @Nullable
  protected String getAttributeStringValue(AttributeAware attributeAware, String... scriptAttributes) {
    for(String scriptAttribute : scriptAttributes) {
      if(attributeAware.hasAttribute(scriptAttribute)) {
        return attributeAware.getAttributeStringValue(scriptAttribute);
      }
    }
    return null;
  }
}
