/*
 * Copyright (c) 2016 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.magma.js;

import org.mozilla.javascript.ContextFactory;
import org.obiba.magma.MagmaEngineExtension;

/**
 * A {@code MagmaEngine} extension for creating derived variables using JavaScript.
 */
public class MagmaJsExtension implements MagmaEngineExtension {

  private static final long serialVersionUID = 2071830136892020358L;

  private transient MagmaContextFactory magmaContextFactory = new MagmaContextFactory();

  public void setMagmaContextFactory(MagmaContextFactory magmaContextFactory) {
    this.magmaContextFactory = magmaContextFactory;
  }

  @Override
  public String getName() {
    return "magma-js";
  }

  @Override
  public void initialise() {
    // Set MagmaContextFactory as the global factory. We can only do this if it hasn't been done already.
    if(!ContextFactory.hasExplicitGlobal()) {
      ContextFactory.initGlobal(magmaContextFactory);
      // Initialise the shared scope
      magmaContextFactory.initialise();
    }

  }
}
