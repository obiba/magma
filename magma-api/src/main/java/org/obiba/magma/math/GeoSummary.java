/*
 * Copyright (c) 2022 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.magma.math;

import org.obiba.magma.Coordinate;

import java.util.List;

public interface GeoSummary extends FrequenciesSummary {

  /**
   * Vertices of the concave hull of the coordinates.
   */
  List<Coordinate> getCoordinates();

  /**
   * Lower corner of the bounding box of all the coordinates, null if there are none.
   */
  default Coordinate getMin() {
    return null;
  }

  /**
   * Upper corner of the bounding box of all the coordinates, null if there are none.
   */
  default Coordinate getMax() {
    return null;
  }

  /**
   * Mean of the feature centroids, null if there are none.
   */
  default Coordinate getCentroid() {
    return null;
  }

  /**
   * Feature centroids counted by H3 cell, empty if H3 is not available.
   */
  default List<H3Cell> getCells() {
    return List.of();
  }

}
