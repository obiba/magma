package org.obiba.magma.filter;

import org.obiba.magma.Attribute;
import org.obiba.magma.VariableValueSource;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("variableAttribute")
public class VariableAttributeFilter extends AbstractFilter<VariableValueSource> {

  @XStreamAlias("attribute")
  private String attributeName;

  private String value;

  protected VariableAttributeFilter(String attributeName, String value) {
    validateArguments(attributeName, value);
    this.attributeName = attributeName;
    this.value = value;
  }

  private void validateArguments(String attributeName, String value) {
    if(attributeName == null || value == null) throw new IllegalArgumentException("The arguments [attribute] and [value] cannot be null.");
  }

  @Override
  protected boolean runFilter(VariableValueSource item) {
    for(Attribute attribute : item.getVariable().getAttributes()) {
      if(attribute.getName().equalsIgnoreCase(attributeName)) {
        if(attribute.getValue().getValue().equals(value)) return true;
      }
    }
    return false;
  }

  private Object readResolve() {
    validateArguments(attributeName, value);
    validateType();
    return this;
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
      filter.validateType();
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
