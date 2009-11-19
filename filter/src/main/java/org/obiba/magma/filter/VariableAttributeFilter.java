package org.obiba.magma.filter;

import org.obiba.magma.Attribute;
import org.obiba.magma.VariableValueSource;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("variableAttribute")
public class VariableAttributeFilter extends AbstractFilter<VariableValueSource> {

  @XStreamAlias("attribute")
  private String attributeName;

  private String value;

  public VariableAttributeFilter(String attributeName, String value) {
    this.attributeName = attributeName;
    this.value = value;
  }

  @Override
  boolean runFilter(VariableValueSource item) {
    for(Attribute attribute : item.getVariable().getAttributes()) {
      if(attribute.getName().equalsIgnoreCase(attributeName)) {
        if(attribute.getValue().getValue().equals(value)) return true;
      }
    }
    return false;
  }
}
