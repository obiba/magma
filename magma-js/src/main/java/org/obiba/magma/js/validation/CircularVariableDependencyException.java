/*
 * Copyright (c) 2016 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.magma.js.validation;

import org.obiba.magma.js.MagmaJsEvaluationRuntimeException;

import static org.obiba.magma.js.validation.VariableScriptValidator.VariableRefNode;

public class CircularVariableDependencyException extends MagmaJsEvaluationRuntimeException {

  private static final long serialVersionUID = 6224713591897743417L;

  private final String variableRef;

  private final VariableRefNode variableRefNode;

  public CircularVariableDependencyException(VariableRefNode variableRefNode) {
    super("Circular dependency for variable '" + variableRefNode.getVariableRef() + "'");
    this.variableRefNode = variableRefNode;
    variableRef = variableRefNode.getVariableRef();
  }

  public String getVariableRef() {
    return variableRef;
  }

  public String getHierarchy() {
    VariableRefNode root = getRoot(variableRefNode);
    StringBuilder sb = new StringBuilder("Calls hierarchy:").append(System.lineSeparator());
    print(root, sb, 0);
    return sb.toString();
  }

  private VariableRefNode getRoot(VariableRefNode node) {
    for(VariableRefNode caller : node.getCallers()) {
      getRoot(caller);
    }
    return node;
  }

  private void print(VariableRefNode node, StringBuilder sb, int indent) {
    for(int i = 0; i < indent; i++) {
      sb.append("    ");
    }
    sb.append("+-- ");
    sb.append(node.getVariableRef()).append(System.lineSeparator());
    for(VariableRefNode callee : node.getCallees()) {
      print(callee, sb, indent + 1);
    }
  }
}
