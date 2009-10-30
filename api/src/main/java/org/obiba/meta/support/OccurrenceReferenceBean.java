package org.obiba.meta.support;

import org.obiba.meta.OccurrenceReference;
import org.obiba.meta.ValueSetReference;
import org.obiba.meta.Variable;

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
