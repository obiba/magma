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

import org.springframework.data.neo4j.annotation.NodeEntity;
import org.springframework.data.neo4j.annotation.RelatedTo;

import static org.neo4j.graphdb.Direction.INCOMING;

@NodeEntity
public class ValueSetValueNode extends AbstractValueAwareNode {

  @RelatedTo(type = "VALUE_SET_HAS_VALUE_SET_VALUES", direction = INCOMING)
  private ValueSetNode valueSet;

  @RelatedTo(type = "VARIABLE_HAS_VALUE_SET_VALUES", direction = INCOMING)
  private VariableNode variable;

  public ValueSetNode getValueSet() {
    return valueSet;
  }

  public void setValueSet(ValueSetNode valueSet) {
    this.valueSet = valueSet;
  }

  public VariableNode getVariable() {
    return variable;
  }

  public void setVariable(VariableNode variable) {
    this.variable = variable;
  }

}
