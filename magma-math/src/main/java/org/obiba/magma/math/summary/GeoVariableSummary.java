/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.magma.math.summary;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.collect.Iterators;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import org.obiba.magma.*;
import org.obiba.magma.math.FrequenciesSummary;
import org.obiba.magma.math.Frequency;
import org.obiba.magma.math.GeoSummary;
import org.obiba.magma.math.summary.support.DefaultFrequency;
import org.obiba.magma.math.summary.support.DefaultGeoSummary;
import org.obiba.magma.type.LineStringType;
import org.obiba.magma.type.PointType;
import org.obiba.magma.type.PolygonType;
import org.opensphere.geometry.algorithm.ConcaveHull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

/**
 *
 */
public class GeoVariableSummary extends AbstractVariableSummary implements GeoSummary, Serializable {

  private static final long serialVersionUID = 203198842420473154L;

  private static final Logger log = LoggerFactory.getLogger(GeoVariableSummary.class);

  private final org.apache.commons.math3.stat.Frequency frequencyDist = new org.apache.commons.math3.stat.Frequency();

  private final Collection<Frequency> frequencies = new ArrayList<>();

  private GeoVariableSummary(@NotNull Variable variable) {
    super(variable);
  }

  @Override
  public String getCacheKey(ValueTable table) {
    return GeoVariableSummaryFactory.getCacheKey(variable, table, getOffset(), getLimit());
  }

  private GeoSummary geoSummary;

  @Override
  public long getN() {
    return geoSummary.getN();
  }

  @Override
  @NotNull
  public Iterable<Frequency> getFrequencies() {
    return geoSummary.getFrequencies();
  }

  @Override
  public List<Coordinate> getCoordinates() {
    return geoSummary.getCoordinates();
  }

  @SuppressWarnings("ParameterHidesMemberVariable")
  public static class Builder implements VariableSummaryBuilder<GeoVariableSummary, Builder> {

    public ArrayList<Coordinate> coords = new ArrayList<>();

    private final GeoVariableSummary summary;

    @NotNull
    private final Variable variable;

    private final List<String> missings;

    private boolean addedTable;

    private boolean addedValue;

    public Builder(@NotNull Variable variable) {
      this.variable = variable;
      this.missings = variable.getCategories().stream()
          .filter(Category::isMissing)
          .map(Category::getName)
          .collect(Collectors.toList());
      summary = new GeoVariableSummary(variable);
    }

    @Override
    public Builder addValue(@NotNull Value value) {
      if (addedTable) {
        throw new IllegalStateException("Cannot add value for variable " + summary.variable.getName() +
            " because values where previously added from the whole table with addTable().");
      }
      add(value);
      addedValue = true;
      return this;
    }

    @Override
    public Builder addTable(@NotNull ValueTable table, @NotNull ValueSource valueSource) {
      if (addedValue) {
        throw new IllegalStateException("Cannot add table for variable " + summary.variable.getName() +
            " because values where previously added with addValue().");
      }
      add(table, valueSource);
      addedTable = true;

      return this;
    }

    private void add(@NotNull ValueTable table, @NotNull ValueSource variableValueSource) {
      //noinspection ConstantConditions
      Preconditions.checkArgument(table != null, "table cannot be null");
      //noinspection ConstantConditions
      Preconditions.checkArgument(variableValueSource != null, "variableValueSource cannot be null");

      if (!variableValueSource.supportVectorSource()) return;
      VectorSource vs = variableValueSource.asVectorSource();
      Iterable<VariableEntity> entities = summary.getFilteredVariableEntities(table);
      if (vs.supportVectorSummary()) {
        summary.geoSummary = vs.getVectorSummarySource(entities).asGeoSummary();
      }
      // if no pre-computed summary, go through values
      if (summary.geoSummary == null) {
        for (Value value : vs.getValues(entities)) {
          add(value);
        }
      }
    }

