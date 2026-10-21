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
import com.uber.h3core.H3Core;
import com.uber.h3core.util.LatLng;
import org.obiba.magma.Coordinate;
import org.obiba.magma.math.GeoSummary;
import org.obiba.magma.math.H3Cell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DefaultGeoSummary extends DefaultFrequenciesSummary implements GeoSummary {

  private static final Logger log = LoggerFactory.getLogger(DefaultGeoSummary.class);

  public static final int H3_RESOLUTION = 7;

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

  /**
   * Sets the bounding box and the centroid of the coordinates and, if requested, counts them by H3 cell.
   *
   * @param withCells true for points, the vertices of lines and polygons are not counted
   */
  public void addExtentAndCells(List<Coordinate> coordinates, boolean withCells) {
    if (coordinates == null || coordinates.isEmpty()) return;
    addExtent(coordinates);
    if (withCells) addCells(getCells(coordinates));
  }

  private void addExtent(List<Coordinate> coordinates) {
    double minLat = Double.MAX_VALUE, minLon = Double.MAX_VALUE, maxLat = -Double.MAX_VALUE, maxLon = -Double.MAX_VALUE;
    double sumLat = 0, sumLon = 0;
    for (Coordinate coord : coordinates) {
      minLat = Math.min(minLat, coord.getLatitude());
      minLon = Math.min(minLon, coord.getLongitude());
      maxLat = Math.max(maxLat, coord.getLatitude());
      maxLon = Math.max(maxLon, coord.getLongitude());
      sumLat += coord.getLatitude();
      sumLon += coord.getLongitude();
    }
    setMin(new Coordinate(minLon, minLat));
    setMax(new Coordinate(maxLon, maxLat));
    // ponytail: arithmetic mean, wrong across the antimeridian, and the mean of the vertices for lines and polygons
    setCentroid(new Coordinate(sumLon / coordinates.size(), sumLat / coordinates.size()));
  }

  private static List<H3Cell> getCells(List<Coordinate> coordinates) {
    if (H3.CORE == null) return List.of();
    Map<Long, Long> counts = new HashMap<>();
    for (Coordinate coord : coordinates) {
      counts.merge(H3.CORE.latLngToCell(coord.getLatitude(), coord.getLongitude(), H3_RESOLUTION), 1L, Long::sum);
    }
    List<H3Cell> cells = new ArrayList<>();
    counts.forEach((cell, count) -> {
      List<Coordinate> boundary = new ArrayList<>();
      for (LatLng vertex : H3.CORE.cellToBoundary(cell)) {
        boundary.add(new Coordinate(vertex.lng, vertex.lat));
      }
      cells.add(new H3Cell(H3.CORE.h3ToString(cell), count, boundary));
    });
    return cells;
  }

  /**
   * Loads the H3 native library on first use, null if it is not available on this platform.
   */
  private static class H3 {
    private static final H3Core CORE = load();

    private static H3Core load() {
      try {
        return H3Core.newInstance();
      } catch (IOException | LinkageError e) {
        log.warn("H3 library not available, geo summaries will not count points by cell: {}", e.getMessage());
        return null;
      }
    }
  }
}
