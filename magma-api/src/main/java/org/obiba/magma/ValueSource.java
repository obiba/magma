/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.magma;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

/**
 * Defines the contract for obtaining a particular {@link Value} from a {@code ValueSet}.
 */
public interface ValueSource {

  @NotNull
  ValueType getValueType();

  /**
   * This method should never return null.
   *
   * @param valueSet
   * @return
   * @throws IllegalArgumentException when the provided valueSet is for a entityType different than the variable's
   * entityType
   */
  @NotNull
  Value getValue(ValueSet valueSet);

  @Nullable
  VectorSource asVectorSource();

}
