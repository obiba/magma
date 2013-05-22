/*
 * Copyright (c) 2013 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.magma.datasource.neo4j.converter;

import javax.annotation.Nonnull;

import org.obiba.magma.Value;
import org.obiba.magma.ValueType;
import org.obiba.magma.datasource.neo4j.domain.ValueNode;

public class ValueConverter implements Neo4jConverter<ValueNode, Value> {

  public static ValueConverter getInstance() {
    return new ValueConverter();
  }

  private ValueConverter() {
  }

  @Override
  public ValueNode marshal(@Nonnull Value value, @Nonnull Neo4jMarshallingContext context) {
    return null;  //To change body of implemented methods use File | Settings | File Templates.
  }

  @SuppressWarnings("unchecked")
  @Override
  public Value unmarshal(@Nonnull ValueNode valueNode, @Nonnull Neo4jMarshallingContext context) {
    final ValueType valueType = ValueType.Factory.forName(valueNode.getValueType());
    if(valueNode.isSequence()) {
      //TODO
      return null;
//      Iterable<Value> values = Iterables
//          .transform((Iterable<Serializable>) valueNode.getValue(), new Function<Serializable, Value>() {
//            @Override
//            public Value apply(@Nullable Serializable input) {
//              return ValueType.Factory.newValue(valueType, input);
//            }
//          });
//      return ValueType.Factory.newSequence(valueType, values);
    }
    return ValueType.Factory.newValue(valueType, valueNode.getValue());
  }
}
