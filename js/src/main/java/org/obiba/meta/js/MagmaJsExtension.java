package org.obiba.meta.js;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.ContextAction;
import org.mozilla.javascript.ContextFactory;
import org.obiba.meta.Initialisable;

/**
 * A {@code MagmaEngine} extension for creating manipulating variables using javascript.
 */
public class MagmaJsExtension implements Initialisable {

  @Override
  public void initialise() {
    // Set MagmaContextFactory as the global factory
    ContextFactory.initGlobal(new MagmaContextFactory());

    // Initialise the shared scope
    ContextFactory.getGlobal().call(new ContextAction() {
      @Override
      public Object run(Context cx) {
        return cx.initStandardObjects(null, true);
      }
    });

  }
}
