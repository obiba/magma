package org.obiba.magma.datasource.neo4j.converter;

import javax.annotation.Nullable;

import org.obiba.magma.MagmaRuntimeException;
import org.obiba.magma.Variable;
import org.obiba.magma.datasource.neo4j.domain.ValueTableNode;
import org.obiba.magma.datasource.neo4j.domain.VariableNode;

public class VariableConverter implements Neo4jConverter<VariableNode, Variable> {

  private static final VariableConverter INSTANCE = new VariableConverter();

  public static VariableConverter getInstance() {
    return INSTANCE;
  }

  private VariableConverter() {
  }

  @Override
  public VariableNode marshal(Variable variable, Neo4jMarshallingContext context) {
    VariableNode variableNode = getNodeForVariable(variable, context);
    ValueTableNode valueTableNode = context.getValueTable();
    if(variableNode == null) {
      variableNode = new VariableNode(valueTableNode, variable);
      valueTableNode.getVariables().add(variableNode);
    } else {
      variableNode.copyVariableFields(variable);
    }

    if(variableNode.getValueType() != variable.getValueType()) {
      throw new MagmaRuntimeException(
          "Changing the value type of a variable is not supported. Cannot modify variable '" + variable.getName() +
              "' in table '" + valueTableNode.getName() + "'");
    }

    addAttributes(magmaObject, variableNode);
    marshalCategories(magmaObject, variableNode);

    return variableNode;
  }

  @Override
  public Variable unmarshal(VariableNode variableNode, Neo4jMarshallingContext context) {
    return null;  //To change body of implemented methods use File | Settings | File Templates.
  }

  @Nullable
  public VariableNode getNodeForVariable(Variable variable, Neo4jMarshallingContext context) {
    for(VariableNode node : context.getValueTable().getVariables()) {
      if(node.getName().equals(variable.getName())) return node;
    }
    return null;
  }
}
