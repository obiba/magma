package org.obiba.meta;

import org.obiba.meta.support.ValueSetReferenceBean;

public class OccurrenceReferenceBean extends ValueSetReferenceBean implements OccurrenceReference {

  Variable variable;

  int order;

  public OccurrenceReferenceBean(String entityType, String entityIdentifier, String valueSetIdentifier, Variable variable, int order) {
    super(entityType, entityIdentifier, valueSetIdentifier);
  }

  public OccurrenceReferenceBean(ValueSetReference reference, Variable variable, int order) {
    this(reference.getVariableEntity().getType(), reference.getVariableEntity().getIdentifier(), reference.getIdentifier(), variable, order);
  }

  @Override
  public int getOrder() {
    return order;
  }

  @Override
  public Variable getVariable() {
    return variable;
  }

}
