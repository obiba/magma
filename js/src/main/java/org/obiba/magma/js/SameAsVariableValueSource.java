package org.obiba.magma.js;

import org.obiba.magma.Attribute;
import org.obiba.magma.Category;
import org.obiba.magma.NoSuchAttributeException;
import org.obiba.magma.ValueTable;
import org.obiba.magma.Variable;

public class SameAsVariableValueSource extends JavascriptVariableValueSource {

  public static final String SAME_AS_ATTRIBUTE_NAME = "sameAs";

  private final ValueTable valueTable;

  public SameAsVariableValueSource(Variable variable, ValueTable valueTable) {
    super(variable);
    if(valueTable == null) throw new IllegalArgumentException("valueTable cannot be null");
    this.valueTable = valueTable;
  }

  @Override
  public String getScript() {
    try {
      return super.getScript();
    } catch(NoSuchAttributeException e) {
      return new StringBuilder().append("$('").append(getSameAs()).append("')").toString();
    }
  }

  public String getSameAs() {
    return super.getVariable().getAttribute(SAME_AS_ATTRIBUTE_NAME).getValue().toString();
  }

  @Override
  public Variable getVariable() {
    Variable original = valueTable.getVariable(getSameAs());
    Variable derived = super.getVariable();
    Variable.Builder builder = Variable.Builder.sameAs(original);
    overrideVariables(builder, derived);
    if(overrideAttributes()) {
      builder.clearAttributes();
      for(Attribute attribute : derived.getAttributes()) {
        builder.addAttribute(attribute);
      }
    } else {
      builder.addAttribute(derived.getAttribute(SAME_AS_ATTRIBUTE_NAME));
    }
    if(overrideCategories()) {
      builder.clearCategories();
      for(Category category : derived.getCategories()) {
        builder.addCategory(category);
      }
    }
    return builder.build();
  }

  private Variable.Builder overrideVariables(Variable.Builder builder, Variable override) {
    builder.name(override.getName());
    builder.mimeType(override.getMimeType());
    builder.referencedEntityType(override.getEntityType());
    builder.unit(override.getUnit());
    builder.occurrenceGroup(override.getOccurrenceGroup());
    return builder;
  }

  private boolean overrideAttributes() {
    return super.getVariable().getAttributes().size() > 1;
  }

  private boolean overrideCategories() {
    return super.getVariable().getCategories().size() > 0;
  }

}
