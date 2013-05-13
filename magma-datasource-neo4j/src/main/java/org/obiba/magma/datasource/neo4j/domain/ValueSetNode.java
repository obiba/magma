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

import java.util.Set;

import org.springframework.data.neo4j.annotation.NodeEntity;

@NodeEntity
public class ValueSetNode extends AbstractAttributeAwareNode {

  private ValueTableNode valueTable;

  private VariableEntityNode variableEntity;

  private Set<ValueSetValueNode> values;

//  private transient Map<String, ValueNode> valueMap;

  public ValueSetNode() {
  }

//  public synchronized Map<String, ValueNode> getValueMap() {
//    return valueMap == null ? valuesAsMap() : valueMap;
//  }
//
//  private synchronized Map<String, ValueNode> valuesAsMap() {
//    if(valueMap == null) {
//      Map<String, ValueNode> map = Maps.newHashMap();
//      for(ValueNode vsv : getValues()) {
//        // log.info("{}={}", vsv.getVariable().getName(), vsv.getValue().toString());
//        map.put(vsv.getVariable().getName(), vsv);
//      }
//      valueMap = Collections.unmodifiableMap(map);
//    }
//    return valueMap;
//  }

}
