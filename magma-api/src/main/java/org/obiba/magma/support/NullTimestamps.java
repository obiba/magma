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

import java.lang.ref.WeakReference;

import javax.validation.constraints.NotNull;

import org.obiba.magma.MagmaEngine;
import org.obiba.magma.Timestamps;
import org.obiba.magma.Value;
import org.obiba.magma.type.DateTimeType;

public class NullTimestamps implements Timestamps {

  @SuppressWarnings("StaticNonFinalField")
  private static WeakReference<NullTimestamps> instance = MagmaEngine.get().registerInstance(new NullTimestamps());

  private NullTimestamps() {

  }

  @SuppressWarnings("ConstantConditions")
  @edu.umd.cs.findbugs.annotations.SuppressWarnings("NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE")
  @NotNull
  public static NullTimestamps get() {
    if(instance == null || instance.get() == null) {
      instance = MagmaEngine.get().registerInstance(new NullTimestamps());
    }
    return instance.get();
  }

  @NotNull
  @Override
  public Value getCreated() {
    return DateTimeType.get().nullValue();
  }

  @NotNull
  @Override
  public Value getLastUpdate() {
    return DateTimeType.get().nullValue();
  }

}
