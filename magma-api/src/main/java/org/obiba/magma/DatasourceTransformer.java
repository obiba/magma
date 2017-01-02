/*
 * Copyright (c) 2017 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.magma;

public interface DatasourceTransformer {

  /**
   * Transforms the specified datasource into another.
   *
   * @param datasource datasource
   * @return datasource resulting from the transformation original (note that this could be the same datasource object
   *         that was passed in, modified in some way, or it could be a new datasource object)
   */
  Datasource transform(Datasource datasource);
}
