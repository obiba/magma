package org.obiba.magma.js;

import java.util.Map;
import java.util.Set;

import com.google.common.collect.Maps;
import jdk.nashorn.api.scripting.JSObject;
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

  @Override
  public Object getMember(final String name) {
    return getMembers().get(name);
  }

  @Override
  public Set<String> keySet() {
    return getMembers().keySet();
  }

  private static Map<String, JSObject> members = Maps.newConcurrentMap();

  static {
    addMethodProvider(members, ScriptableVariableMethods.class);
  }

  public static Map<String, JSObject> getMembers() {
    return members;
  }
}
