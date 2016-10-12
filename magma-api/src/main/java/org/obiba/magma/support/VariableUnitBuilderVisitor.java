/*
 * Copyright (c) 2016 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.magma.support;

import java.util.Collections;
import java.util.Map;

import org.obiba.magma.Variable;
import org.obiba.magma.Variable.Builder;

/**
 * A {@code BuilderVisitor} for setting the {@code unit} property of variables. This is used when building multiple
 * variables where only a sub-set of them have a unit property to set. Build an instance of this type, provide a map of
 * variable name to unit and pass the instance to the {@code Variable.Builder} of each variable.
 */
public class VariableUnitBuilderVisitor implements Variable.BuilderVisitor {

  private Map<String, String> variableUnitMap = Collections.emptyMap();

  public VariableUnitBuilderVisitor() {
  }

  public VariableUnitBuilderVisitor(Map<String, String> variableUnitMap) {
    this.variableUnitMap = variableUnitMap;
  }

  public void setVariableUnitMap(Map<String, String> variableUnitMap) {
    this.variableUnitMap = variableUnitMap;
  }

  @Override
  public void visit(Builder builder) {
    for(Map.Entry<String, String> entry : variableUnitMap.entrySet()) {
      if(builder.isName(entry.getKey())) {
        builder.unit(entry.getValue());
      }
    }
  }

}
