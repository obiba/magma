/*
 * Copyright (c) 2013 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.magma.datasource.neo4j;

import java.io.IOException;

import javax.annotation.Nonnull;

import org.obiba.magma.NoSuchVariableException;
import org.obiba.magma.Value;
import org.obiba.magma.ValueTableWriter;
import org.obiba.magma.Variable;
import org.obiba.magma.VariableEntity;
import org.obiba.magma.datasource.neo4j.converter.Neo4jMarshallingContext;
import org.obiba.magma.datasource.neo4j.converter.VariableConverter;
import org.obiba.magma.datasource.neo4j.domain.ValueNode;
import org.obiba.magma.datasource.neo4j.domain.ValueSetNode;
import org.obiba.magma.datasource.neo4j.domain.ValueSetValueNode;
import org.obiba.magma.datasource.neo4j.domain.ValueTableNode;
import org.obiba.magma.datasource.neo4j.domain.VariableEntityNode;
import org.obiba.magma.datasource.neo4j.domain.VariableNode;
import org.obiba.magma.type.BinaryType;
import org.springframework.data.neo4j.support.Neo4jTemplate;
import org.springframework.util.Assert;

import static org.springframework.util.Assert.notNull;

public class Neo4jValueTableWriter implements ValueTableWriter {

  private final Neo4jValueTable valueTable;

  private final VariableConverter variableConverter = VariableConverter.getInstance();

  private final Neo4jMarshallingContext context;

  private final Neo4jTemplate neo4jTemplate;

  public Neo4jValueTableWriter(Neo4jValueTable valueTable) {
    this.valueTable = valueTable;
    context = valueTable.createContext();
    neo4jTemplate = valueTable.getDatasource().getNeo4jTemplate();
  }

  @Override
  public VariableWriter writeVariables() {
    return new Neo4jVariableWriter();
  }

  @Nonnull
  @Override
  public ValueSetWriter writeValueSet(@Nonnull VariableEntity entity) {
    return new Neo4jValueSetWriter(entity);
  }

  @Override
  public void close() throws IOException {
  }

  private class Neo4jValueSetWriter implements ValueSetWriter {

    private ValueSetNode valueSetNode;

    private Neo4jValueSetWriter(@Nonnull VariableEntity entity) {
      notNull(entity, "entity cannot be null");

      // find entity or create it
      VariableEntityNode entityNode = context.getVariableEntityRepository()
          .findByIdentifierAndType(entity.getIdentifier(), entity.getType());
      if(entityNode == null) {
        entityNode = context.getNeo4jTemplate().save(new VariableEntityNode(entity.getIdentifier(), entity.getType()));
        valueTable.writeVariableEntity(entity);
      }
      context.getNeo4jTemplate().fetch(entityNode);

      ValueTableNode tableNode = valueTable.getNode();
      valueSetNode = valueTable.getDatasource().getValueSetRepository().find(tableNode, entityNode);
      if(valueSetNode == null) {
        valueSetNode = new ValueSetNode();
        valueSetNode.setValueTable(tableNode);
        valueSetNode.setVariableEntity(entityNode);
      }
      // update timestamps
      valueSetNode = neo4jTemplate.save(valueSetNode);
    }

    @Override
    public void writeValue(@Nonnull Variable variable, @Nonnull Value value) {
      notNull(variable, "variable cannot be null");
      notNull(value, "value cannot be null");

      ValueTableNode tableNode = valueTable.getNode();
      VariableNode variableNode = valueTable.getDatasource().getVariableRepository()
          .findByTableAndName(tableNode, variable.getName());
      if(variableNode == null) {
        throw new NoSuchVariableException(valueTable.getName(), variable.getName());
      }

      ValueNode valueNode = valueTable.getDatasource().getValueRepository().find(variableNode, valueSetNode);
      if(valueNode == null) {
        createValue(value, variableNode);
      } else {
        updateValue(value, valueNode);
      }

      // update timestamps
      neo4jTemplate.save(tableNode);
      neo4jTemplate.save(valueSetNode);
    }

    private void createValue(Value value, VariableNode variableNode) {
      ValueNode valueNode = neo4jTemplate.save(new ValueNode(value));
      ValueSetValueNode valueSetValue = new ValueSetValueNode();
      valueSetValue.setValue(valueNode);
      valueSetValue.setValueSet(valueSetNode);
      valueSetNode.getValueSetValues().add(valueSetValue);
      valueSetValue.setVariable(variableNode);
      neo4jTemplate.save(valueSetValue);
    }

    private void updateValue(Value value, ValueNode valueNode) {
      neo4jTemplate.fetch(valueNode);
      if(value.isNull()) {
//        neo4jTemplate.fetch(valueNode.getParent());
        neo4jTemplate.delete(valueNode.getParent());
        neo4jTemplate.delete(valueNode);
      } else {
        if(BinaryType.get().equals(value.getValueType())) {
          //TODO write binary value
        } else {
          valueNode.copyProperties(value);
        }
      }
    }

    @Override
    public void close() throws IOException {
    }
  }

  private class Neo4jVariableWriter implements VariableWriter {

    @Override
    public void writeVariable(@Nonnull Variable variable) {
      notNull(variable, "variable cannot be null");
      Assert.isTrue(valueTable.isForEntityType(variable.getEntityType()),
          "Wrong entity type for variable '" + variable.getName() + "': " + valueTable.getEntityType() +
              " expected, " + variable.getEntityType() + " received.");

      VariableNode node = variableConverter.marshal(variable, context);
      neo4jTemplate.save(node);
      neo4jTemplate.save(valueTable.getNode()); // update ValueTableNode timestamps
      valueTable.writeVariableValueSource(node, variable);
    }

    @Override
    public void close() throws IOException {
    }
  }
}
