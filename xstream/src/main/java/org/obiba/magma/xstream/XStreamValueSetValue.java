package org.obiba.magma.xstream;

import org.obiba.magma.Value;

import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

public class XStreamValueSetValue {

  @XStreamAsAttribute
  private String variable;

  private Value value;

  @XStreamAsAttribute
  private Integer occurrence;

  XStreamValueSetValue(String variable, Value value) {
    this(variable, value, null);
  }

  XStreamValueSetValue(String variable, Value value, Integer occurrence) {
    this.variable = variable;
    this.value = value;
    this.occurrence = occurrence;
  }
}
