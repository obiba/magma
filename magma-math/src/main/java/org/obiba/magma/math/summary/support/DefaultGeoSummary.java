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
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.Point;
import org.obiba.magma.Coordinate;
import org.obiba.magma.Value;
import org.obiba.magma.math.GeoSummary;
import org.obiba.magma.math.H3Cell;
import org.obiba.magma.type.LineStringType;
import org.obiba.magma.type.PointType;
import org.obiba.magma.type.PolygonType;
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
   * Sets the bounding box of the vertices, and the mean of the feature centroids, which are also counted by H3 cell.
   *
   * @param vertices all the coordinates of the features
   * @param centroids one centroid per feature, see {@link #getCentroid(Value)}
   */
  public void addExtentAndCells(List<Coordinate> vertices, List<Coordinate> centroids) {
    if (vertices == null || vertices.isEmpty() || centroids == null || centroids.isEmpty()) return;
    addExtent(vertices, centroids);
    addCells(getCells(centroids));
  }

  /**
   * All the coordinates of a point, line string or polygon value, empty if it is not one of these.
   */
  @SuppressWarnings("unchecked")
  public static List<Coordinate> getVertices(Value value) {
    List<Coordinate> vertices = new ArrayList<>();
    if (value.isNull()) return vertices;
    if (value.getValueType() == PointType.get()) {
      vertices.add((Coordinate) value.getValue());
    } else if (value.getValueType() == LineStringType.get()) {
      vertices.addAll((Collection<Coordinate>) value.getValue());
    } else if (value.getValueType() == PolygonType.get()) {
      for (List<Coordinate> ring : (Collection<List<Coordinate>>) value.getValue()) {
        vertices.addAll(ring);
      }
    }
    return vertices;
  }

  /**
   * Centroid of a point, line string or polygon value (the first ring is the shell, the others are holes),
   * null if it is not one of these. Falls back to the mean of the vertices when the geometry is degenerate.
   */
  @SuppressWarnings("unchecked")
  public static Coordinate getCentroid(Value value) {
    List<Coordinate> vertices = getVertices(value);
    if (vertices.isEmpty()) return null;
    if (value.getValueType() == PointType.get()) return vertices.get(0);
    // ponytail: planar centroid in degrees, fine at the scale of an H3 cell, not for continent-wide shapes
    try {
      GeometryFactory factory = new GeometryFactory();
      Geometry geometry;
      if (value.getValueType() == LineStringType.get()) {
        geometry = factory.createLineString(asJts(vertices));
      } else {
        List<LinearRing> rings = new ArrayList<>();
        for (List<Coordinate> ring : (Collection<List<Coordinate>>) value.getValue()) {
          rings.add(factory.createLinearRing(asJts(ring)));
        }
        geometry = factory.createPolygon(rings.get(0), rings.subList(1, rings.size()).toArray(new LinearRing[0]));
      }
      Point centroid = geometry.getCentroid();
      if (!centroid.isEmpty()) return new Coordinate(centroid.getX(), centroid.getY());
    } catch (RuntimeException e) {
      log.debug("Degenerate geometry, centroid is the mean of its vertices: {}", e.getMessage());
    }
    return mean(vertices);
  }

  private static com.vividsolutions.jts.geom.Coordinate[] asJts(List<Coordinate> coordinates) {
    return coordinates.stream()
        .map(coord -> new com.vividsolutions.jts.geom.Coordinate(coord.getLongitude(), coord.getLatitude()))
        .toArray(com.vividsolutions.jts.geom.Coordinate[]::new);
  }

  private static Coordinate mean(List<Coordinate> coordinates) {
    double sumLat = 0, sumLon = 0;
    for (Coordinate coord : coordinates) {
      sumLat += coord.getLatitude();
      sumLon += coord.getLongitude();
    }
    return new Coordinate(sumLon / coordinates.size(), sumLat / coordinates.size());
  }

  private void addExtent(List<Coordinate> vertices, List<Coordinate> centroids) {
    double minLat = Double.MAX_VALUE, minLon = Double.MAX_VALUE, maxLat = -Double.MAX_VALUE, maxLon = -Double.MAX_VALUE;
    for (Coordinate coord : vertices) {
      minLat = Math.min(minLat, coord.getLatitude());
      minLon = Math.min(minLon, coord.getLongitude());
      maxLat = Math.max(maxLat, coord.getLatitude());
      maxLon = Math.max(maxLon, coord.getLongitude());
    }
    setMin(new Coordinate(minLon, minLat));
    setMax(new Coordinate(maxLon, maxLat));
    // ponytail: arithmetic mean, wrong across the antimeridian
    setCentroid(mean(centroids));
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
