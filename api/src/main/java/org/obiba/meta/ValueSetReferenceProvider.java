package org.obiba.meta;

import java.util.Set;

/**
 * 
 */
public interface ValueSetReferenceProvider {

  public String getEntityType();

  public boolean isForEntityType(String entityType);

  public Set<ValueSetReference> getValueSetReferences();

  public Set<OccurrenceReference> getOccurrenceReferences(ValueSetReference reference, Variable variable);

}
