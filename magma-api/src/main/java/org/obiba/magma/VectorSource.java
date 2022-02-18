/*
 * Copyright (c) 2022 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.magma;

/**
 * A source of iterable values from the same variable.
 */
public interface VectorSource {

  /**
   * Value type of the values being accessed.
   *
   * @return
   */
  ValueType getValueType();

  /**
   * Iterable values for the provided entities; implementation can optimize values extraction requests while iteration is performed.
   *
   * @param entities
   * @return
   */
  Iterable<Value> getValues(Iterable<VariableEntity> entities);

  /**
   * Whether a vector values summary can be extracted directly from the underlying system.
   *
   * @return
   */
  default boolean supportVectorSummary() {
    return false;
  }

  /**
   * Get the summary statistics for the vector's values.
   *
   * @param entities
   * @return
   * @throws VectorSummarySourceNotSupportedException
   */
  default VectorSummarySource getVectorSummarySource(Iterable<VariableEntity> entities) throws VectorSummarySourceNotSupportedException {
    throw new VectorSummarySourceNotSupportedException(getClass());
  }

}
