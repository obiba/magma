package org.obiba.meta.xstream;

import java.util.LinkedList;
import java.util.List;

import org.obiba.meta.Occurrence;
import org.obiba.meta.ValueSet;
import org.obiba.meta.VariableValueSource;
import org.obiba.meta.support.ValueSetBean;

import com.thoughtworks.xstream.annotations.XStreamImplicit;

public class XStreamValueSet extends ValueSetBean {

  @XStreamImplicit
  private List<XStreamValueSetValue> values = new LinkedList<XStreamValueSetValue>();

  public XStreamValueSet(ValueSet valueSet) {
    super(valueSet);
  }

  public void addValue(VariableValueSource source) {
    values.add(new XStreamValueSetValue(source.getVariable().getName(), source.getValue(this)));
  }

  public void addValue(Occurrence occurrence, VariableValueSource source) {
    values.add(new XStreamValueSetValue(source.getVariable().getName(), source.getValue(occurrence), occurrence.getOrder()));
  }

}
