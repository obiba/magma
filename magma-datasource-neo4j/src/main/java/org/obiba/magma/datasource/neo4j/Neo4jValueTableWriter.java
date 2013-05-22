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

import org.obiba.magma.Value;
import org.obiba.magma.ValueTableWriter;
import org.obiba.magma.Variable;
import org.obiba.magma.VariableEntity;
import org.obiba.magma.datasource.neo4j.converter.Neo4jMarshallingContext;
import org.obiba.magma.datasource.neo4j.converter.VariableConverter;
import org.obiba.magma.datasource.neo4j.domain.VariableNode;
import org.springframework.util.Assert;

public class Neo4jValueTableWriter implements ValueTableWriter {

  private final Neo4jValueTable valueTable;

  private final VariableConverter variableConverter = VariableConverter.getInstance();

  private final Neo4jMarshallingContext context;

  public Neo4jValueTableWriter(Neo4jValueTable valueTable) {
    this.valueTable = valueTable;
    context = valueTable.createContext();
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
    //To change body of implemented methods use File | Settings | File Templates.
  }

  private class Neo4jValueSetWriter implements ValueSetWriter {

    private Neo4jValueSetWriter(VariableEntity entity) {
      //To change body of created methods use File | Settings | File Templates.
    }

    @Override
    public void writeValue(@Nonnull Variable variable, Value value) {
      //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void close() throws IOException {
      //To change body of implemented methods use File | Settings | File Templates.
    }
  }

  private class Neo4jVariableWriter implements VariableWriter {

    @Override
    public void writeVariable(@Nonnull Variable variable) {
      Assert.notNull(variable, "variable cannot be null");
      Assert.isTrue(valueTable.isForEntityType(variable.getEntityType()),
          "Wrong entity type for variable '" + variable.getName() + "': " + valueTable.getEntityType() +
              " expected, " + variable.getEntityType() + " received.");

      VariableNode node = variableConverter.marshal(variable, context);
      valueTable.getNeo4jTemplate().save(node);
      valueTable.writeVariableValueSource(node, variable);

      //TODO update ValueTableNode timestamps
    }

    @Override
    public void close() throws IOException {
      //To change body of implemented methods use File | Settings | File Templates.
    }
  }
}
