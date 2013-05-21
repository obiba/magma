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

import org.obiba.magma.Category;
import org.obiba.magma.MagmaRuntimeException;
import org.obiba.magma.Variable;
import org.obiba.magma.datasource.neo4j.domain.CategoryNode;
import org.obiba.magma.datasource.neo4j.domain.ValueTableNode;
import org.obiba.magma.datasource.neo4j.domain.VariableNode;

public class VariableConverter extends AttributeAwareConverter implements Neo4jConverter<VariableNode, Variable> {

  public static VariableConverter getInstance() {
    return new VariableConverter();
  }

  private VariableConverter() {
  }

  @Override
  public VariableNode marshal(@Nonnull Variable variable, @Nonnull Neo4jMarshallingContext context) {
    ValueTableNode valueTableNode = context.getValueTable();
    VariableNode variableNode = context.getVariableRepository().findByTableAndName(valueTableNode, variable.getName());
    if(variableNode == null) {
      variableNode = new VariableNode(valueTableNode, variable);
      valueTableNode.getVariables().add(variableNode);
    } else {
      context.getNeo4jTemplate().fetch(variableNode);
      variableNode.copyVariableFields(variable);
    }

    if(variableNode.getValueType() != variable.getValueType()) {
      throw new MagmaRuntimeException(
          "Changing the value type of a variable is not supported. Cannot modify variable '" + variable.getName() +
              "' in table '" + valueTableNode.getName() + "'");
    }

    addAttributes(variable, variableNode, context);
    marshalCategories(variable, variableNode, context);

    return variableNode;
  }

  @Override
  public Variable unmarshal(@Nonnull VariableNode variableNode, @Nonnull Neo4jMarshallingContext context) {
    Variable.Builder builder = Variable.Builder
        .newVariable(variableNode.getName(), variableNode.getValueType(), variableNode.getEntityType());
    builder.mimeType(variableNode.getMimeType()).occurrenceGroup(variableNode.getOccurrenceGroup())
        .referencedEntityType(variableNode.getReferencedEntityType()).unit(variableNode.getUnit());
    if(variableNode.isRepeatable()) {
      builder.repeatable();
    }

    buildAttributeAware(builder, variableNode, context);
    unmarshalCategories(builder, variableNode, context);
    return builder.build();
  }

  private void marshalCategories(Variable variable, VariableNode variableNode, Neo4jMarshallingContext context) {
    context.getNeo4jTemplate().fetch(variableNode.getCategories());
    for(Category category : variable.getCategories()) {
      CategoryNode categoryNode = variableNode.getCategory(category.getName());
      if(categoryNode == null) {
        variableNode.getCategories().add(new CategoryNode(category));
      } else {
        categoryNode.copyCategoryFields(category);
      }
    }
  }

  private void unmarshalCategories(Variable.Builder builder, VariableNode variableNode,
      Neo4jMarshallingContext context) {
    context.getNeo4jTemplate().fetch(variableNode.getCategories());
    for(CategoryNode categoryNode : variableNode.getCategories()) {
      Category.Builder categoryBuilder = Category.Builder.newCategory(categoryNode.getName())
          .withCode(categoryNode.getCode()).missing(categoryNode.isMissing());
      buildAttributeAware(categoryBuilder, categoryNode, context);
      builder.addCategory(categoryBuilder.build());
    }
  }

}
