package org.obiba.magma.filter;

import java.util.Objects;

import org.obiba.magma.Initialisable;
import org.obiba.magma.Value;
import org.obiba.magma.Variable;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamOmitField;

@XStreamAlias("variableAttribute")
public class VariableAttributeFilter extends AbstractFilter<Variable> implements Initialisable {

  @XStreamAlias("attribute")
  private final String attributeName;

  private final String value;

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
    if(attributeName == null || value == null)
      throw new IllegalArgumentException("The arguments [attribute] and [value] cannot be null.");
  }

  @Override
  protected Boolean runFilter(Variable item) {
    initialise();
    if(!item.hasAttribute(attributeName)) return false;
    Value attrValue = item.getAttribute(attributeName).getValue();
    return !attrValue.isNull() && Objects.equals(attrValue.toString(), value);
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
      value = attributeValue;
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
