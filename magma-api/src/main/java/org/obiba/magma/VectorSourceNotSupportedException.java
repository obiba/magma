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

public class VectorSourceNotSupportedException extends MagmaRuntimeException {

  private static final long serialVersionUID = -2930317701287038250L;

  private final Class<? extends ValueSource> valueSourceClass;

  public VectorSourceNotSupportedException(Class<? extends ValueSource> valueSourceClass) {
    super(valueSourceClass + " does not support VectorSource");
    this.valueSourceClass = valueSourceClass;
  }

  public Class<? extends ValueSource> getValueSourceClass() {
    return valueSourceClass;
  }
}
