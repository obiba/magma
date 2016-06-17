package org.obiba.magma.js;

import java.util.Map;

import com.google.common.collect.Maps;
import groovy.lang.Closure;
import groovy.lang.ExpandoMetaClass;
import org.obiba.magma.Variable;
import org.obiba.magma.js.methods.ScriptableVariableMethods;

/**
 * A {@code Scriptable} implementation for {@code Variable} objects.
 */
public class ScriptableVariable extends Scriptable {

  private final Variable variable;

  public ScriptableVariable(Variable variable) {
    if(variable == null) throw new IllegalArgumentException("variable cannot be null");
    this.variable = variable;
  }

  @Override
  public String toString() {
    return variable.toString();
  }

  public Variable getVariable() {
    return variable;
  }

  private static Map<String, Closure> members = Maps.newConcurrentMap();

  static {
    ExpandoMetaClass expandoMetaClass = new ExpandoMetaClass(ScriptableVariable.class, true, false);
    addMethodProvider(expandoMetaClass, members, ScriptableVariableMethods.class);
    expandoMetaClass.initialize();
  }

  public static Map<String, Closure> getMembers() {
    return members;
  }
}
