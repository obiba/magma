package org.obiba.magma.js;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.ContextFactory;

/**
 * Creates instances of {@code MagmaContext}
 */
public class MagmaContextFactory extends ContextFactory {

  @Override
  protected Context makeContext() {
    return new MagmaContext(this);
  }

}
