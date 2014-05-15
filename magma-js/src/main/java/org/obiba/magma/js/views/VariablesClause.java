package org.obiba.magma.js.views;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.validation.constraints.NotNull;

import org.obiba.magma.Initialisable;
import org.obiba.magma.MagmaRuntimeException;
import org.obiba.magma.NoSuchVariableException;
import org.obiba.magma.ValueTable;
import org.obiba.magma.ValueTableWriter.VariableWriter;
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

  private Set<Variable> variables = new LinkedHashSet<>();

  @SuppressWarnings("TransientFieldInNonSerializableClass")
  private transient ValueTable valueTable;

  @SuppressWarnings("TransientFieldInNonSerializableClass")
  private transient Set<VariableValueSource> variableValueSources;

  @SuppressWarnings("TransientFieldInNonSerializableClass")
  private transient boolean initialised = false;

  public void setVariables(Collection<Variable> variables) {
    this.variables.clear();
    if(variables != null) {
      this.variables.addAll(variables);
    }
  }

  @NotNull
  @Override
  public VariableValueSource getVariableValueSource(String name) throws NoSuchVariableException {
    if(!initialised) {
      throw new IllegalStateException("The initialise() method must be called before getVariableValueSource().");
    }
    for(VariableValueSource variableValueSource : variableValueSources) {
      if(variableValueSource.getVariable().getName().equals(name)) {
        return variableValueSource;
      }
    }
    throw new NoSuchVariableException(name);
  }

  @Override
  public Iterable<VariableValueSource> getVariableValueSources() {
    if(!initialised) {
      throw new IllegalStateException("The initialise() method must be called before getVariableValueSources().");
    }
    return variableValueSources;
  }

  @Override
  public void initialise() {
    if(valueTable == null) {
      throw new IllegalStateException("The setValueTable() method must be called before initialise().");
    }
    JavascriptVariableValueSourceFactory factory = new JavascriptVariableValueSourceFactory();
    factory.setVariables(variables);
    factory.setValueTable(valueTable);
    variableValueSources = factory.createSources();
    for(VariableValueSource vvs : variableValueSources) {
      try {
        Initialisables.initialise(vvs);
      } catch(MagmaRuntimeException ignored) {

      }
    }
    initialised = true;
  }

  @Override
  public void setValueTable(@NotNull ValueTable valueTable) {
    //noinspection ConstantConditions
    if(valueTable == null) throw new IllegalArgumentException("valueTable cannot be null");
    this.valueTable = valueTable;
  }

  @Override
  public VariableWriter createWriter() {
    return new JsVariableWriter();
  }

  private class JsVariableWriter implements VariableWriter {

    @Override
    public void writeVariable(@NotNull Variable variable) {
      // update or add variable
      Set<Variable> variableSet = new LinkedHashSet<>();
      boolean updated = false;
      for(Variable var : variables) {
        if(var.getName().equals(variable.getName())) {
          variableSet.add(variable);
          updated = true;
        } else {
          variableSet.add(var);
        }
      }

      if(!updated) {
        variableSet.add(variable);
      }

      variables = variableSet;
    }

    @Override
    public void removeVariable(@NotNull Variable variable) {
      // update or add variable
      Set<Variable> variableSet = new LinkedHashSet<>();
      for(Variable var : variables) {
        if(!var.getName().equals(variable.getName())) {
          variableSet.add(var);
        }
      }

      variables = variableSet;
    }

    @Override
    public void close() {
    }
  }

}
