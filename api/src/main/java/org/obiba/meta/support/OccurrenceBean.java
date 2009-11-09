package org.obiba.meta.support;

import org.obiba.meta.Occurrence;
import org.obiba.meta.ValueSet;
import org.obiba.meta.ValueSetProvider;

public class OccurrenceBean extends ValueSetBean implements Occurrence {

  private ValueSet valueSet;

  private String group;

  private int order;

  public OccurrenceBean(ValueSetProvider valueSetProvider, ValueSet valueSet, String group, int order) {
    super(valueSetProvider, valueSet.getVariableEntity(), valueSet.getStartDate(), valueSet.getEndDate());
    this.valueSet = valueSet;
    this.group = group;
    this.order = order;
  }

  @Override
  public ValueSet getParent() {
    return valueSet;
  }

  @Override
  public String getGroup() {
    return group;
  }

  @Override
  public int getOrder() {
    return order;
  }

}
