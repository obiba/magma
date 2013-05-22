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

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import java.util.SortedSet;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.obiba.core.util.TimedExecution;
import org.obiba.magma.Datasource;
import org.obiba.magma.Initialisable;
import org.obiba.magma.NoSuchValueSetException;
import org.obiba.magma.NoSuchValueTableException;
import org.obiba.magma.Timestamps;
import org.obiba.magma.Value;
import org.obiba.magma.ValueSet;
import org.obiba.magma.ValueType;
import org.obiba.magma.Variable;
import org.obiba.magma.VariableEntity;
import org.obiba.magma.VariableValueSource;
import org.obiba.magma.VectorSource;
import org.obiba.magma.datasource.neo4j.converter.Neo4jMarshallingContext;
import org.obiba.magma.datasource.neo4j.converter.VariableConverter;
import org.obiba.magma.datasource.neo4j.domain.ValueTableNode;
import org.obiba.magma.datasource.neo4j.domain.VariableEntityNode;
import org.obiba.magma.datasource.neo4j.domain.VariableNode;
import org.obiba.magma.support.AbstractValueTable;
import org.obiba.magma.support.AbstractVariableEntityProvider;
import org.obiba.magma.support.VariableEntityBean;
import org.obiba.magma.type.DateTimeType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.neo4j.support.Neo4jTemplate;

import com.google.common.collect.ImmutableList;

import static org.springframework.util.Assert.notNull;

public class Neo4jValueTable extends AbstractValueTable {

  private static final Logger log = LoggerFactory.getLogger(Neo4jValueTable.class);

  private final Long graphId;

  public Neo4jValueTable(Datasource datasource, @Nonnull ValueTableNode valueTableNode) {
    super(datasource, valueTableNode.getName());
    graphId = valueTableNode.getGraphId();
    log.debug("ValueTable: {}, graphId: {}", valueTableNode.getName(), graphId);
    setVariableEntityProvider(new Neo4jVariableEntityProvider(valueTableNode.getEntityType()));
  }

  @Override
  public void initialise() {
    super.initialise();
    ((Initialisable) getVariableEntityProvider()).initialise();
    cacheVariables();
  }

  @Override
  public ValueSet getValueSet(VariableEntity entity) throws NoSuchValueSetException {
    return null;  //To change body of implemented methods use File | Settings | File Templates.
  }

  @Override
  public Timestamps getTimestamps() {
    final ValueTableNode node = getNode();
    return new Timestamps() {

      @Nonnull
      @Override
      public Value getCreated() {
        return DateTimeType.get().valueOf(node.getCreated());
      }

      @Nonnull
      @Override
      public Value getLastUpdate() {
        return DateTimeType.get().valueOf(node.getUpdated());
      }
    };
  }

  @Nonnull
  @Override
  public Neo4jDatasource getDatasource() {
    return (Neo4jDatasource) super.getDatasource();
  }

  @Nonnull
  ValueTableNode getNode() throws NoSuchValueTableException {
    try {
      return getNeo4jTemplate().findOne(graphId, ValueTableNode.class);
    } catch(Exception e) {
      throw new NoSuchValueTableException(getName());
    }
  }

  Neo4jTemplate getNeo4jTemplate() {
    return getDatasource().getNeo4jTemplate();
  }

  Neo4jMarshallingContext createContext() {
    return getDatasource().createContext(getNode());
  }

  private void cacheVariables() {
    log.debug("Populating variable cache for table {}", getName());
    TimedExecution timedExecution = new TimedExecution().start();
    for(VariableNode variableNode : getNode().getVariables()) {
      addVariableValueSource(new Neo4jVariableValueSource(variableNode));
    }
    log.debug("Populating variable cache for {}: {} variables loaded in {}", getName(), getSources().size(),
        timedExecution.end().formatExecutionTime());
  }

  void writeVariableValueSource(VariableNode node, Variable variable) {
    addVariableValueSource(new Neo4jVariableValueSource(node, variable));
  }

  public class Neo4jVariableEntityProvider extends AbstractVariableEntityProvider implements Initialisable {

    private final Set<VariableEntity> entities = new LinkedHashSet<VariableEntity>();

    public Neo4jVariableEntityProvider(String entityType) {
      super(entityType);
    }

    @Override
    public void initialise() {
      log.debug("Populating entity cache for table {}", getName());
      // get the variable entities that have a value set in the table
      TimedExecution timedExecution = new TimedExecution().start();
      for(VariableEntityNode entityNode : getNode().getVariableEntities()) {
        getNeo4jTemplate().fetch(entityNode);
        entities.add(new VariableEntityBean(entityNode.getType(), entityNode.getIdentifier()));
      }
      log.debug("Populating entity cache for {}: {} entities loaded in {}", getName(), entities.size(),
          timedExecution.end().formatExecutionTime());
    }

    @Override
    public Set<VariableEntity> getVariableEntities() {
      return Collections.unmodifiableSet(entities);
    }
  }

  public class Neo4jVariableValueSource implements VariableValueSource, VectorSource {

    @Nonnull
    private final Long graphId;

    private Variable variable;

    Neo4jVariableValueSource(@Nonnull VariableNode node, @Nonnull Variable variable) {
      notNull(node, "node cannot be null");
      notNull(node.getGraphId(), "node graphId cannot be null");
      notNull(variable, "variable cannot be null");
      graphId = node.getGraphId();
      this.variable = variable;
    }

    Neo4jVariableValueSource(@Nonnull VariableNode node) {
      notNull(node, "node cannot be null");
      notNull(node.getGraphId(), "node graphId cannot be null");
      graphId = node.getGraphId();
    }

    @Override
    public Variable getVariable() {
      if(variable == null) {
        VariableNode node = getNeo4jTemplate().findOne(graphId, VariableNode.class);
        getNeo4jTemplate().fetch(node);
        getNeo4jTemplate().fetch(node.getCategories());
        variable = VariableConverter.getInstance().unmarshal(node, createContext());
      }
      return variable;
    }

    @Nonnull
    @Override
    public Value getValue(ValueSet valueSet) {
      //TODO
      return null;
    }

    @Nullable
    @Override
    public VectorSource asVectorSource() {
      return this;
    }

    @Override
    public Iterable<Value> getValues(SortedSet<VariableEntity> entities) {
      if(entities.isEmpty()) {
        return ImmutableList.of();
      }
      //TODO
      return null;

    }

    @Nonnull
    @Override
    public ValueType getValueType() {
      return getVariable().getValueType();
    }

    @Override
    public boolean equals(Object obj) {
      if(this == obj) {
        return true;
      }
      if(obj == null) {
        return false;
      }
      if(!(obj instanceof Neo4jVariableValueSource)) {
        return super.equals(obj);
      }
      return Objects.equals(((Neo4jVariableValueSource) obj).graphId, graphId);
    }

    @Override
    public int hashCode() {
      return graphId.hashCode();
    }
  }
}
