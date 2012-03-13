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
  private String type;

  VariableValueTypeFilter(String type) {
    this.type = type;
  }

  @Override
  public void initialise() {
    Preconditions.checkState(Strings.isNullOrEmpty(type) == false);
  }

  @Override
  protected Boolean runFilter(Variable item) {
    initialise();
    return item.getValueType().getName().equalsIgnoreCase(type);
  }
}
