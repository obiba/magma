/*
 * Copyright (c) 2022 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.magma.math.summary.support;

import org.obiba.magma.Coordinate;
import org.obiba.magma.math.GeoSummary;
import org.obiba.magma.math.summary.support.DefaultFrequenciesSummary;

import java.util.Collection;
import java.util.List;

public class DefaultGeoSummary extends DefaultFrequenciesSummary implements GeoSummary {

  private List<Coordinate> coordinates;

  @Override
  public List<Coordinate> getCoordinates() {
    return coordinates;
  }

  void addCoordinate(Coordinate coordinate) {
    coordinates.add(coordinate);
  }

  public void addCoordinates(Collection<Coordinate> coords) {
    coordinates.addAll(coords);
  }
}
