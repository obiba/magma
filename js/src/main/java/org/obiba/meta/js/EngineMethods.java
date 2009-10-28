package org.obiba.meta.js;

import org.mozilla.javascript.Context;
import org.obiba.meta.ValueSetReference;
import org.obiba.meta.VariableValueSource;
import org.obiba.meta.MetaEngine;
import org.obiba.meta.Value;

public final class EngineMethods {

  /**
   * Accessed as $ in js.
   * 
   * <pre>
   *   var value = $('Participant.firstName');
   *   var value2 = $('other-collection:SMOKER_STATUS');
   * </pre>
   * @param name
   * @return
   */
  public static Object valueOf(String name) {
    ValueSetReference reference = (ValueSetReference) Context.getCurrentContext().getThreadLocal(ValueSetReference.class);

    VariableValueSource source = lookupSource(reference, name);

    Value value = source.getValue(reference);
    return value.getValue();
  }

  private static VariableValueSource lookupSource(ValueSetReference reference, String name) {
    return MetaEngine.get().lookupVariable(reference.getVariableEntity().getType(), name);
  }

}
