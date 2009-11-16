package org.obiba.magma.beans;

import java.util.Set;

import org.obiba.magma.Occurrence;
import org.obiba.magma.ValueSet;
import org.obiba.magma.Variable;

public interface OccurrenceProvider {

  public boolean providesOccurrencesOf(Variable variable);

  public Set<Occurrence> loadOccurrences(ValueSet valueSet, Variable variable);

}
