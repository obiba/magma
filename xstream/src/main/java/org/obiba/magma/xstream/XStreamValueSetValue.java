package org.obiba.magma.xstream;

import org.obiba.magma.Value;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

@XStreamAlias(value = "variableValue")
public class XStreamValueSetValue {

  @XStreamAsAttribute
  private String variable;

  private Value value;

  public XStreamValueSetValue(String variable, Value value) {
    this.variable = variable;
    this.value = value;
  }
}
