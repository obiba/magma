/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.magma.transform;

import com.google.common.base.Function;

public interface BijectiveFunction<F, T> extends Function<F, T> {

  /**
   * Applies the reverse of the function defined by {@code apply}
   */
  F unapply(T from);

}
