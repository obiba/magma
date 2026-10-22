/*
 * Copyright (c) 2026 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.magma.math.summary;

import org.junit.Test;
import org.obiba.magma.Coordinate;
import org.obiba.magma.Value;
import org.obiba.magma.Variable;
import org.obiba.magma.math.H3Cell;
import org.obiba.magma.math.summary.support.DefaultGeoSummary;
import org.obiba.magma.test.AbstractMagmaTest;
import org.obiba.magma.type.LineStringType;
import org.obiba.magma.type.PointType;
import org.obiba.magma.type.PolygonType;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.fest.assertions.api.Assertions.offset;

public class GeoVariableSummaryTest extends AbstractMagmaTest {

  @Test
  public void test_pointsCountedByCell() {
    Variable variable = Variable.Builder.newVariable("mock", PointType.get(), "mock").build();
    GeoVariableSummary.Builder builder = new GeoVariableSummary.Builder(variable);
    // 20 points in the interior, inside one cell, and 4 far apart on the hull
    for (int i = 0; i < 20; i++) {
      builder.addValue(PointType.get().valueOf(new Coordinate(4.835 + i * 0.0001, 45.764)));
    }
    builder.addValue(PointType.get().valueOf(new Coordinate(4.0, 45.0)));
    builder.addValue(PointType.get().valueOf(new Coordinate(6.0, 45.0)));
    builder.addValue(PointType.get().valueOf(new Coordinate(6.0, 47.0)));
    builder.addValue(PointType.get().valueOf(new Coordinate(4.0, 47.0)));
    builder.addValue(PointType.get().nullValue());
    GeoVariableSummary summary = builder.build();

    assertThat(summary.getN()).isEqualTo(25);
    assertThat(summary.getCells()).hasSize(5);
    assertThat(summary.getCells().stream().mapToLong(H3Cell::count).sum()).isEqualTo(24);
    assertThat(summary.getCells().stream().mapToLong(H3Cell::count).max().getAsLong()).isEqualTo(20);
    assertThat(summary.getCells().get(0).boundary()).hasSize(6);
    assertThat(summary.getMin().getLongitude()).isEqualTo(4.0);
    assertThat(summary.getMin().getLatitude()).isEqualTo(45.0);
    assertThat(summary.getMax().getLongitude()).isEqualTo(6.0);
    assertThat(summary.getMax().getLatitude()).isEqualTo(47.0);
    assertThat(summary.getCentroid().getLatitude()).isEqualTo((20 * 45.764 + 4 * 46) / 24, offset(1e-9));
  }

  @Test
  public void test_featureCentroids() {
    // square with a hole on its east half: area centroid is west of the middle
    Value polygon = PolygonType.get().valueOf("[[[4.0,45.0],[5.0,45.0],[5.0,46.0],[4.0,46.0],[4.0,45.0]],[[4.5,45.0],[5.0,45.0],[5.0,46.0],[4.5,46.0],[4.5,45.0]]]");
    Coordinate centroid = DefaultGeoSummary.getCentroid(polygon);
    assertThat(centroid.getLongitude()).isEqualTo(4.25, offset(1e-9));
    assertThat(centroid.getLatitude()).isEqualTo(45.5, offset(1e-9));

    // line: length weighted, not the mean of the 4 vertices
    centroid = DefaultGeoSummary.getCentroid(LineStringType.get().valueOf("[[4.0,45.0],[4.0001,45.0],[4.0002,45.0],[5.0,45.0]]"));
    assertThat(centroid.getLongitude()).isEqualTo(4.5, offset(1e-9));

    // degenerate polygon, too few points for a ring: mean of the vertices
    centroid = DefaultGeoSummary.getCentroid(PolygonType.get().valueOf("[[[4.0,45.0],[5.5,45.0],[4.0,45.0]]]"));
    assertThat(centroid.getLongitude()).isEqualTo(4.5, offset(1e-9));

    assertThat(DefaultGeoSummary.getCentroid(PolygonType.get().nullValue())).isNull();
  }

  @Test
  public void test_polygonsCountedByCentroid() {
    Variable variable = Variable.Builder.newVariable("mock", PolygonType.get(), "mock").build();
    GeoVariableSummary.Builder builder = new GeoVariableSummary.Builder(variable);
    // 5 small squares around the same place, one cell per polygon, not per vertex
    for (int i = 0; i < 5; i++) {
      builder.addValue(PolygonType.get().valueOf("[[[4.835,45.764],[4.836,45.764],[4.836,45.765],[4.835,45.765],[4.835,45.764]]]"));
    }
    GeoVariableSummary summary = builder.build();
    assertThat(summary.getCells()).hasSize(1);
    assertThat(summary.getCells().get(0).count()).isEqualTo(5);
    assertThat(summary.getMax().getLongitude()).isEqualTo(4.836);
    assertThat(summary.getCentroid().getLongitude()).isEqualTo(4.8355, offset(1e-9));
  }

}
