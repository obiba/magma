package org.obiba.meta.js;

import org.mozilla.javascript.Context;
import org.obiba.meta.IValueSetReference;
import org.obiba.meta.IVariableValueSource;
import org.obiba.meta.Value;

public final class EngineMethods {

  /**
   * Accessed as $ in js.
   * 
   * <pre>
   *   var value = $('Participant.firstName');
   * </pre>
   * @param name
   * @return
   */
  public static Object valueOf(String name) {
    IValueSetReference reference = (IValueSetReference) Context.getCurrentContext().getThreadLocal(IValueSetReference.class);

    IVariableValueSource source = lookupSource(name);

    Value value = source.getValue(reference);
    return value.getValue();
  }

  private static IVariableValueSource lookupSource(String name) {
    return null;
  }

}
