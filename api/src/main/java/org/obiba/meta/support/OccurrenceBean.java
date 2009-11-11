package org.obiba.meta.support;

import org.obiba.meta.Occurrence;
import org.obiba.meta.ValueSet;

public class OccurrenceBean extends ValueSetBean implements Occurrence {

  private ValueSet valueSet;

  private String group;

  private int order;

  public OccurrenceBean(ValueSet valueSet, String group, int order) {
    super(valueSet.getCollection(), valueSet.getVariableEntity());
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
