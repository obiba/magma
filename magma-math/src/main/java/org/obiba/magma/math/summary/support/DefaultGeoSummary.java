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

import com.google.common.collect.Lists;
import org.obiba.magma.Coordinate;
import org.obiba.magma.math.GeoSummary;
import org.obiba.magma.math.H3Cell;

import java.util.Collection;
import java.util.List;

public class DefaultGeoSummary extends DefaultFrequenciesSummary implements GeoSummary {

  private final List<Coordinate> coordinates = Lists.newArrayList();

  private Coordinate min;

  private Coordinate max;

  private Coordinate centroid;

  private final List<H3Cell> cells = Lists.newArrayList();

  @Override
  public List<Coordinate> getCoordinates() {
    return coordinates;
  }

  void addCoordinate(Coordinate coordinate) {
    coordinates.add(coordinate);
  }

  public void addCoordinates(Collection<Coordinate> coords) {
    if (coords != null)
      coordinates.addAll(coords);
  }

  @Override
  public Coordinate getMin() {
    return min;
  }

  public void setMin(Coordinate min) {
    this.min = min;
  }

  @Override
  public Coordinate getMax() {
    return max;
  }

  public void setMax(Coordinate max) {
    this.max = max;
  }

  @Override
  public Coordinate getCentroid() {
    return centroid;
  }

  public void setCentroid(Coordinate centroid) {
    this.centroid = centroid;
  }

  @Override
  public List<H3Cell> getCells() {
    return cells;
  }

  public void addCells(Collection<H3Cell> cells) {
    this.cells.addAll(cells);
  }
}
