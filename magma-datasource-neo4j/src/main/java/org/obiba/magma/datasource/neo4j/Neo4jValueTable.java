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
import java.util.Set;

import javax.annotation.Nonnull;

import org.obiba.core.util.TimedExecution;
import org.obiba.magma.Datasource;
import org.obiba.magma.Initialisable;
import org.obiba.magma.NoSuchValueSetException;
import org.obiba.magma.NoSuchValueTableException;
import org.obiba.magma.Timestamps;
import org.obiba.magma.Value;
import org.obiba.magma.ValueSet;
import org.obiba.magma.VariableEntity;
import org.obiba.magma.datasource.neo4j.converter.Neo4jMarshallingContext;
import org.obiba.magma.datasource.neo4j.domain.ValueSetNode;
import org.obiba.magma.datasource.neo4j.domain.ValueTableNode;
import org.obiba.magma.datasource.neo4j.domain.VariableEntityNode;
import org.obiba.magma.support.AbstractValueTable;
import org.obiba.magma.support.AbstractVariableEntityProvider;
import org.obiba.magma.support.VariableEntityBean;
import org.obiba.magma.type.DateTimeType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.neo4j.support.Neo4jTemplate;

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

  private Neo4jTemplate getNeo4jTemplate() {
    return getDatasource().getNeo4jTemplate();
  }

  Neo4jMarshallingContext createContext() {
    return getDatasource().createContext(getNode());
  }

  private void cacheVariables() {
    log.debug("Populating variable cache for table {}", getName());
    TimedExecution timedExecution = new TimedExecution().start();
    addVariableValueSources(new Neo4jVariableValueSourceFactory(this).createSources());
    log.debug("Populating variable cache for {}: {} variables loaded in {}", getName(), getSources().size(),
        timedExecution.end().formatExecutionTime());
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

      ValueTableNode tableNode = getNode();
//      getDatasource().getNeo4jTemplate().fetch(tableNode.getValueSets());
      for(ValueSetNode valueSetNode : tableNode.getValueSets()) {
        VariableEntityNode variableEntityNode = valueSetNode.getVariableEntity();
        getNeo4jTemplate().fetch(variableEntityNode);
        entities.add(new VariableEntityBean(variableEntityNode.getType(), variableEntityNode.getIdentifier()));
      }
      log.debug("Populating entity cache for {}: {} entities loaded in {}", getName(), entities.size(),
          timedExecution.end().formatExecutionTime());
    }

    @Override
    public Set<VariableEntity> getVariableEntities() {
      return Collections.unmodifiableSet(entities);
    }
  }
}
