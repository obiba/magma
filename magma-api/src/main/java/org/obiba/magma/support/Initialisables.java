/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.magma.support;

import org.obiba.magma.Initialisable;
import org.obiba.magma.MagmaRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

public final class Initialisables {

  private static final Logger log = LoggerFactory.getLogger(Initialisables.class);

  private Initialisables() {
  }

  public static void silentlyInitialise(@Nullable Initialisable initialisable) {
    try {
      initialise(initialisable);
    } catch (RuntimeException e) {
      log.warn("Ignoring exception during initialisable.initialise().", e);
    }
  }

  public static void silentlyInitialise(@NotNull Object... initialisable) {
    try {
      initialise(initialisable);
    } catch (RuntimeException e) {
      log.warn("Ignoring exception during initialisable.initialise().", e);
    }
  }

  public static void initialise(@NotNull Initialisable initialisable) {
    try {
      initialisable.initialise();
    } catch (MagmaRuntimeException e) {
      throw e;
    } catch (RuntimeException e) {
      throw new MagmaRuntimeException(e);
    }
  }

  public static void initialise(Object initialisable) {
    if (initialisable instanceof Initialisable) {
      initialise((Initialisable) initialisable);
    }
  }

  public static void initialise(@NotNull Initialisable... initialisable) {
    for (Initialisable o : initialisable) {
      initialise(o);
    }
  }

  public static void initialise(@NotNull Object... initialisable) {
    for (Object o : initialisable) {
      initialise(o);
    }
  }

  public static void initialise(@NotNull Iterable<?> initialisables) {
    for (Object o : initialisables) {
      initialise(o);
    }
  }

}
