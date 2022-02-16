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

public class VectorSummarySourceNotSupportedException extends MagmaRuntimeException {

  private static final long serialVersionUID = -2344327012324234250L;

  private final Class<? extends VectorSource> vectorSourceClass;

  public VectorSummarySourceNotSupportedException(Class<? extends VectorSource> vectorSourceClass) {
    super(vectorSourceClass + " does not support VectorSummary");
    this.vectorSourceClass = vectorSourceClass;
  }

  public Class<? extends VectorSource> getVectorSourceClass() {
    return vectorSourceClass;
  }
}