    private void add(@NotNull Value value) {
      //noinspection ConstantConditions
      Preconditions.checkArgument(value != null, "value cannot be null");

      if (value.isSequence()) {
        if (value.isNull()) {
          summary.frequencyDist.addValue(FrequenciesSummary.NULL_NAME);
        } else {
          for (Value v : value.asSequence().getValue()) {
            add(v);
          }
        }
      } else if (value.isNull()) {
        summary.frequencyDist.addValue(FrequenciesSummary.NULL_NAME);
      } else {
        boolean added = false;
        if (!missings.isEmpty()) {
          String valueStr = value.toString();
          if (missings.contains(valueStr)) {
            summary.frequencyDist.addValue(valueStr);
            added = true;
          }
        }
        if (!added) {
          summary.frequencyDist.addValue(FrequenciesSummary.NOT_NULL_NAME);
          getCoordinates(value);
        }
      }
    }

    private void getCoordinates(Value value) {
      if (value.getValueType() == PointType.get()) {
        coords.add((Coordinate) value.getValue());
      } else if (value.getValueType() == LineStringType.get()) {
        coords.addAll((Collection<Coordinate>) value.getValue());
      } else if (value.getValueType() == PolygonType.get()) {
        Collection<List<Coordinate>> coordinateList = (Collection<List<Coordinate>>) value.getValue();
        for (List<Coordinate> coordinate : coordinateList) {
          coords.addAll(coordinate);
        }
      }
    }

    /**
     * Returns an iterator of frequencyDist names
     */
    private Iterator<String> freqNames(org.apache.commons.math3.stat.Frequency freq) {
      return Iterators.transform(freq.valuesIterator(), new Function<Comparable<?>, String>() {

        @Override
        public String apply(Comparable<?> input) {
          return input.toString();
        }
      });
    }

    private void compute() {
      log.trace("Start compute default summary {}", summary.variable);
      if (summary.geoSummary == null) {
        DefaultGeoSummary geoSummary = new DefaultGeoSummary();
        long max = 0;
        Iterator<String> concat = freqNames(summary.frequencyDist);

        // Iterate over all values.
        // The loop will also determine the mode of the distribution (most frequent value)
        while (concat.hasNext()) {
          String value = concat.next();
          long count = summary.frequencyDist.getCount(value);
          if (count > max) {
            max = count;
          }
          geoSummary.addFrequency(new DefaultFrequency(value, summary.frequencyDist.getCount(value),
              Double.isNaN(summary.frequencyDist.getPct(value)) ? 0.0 : summary.frequencyDist.getPct(value),
              missings.contains(value) || value.equals(FrequenciesSummary.NULL_NAME)));
        }
        geoSummary.setN(summary.frequencyDist.getSumFreq());
        geoSummary.addCoordinates(getConcaveHull(coords));

        summary.geoSummary = geoSummary;
      }
    }

    public Builder filter(Integer offset, Integer limit) {
      summary.setOffset(offset);
      summary.setLimit(limit);
      return this;
    }

    @Override
    @NotNull
    public GeoVariableSummary build() {
      compute();
      return summary;
    }

    @NotNull
    @Override
    public Variable getVariable() {
      return variable;
    }

    private static Collection<Coordinate> getConcaveHull(List<Coordinate> coordinates) {
      // adjust the treshold to have more or less lines... Lower treshold means more complex polygon
      GeometryCollection geometryCollection = getGeometryCollection(coordinates);
      ConcaveHull concaveHull = new ConcaveHull(geometryCollection, 2);

      com.vividsolutions.jts.geom.Coordinate[] coordinatesConcave = concaveHull.getConcaveHull().getCoordinates();

      // From JTS Coordinate to Magma Coordinate
      Collection<Coordinate> result = new ArrayList<>();
      for (com.vividsolutions.jts.geom.Coordinate aCoordinatesConvex : coordinatesConcave) {
        result.add(new Coordinate(aCoordinatesConvex.x, aCoordinatesConvex.y));
      }

      return result;
    }

    private static GeometryCollection getGeometryCollection(List<Coordinate> coordinates) {

      Point[] coordinatesArray = new Point[coordinates.size()];

      // From Magma Coordinate to JTS coordinate
      GeometryFactory factory = new GeometryFactory();
      for (int i = 0; i < coordinates.size(); i++) {
        Coordinate coordinate = coordinates.get(i);
        coordinatesArray[i] = factory.createPoint(
            new com.vividsolutions.jts.geom.Coordinate(coordinate.getLongitude(), coordinate.getLatitude()));
      }

      // Calculate Concave Hull
      return new GeometryCollection(coordinatesArray, factory);
    }
  }
}

