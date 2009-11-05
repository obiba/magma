package org.obiba.meta;

import java.util.Set;

public interface ValueSetConnection {

  public ValueSet getValueSet();

  public Set<Occurrence> loadOccurrences(Variable variable);

}
