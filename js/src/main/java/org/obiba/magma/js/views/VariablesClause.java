package org.obiba.magma.js.views;

import java.util.HashSet;
import java.util.Set;

import org.obiba.magma.Initialisable;
import org.obiba.magma.NoSuchVariableException;
import org.obiba.magma.Variable;
import org.obiba.magma.VariableValueSource;
import org.obiba.magma.js.JavascriptVariableValueSourceFactory;
import org.obiba.magma.support.Initialisables;
import org.obiba.magma.views.ListClause;

/**
 * This implementation of {@link ListClause} will contain {@link Variable}s with a "script" attribute and/or a "sameAs"
 * attribute used to provide new custom variables derived from existing variables. Before use the initialise method must
 * be called to compile the JavaScript necessary to return the values of the derived variables.
 */
public class VariablesClause implements ListClause, Initialisable {

  private Set<Variable> variables;

  private Set<VariableValueSource> variableValueSources;

  public void setVariables(Set<Variable> variables) {
    this.variables = new HashSet<Variable>();
    if(variables != null) {
      this.variables.addAll(variables);
    }
  }

  @Override
  public VariableValueSource getVariableValueSource(String name) throws NoSuchVariableException {
    for(VariableValueSource variableValueSource : variableValueSources) {
      if(variableValueSource.getVariable().getName().equals(name)) {
        return variableValueSource;
      }
    }
    throw new NoSuchVariableException("The VariableValueSource '" + name + "' does not exist.");
  }

  @Override
  public Iterable<VariableValueSource> getVariableValueSources() {
    return variableValueSources;
  }

  @Override
  public void initialise() {
    JavascriptVariableValueSourceFactory factory = new JavascriptVariableValueSourceFactory();
    factory.setVariables(variables);
    variableValueSources = factory.createSources();
    Initialisables.initialise(variableValueSources);
  }

}
