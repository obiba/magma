package org.obiba.magma.xstream;

import org.obiba.magma.Value;

import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

public class XStreamValueSetValue {

  @XStreamAsAttribute
  private String variable;

  private Value value;

  XStreamValueSetValue(String variable, Value value) {
    this.variable = variable;
    this.value = value;
  }
}
