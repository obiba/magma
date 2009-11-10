package org.obiba.meta.beans;

import java.util.Set;

import org.obiba.meta.Occurrence;
import org.obiba.meta.ValueSet;
import org.obiba.meta.Variable;

public interface OccurrenceProvider {

  public boolean occurenceOf(Variable variable);

  public Set<Occurrence> loadOccurrences(ValueSet valueSet, Variable variable);

}
