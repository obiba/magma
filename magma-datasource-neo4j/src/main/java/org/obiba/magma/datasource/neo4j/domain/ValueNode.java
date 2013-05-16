/*
 * Copyright (c) 2013 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.magma.datasource.neo4j.domain;

import java.io.Serializable;

import org.obiba.magma.Value;
import org.obiba.magma.ValueType;
import org.springframework.data.neo4j.annotation.NodeEntity;
import org.springframework.data.neo4j.annotation.RelatedTo;

import static org.neo4j.graphdb.Direction.INCOMING;

@NodeEntity
public class ValueNode extends AbstractTimestampedGraphItem {

  @RelatedTo(type = "HAS_VALUE", direction = INCOMING)
  private AbstractValueAwareNode parent;

  private ValueType valueType;

  private boolean sequence;

  // TODO binaries will be stored with JPA
  // http://api.neo4j.org/1.8/org/neo4j/graphdb/PropertyContainer.html#setProperty(java.lang.String, java.lang.Object)
  private Serializable value;

  public ValueNode() {
  }

  public ValueNode(Value value) {
    copyProperties(value);
  }

  public void copyProperties(Value magmaValue) {
    valueType = magmaValue.getValueType();
    sequence = magmaValue.isSequence();
    value = (Serializable) magmaValue.getValue();
  }

  public ValueType getValueType() {
    return valueType;
  }

  public void setValueType(ValueType valueType) {
    this.valueType = valueType;
  }

  public boolean isSequence() {
    return sequence;
  }

  public void setSequence(boolean sequence) {
    this.sequence = sequence;
  }

  public Serializable getValue() {
    return value;
  }

  public void setValue(Serializable value) {
    this.value = value;
  }

  public AbstractValueAwareNode getParent() {
    return parent;
  }

  public void setParent(AbstractValueAwareNode parent) {
    this.parent = parent;
  }

}
