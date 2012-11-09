/*******************************************************************************
 * Copyright (c) 2012 OBiBa. All rights reserved.
 *  
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *  
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.magma;

/**
 * Factory of value loader, given a string reference.
 */
public interface ValueLoaderFactory {

  /**
   * Create a {@link ValueLoader} given a reference and the occurrence of the value (starting at 0) in the case of a
   * sequence of values.
   * @param valueRef Provides the reference to the value
   * @param occurrence Null when the value is not a sequence of values
   * @return
   */
  public ValueLoader create(Value valueRef, Integer occurrence);

}
