/*
 * Copyright (c) 2016 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.magma;

/**
 * Decorator design pattern.
 * @param <T>
 */
public interface Decorator<T> {

  /**
   * Decorate an object and return the result.
   * @param object
   * @return
   */
  T decorate(T object);

  /**
   * Release any resources that could have been associated to the decorated object.
   * @param object
   */
  void release(T object);

}
