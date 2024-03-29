/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.magma.datasource.mongodb.converter;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.mongodb.BasicDBList;
import org.bson.Document;
import org.obiba.magma.*;
import org.obiba.magma.datasource.mongodb.MongoDBVariable;
import org.obiba.magma.type.LineStringType;
import org.obiba.magma.type.LocaleType;
import org.obiba.magma.type.PointType;
import org.obiba.magma.type.PolygonType;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class ValueConverter {

  private ValueConverter() {}

  public static Object marshall(Variable variable, Value value) {
    if(value == null || value.isNull()) return null;

    if(variable.isRepeatable()) {
      Collection<Object> list = new BasicDBList();
      for(Value val : value.asSequence().getValues()) {
        list.add(marshall(val));
      }
      return list;
    }
    return marshall(value);
  }

  public static Value unmarshall(MongoDBVariable variable, Document object) {
    return unmarshall(variable.getValueType(), variable.isRepeatable(), variable.getId(), object);
  }

  public static Value unmarshall(ValueType type, boolean repeatable, String field, Document object) {
    if(object == null || !object.containsKey(field)) {
      return repeatable ? type.nullSequence() : type.nullValue();
    }

    if(repeatable) {
      Iterable<?> values = (Iterable<?>) object.get(field);
      if(values == null) return type.nullSequence();
      ImmutableList.Builder<Value> list = ImmutableList.builder();
      for(Object o : values) {
        list.add(unmarshall(type, o));
      }
      return type.sequenceOf(list.build());
    }
    return unmarshall(type, object.get(field));
  }

  public static Value unmarshall(ValueType type, Object value) {
    if(value == null) return type.nullValue();

    if(type.isGeo()) {
      // will be turned to a JSON string
      return type.valueOf(value.toString());
    }

    return type.valueOf(value);
  }

  //
  // Private methods
  //

  private static Object marshall(Value value) {
    if(value == null || value.isNull()) return null;
    ValueType type = value.getValueType();
    Object rawValue = value.getValue();
    if(rawValue instanceof MagmaDate) {
      return ((MagmaDate) rawValue).asDate();
    }
    if(type.isGeo()) {
      return marshallGeo(value);
    }
    return type.equals(LocaleType.get()) ? type.toString(value) : value.getValue();
  }

  @SuppressWarnings("unchecked")
  private static Object marshallGeo(Value value) {
    if(value == null || value.isNull()) return null;

    ValueType type = value.getValueType();
    if(PointType.get().equals(type)) {
      return marshallPoint((Coordinate) value.getValue());
    }
    if(LineStringType.get().equals(type)) {
      return marshallLine((Iterable<Coordinate>) value.getValue());
    }
    if(PolygonType.get().equals(type)) {
      return marshallPolygon((Iterable<Iterable<Coordinate>>) value.getValue());
    }
    throw new RuntimeException("Geo value type expected: " + value.getValueType());
  }

  private static List<Double> marshallPoint(Coordinate point) {
    if (point == null) return null;
    return Lists.newArrayList(point.getLongitude(), point.getLatitude());
  }

  private static List<List<Double>> marshallLine(Iterable<Coordinate> line) {
    return StreamSupport.stream(line.spliterator(), false)
        .map(ValueConverter::marshallPoint)
        .collect(Collectors.toList());
  }

  private static Object marshallPolygon(Iterable<Iterable<Coordinate>> polygon) {
    return StreamSupport.stream(polygon.spliterator(), false)
        .map(ValueConverter::marshallLine)
        .collect(Collectors.toList());
  }

}
