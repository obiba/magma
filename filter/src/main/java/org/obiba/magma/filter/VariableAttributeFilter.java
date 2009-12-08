package org.obiba.magma.filter;

import org.obiba.magma.Attribute;
import org.obiba.magma.Initialisable;
import org.obiba.magma.Variable;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamOmitField;

@XStreamAlias("variableAttribute")
public class VariableAttributeFilter extends AbstractFilter<Variable> implements Initialisable {

  @XStreamAlias("attribute")
  private String attributeName;

  private String value;

  @XStreamOmitField
  private boolean initialised;

  protected VariableAttributeFilter(String attributeName, String value) {
    this.attributeName = attributeName;
    this.value = value;
    initialise();
  }

  @Override
  public void initialise() {
    if(initialised) return;
    validateArguments(attributeName, value);
    initialised = true;
  }

  private void validateArguments(String attributeName, String value) {
    if(attributeName == null || value == null) throw new IllegalArgumentException("The arguments [attribute] and [value] cannot be null.");
  }

  @Override
  protected boolean runFilter(Variable item) {
    initialise();
    for(Attribute attribute : item.getAttributes()) {
      if(attribute.getName().equalsIgnoreCase(attributeName)) {
        if(attribute.getValue().getValue().equals(value)) return true;
      }
    }
    return false;
  }

  public static class Builder extends AbstractFilter.Builder {

    private String attributeName;

    private String value;

    public static Builder newFilter() {
      return new Builder();
    }

    public Builder attributeName(String attributeName) {
      this.attributeName = attributeName;
      return this;
    }

    public Builder attributeValue(String attributeValue) {
      this.value = attributeValue;
      return this;
    }

    public VariableAttributeFilter build() {
      VariableAttributeFilter filter = new VariableAttributeFilter(attributeName, value);
      filter.setType(type);
      return filter;
    }

    @Override
    public Builder exclude() {
      super.exclude();
      return this;
    }

    @Override
    public Builder include() {
      super.include();
      return this;
    }
  }

}
