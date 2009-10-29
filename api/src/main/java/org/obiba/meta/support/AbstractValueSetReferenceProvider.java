package org.obiba.meta.support;

import java.util.Set;

import org.obiba.meta.OccurrenceReference;
import org.obiba.meta.ValueSetReference;
import org.obiba.meta.ValueSetReferenceProvider;
import org.obiba.meta.Variable;

public abstract class AbstractValueSetReferenceProvider implements ValueSetReferenceProvider {

  private String entityType;

  protected AbstractValueSetReferenceProvider(String entityType) {
    this.entityType = entityType;
  }

  @Override
  public String getEntityType() {
    return entityType;
  }

  @Override
  public boolean isForEntityType(String entityType) {
    return getEntityType().equals(entityType);
  }

  @Override
  public Set<OccurrenceReference> getOccurrenceReferences(ValueSetReference reference, Variable variable) {
    return null;
  }

}
