package org.obiba.meta.support;

import java.util.Date;

import org.obiba.meta.Collection;
import org.obiba.meta.Occurrence;
import org.obiba.meta.ValueSet;
import org.obiba.meta.ValueSetExtension;
import org.obiba.meta.VariableEntity;

public class OccurrenceBean implements Occurrence {

  private ValueSet valueSet;

  private String group;

  private int order;

  public OccurrenceBean(ValueSet valueSet, String group, int order) {
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

  public <T> T extend(String extensionName) {
    return ((ValueSetExtension<Occurrence, T>) getCollection().getExtension(extensionName)).extend(this);
  }

  public Collection getCollection() {
    return valueSet.getCollection();
  }

  public Date getEndDate() {
    return valueSet.getEndDate();
  }

  public Date getStartDate() {
    return valueSet.getStartDate();
  }

  public VariableEntity getVariableEntity() {
    return valueSet.getVariableEntity();
  }

}
