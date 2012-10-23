package org.obiba.magma.filter;

import org.obiba.magma.Initialisable;
import org.obiba.magma.Variable;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

@XStreamAlias("variableValueType")
public class VariableValueTypeFilter extends AbstractFilter<Variable> implements Initialisable {

  @XStreamAsAttribute
  private String valueType;

  VariableValueTypeFilter(String valueType) {
    this.valueType = valueType;
  }

  @Override
  public void initialise() {
    Preconditions.checkState(Strings.isNullOrEmpty(valueType) == false);
  }

  @Override
  protected Boolean runFilter(Variable item) {
    initialise();
    return item.getValueType().getName().equalsIgnoreCase(valueType);
  }
}
