/*
 * Copyright (c) 2013 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.magma.datasource.mongodb.converter;

import javax.annotation.Nullable;

import org.bson.types.BasicBSONList;
import org.obiba.magma.Coordinate;
import org.obiba.magma.MagmaDate;
import org.obiba.magma.Value;
import org.obiba.magma.ValueType;
import org.obiba.magma.Variable;
import org.obiba.magma.type.LineStringType;
import org.obiba.magma.type.LocaleType;
import org.obiba.magma.type.PointType;
import org.obiba.magma.type.PolygonType;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.mongodb.BasicDBList;
import com.mongodb.DBObject;

public class ValueConverter {

  private ValueConverter() {}

  public static Object marshall(Variable variable, Value value) {
    if(value == null || value.isNull()) return null;

    if(variable.isRepeatable()) {
      BasicDBList list = new BasicDBList();
      for(Value val : value.asSequence().getValues()) {
        list.add(marshall(val));
      }
      return list;
    }
    return marshall(value);
  }

  public static Value unmarshall(Variable variable, DBObject object) {
    return unmarshall(variable.getValueType(), variable.isRepeatable(),
        VariableConverter.normalizeFieldName(variable.getName()), object);
  }

  public static Value unmarshall(ValueType type, boolean repeatable, String field, DBObject object) {
    if(object == null || !object.containsField(field)) return repeatable ? type.nullSequence() : type.nullValue();

    if(repeatable) {
      BasicBSONList values = (BasicBSONList) object.get(field);
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

  private static Object marshallPoint(Coordinate point) {
    return point.toArray();
  }

  private static Iterable<double[]> marshallLine(Iterable<Coordinate> line) {
    return Iterables.transform(line, new Function<Coordinate, double[]>() {
      @Nullable
      @Override
      public double[] apply(@Nullable Coordinate input) {
        return input == null ? null : input.toArray();
      }
    });
  }

  private static Object marshallPolygon(Iterable<Iterable<Coordinate>> line) {
    return Iterables.transform(line, new Function<Iterable<Coordinate>, Iterable<double[]>>() {
      @Nullable
      @Override
      public Iterable<double[]> apply(@Nullable Iterable<Coordinate> input) {
        return input == null ? null : marshallLine(input);
      }
    });
  }

}
