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

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import org.springframework.data.neo4j.annotation.NodeEntity;
import org.springframework.data.neo4j.annotation.RelatedTo;

import com.google.common.collect.Maps;

import static org.neo4j.graphdb.Direction.INCOMING;
import static org.neo4j.graphdb.Direction.OUTGOING;

@NodeEntity
public class ValueSetNode extends AbstractTimestampedGraphItem {

  @RelatedTo(type = "HAS_VALUE_SETS", direction = INCOMING)
  private ValueTableNode valueTable;

  @RelatedTo(type = "HAS_ENTITIES", direction = INCOMING)
  private VariableEntityNode variableEntity;

  @RelatedTo(type = "HAS_VALUE_SET_VALUES", direction = OUTGOING)
  private Set<ValueSetValueNode> valueSetValues;

  @SuppressWarnings("TransientFieldInNonSerializableClass")
  private transient Map<String, ValueSetValueNode> valueMap;

  public synchronized Map<String, ValueSetValueNode> getValueMap() {
    return valueMap == null ? valuesAsMap() : valueMap;
  }

  private synchronized Map<String, ValueSetValueNode> valuesAsMap() {
    if(valueMap == null) {
      Map<String, ValueSetValueNode> map = Maps.newHashMap();
      for(ValueSetValueNode vsv : getValueSetValues()) {
        // log.info("{}={}", vsv.getVariable().getName(), vsv.getValue().toString());
        map.put(vsv.getVariable().getName(), vsv);
      }
      valueMap = Collections.unmodifiableMap(map);
    }
    return valueMap;
  }

  public ValueTableNode getValueTable() {
    return valueTable;
  }

  public void setValueTable(ValueTableNode valueTable) {
    this.valueTable = valueTable;
  }

  public VariableEntityNode getVariableEntity() {
    return variableEntity;
  }

  public void setVariableEntity(VariableEntityNode variableEntity) {
    this.variableEntity = variableEntity;
  }

  public Set<ValueSetValueNode> getValueSetValues() {
    return valueSetValues;
  }

  public void setValueSetValues(Set<ValueSetValueNode> valueSetValues) {
    this.valueSetValues = valueSetValues;
  }

}
