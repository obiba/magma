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

import static org.springframework.util.Assert.notNull;

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
    notNull(valueNode, "valueNode cannot be null");
    notNull(context, "context cannot be null");
    ValueType valueType = ValueType.Factory.forName(valueNode.getValueType());
    String stringValue = valueNode.getValue();
    return valueNode.isSequence() ? valueType.sequenceOf(stringValue) : valueType.valueOf(stringValue);
  }
}
