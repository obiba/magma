/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.magma.views.support;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.obiba.magma.NoSuchViewException;
import org.obiba.magma.Variable;
import org.obiba.magma.views.View;

public class VariableOperationContext {

  public enum Operation {
    ADD,
    DELETE
  }

  private final Map<String, Map<Operation, Collection<Variable>>> viewOperations = new HashMap<>();

  public void addVariable(View view, Variable variable) {
    addOperation(view, Operation.ADD, variable);
  }

  public void deleteVariable(View view, Variable variable) {
    addOperation(view, Operation.DELETE, variable);
  }

  public Map<Operation, Collection<Variable>> getOperations(View view) {
    String viewName = view.getName();

    if (viewOperations.containsKey(viewName)) {
      return viewOperations.get(viewName);
    }

    throw new NoSuchViewException(view.getName());
  }

  public boolean hasOperations(View view) {
    return viewOperations.containsKey(view.getName());
  }

  private void addOperation(View view, Operation operation, Variable variable) {
    String viewName = view.getName();
    Map<Operation, Collection<Variable>> operations = viewOperations.get(viewName);

    if (operations == null) {
      operations = new HashMap<>();
      viewOperations.put(viewName, operations);
    }

    Collection variables = operations.get(operation);
    if(variables == null) {
      variables = new ArrayList();
      operations.put(operation, variables);
    }

    variables.add(variable);
  }
}
