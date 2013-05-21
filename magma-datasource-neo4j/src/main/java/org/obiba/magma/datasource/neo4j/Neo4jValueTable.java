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
import org.springframework.util.Assert;

import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;

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
    for(VariableNode variableNode : getNode().getVariables()) {
      addVariableValueSource(new Neo4jVariableValueSource(variableNode, true));
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

    private final String variableName;

    private Long graphId;

    private Variable variable;

    Neo4jVariableValueSource(@Nonnull VariableNode node, @Nonnull Variable variable) {
      Assert.notNull(node, "VariableNode cannot be null");
      Assert.notNull(variable, "variable cannot be null");

      getNeo4jTemplate().fetch(node);
      variableName = node.getName();
      graphId = node.getGraphId();
      this.variable = variable;
    }

    Neo4jVariableValueSource(@Nonnull VariableNode node, boolean unmarshall) {
      Assert.notNull(node, "VariableNode cannot be null");

      getNeo4jTemplate().fetch(node);

      variableName = node.getName();
      graphId = node.getGraphId();

      if(unmarshall) {
        getNeo4jTemplate().fetch(node.getCategories());
        unmarshall(node);
      }
    }

    Long getGraphId() {
      return graphId;
    }

    @Override
    public Variable getVariable() {
      if(variable == null) {
        unmarshall(getNeo4jTemplate().findOne(ensureGraphId(), VariableNode.class));
      }
      return variable;
    }

    @Nonnull
    @Override
    public Value getValue(ValueSet valueSet) {
//      ValueSetNode valueSetNode = getNeo4jTemplate()
//          .query("start p=(%person) match p<-[:WORKS_WITH]-other return other.name", map("table", thomas))
//          .to(ValueSetNode.class).single();
//      HibernateValueSet hibernateValueSet = (HibernateValueSet) valueSet;
//      ValueSetValue vsv = hibernateValueSet.getValueSetState().getValueMap().get(variableName);
//      if(vsv == null) {
//        return getVariable().isRepeatable() ? getValueType().nullSequence() : getValueType().nullValue();
//      }
//      return getVariable().getValueType().equals(BinaryType.get()) //
//          ? getBinaryValue(vsv) //
//          : vsv.getValue();
      return null;
    }

    @Nullable
    @Override
    public VectorSource asVectorSource() {
      return this;
    }

    @Override
    public Iterable<Value> getValues(final SortedSet<VariableEntity> entities) {
      if(entities.isEmpty()) {
        return ImmutableList.of();
      }
      return null;

      // This will returns one row per value set in the value table (so it includes nulls)
//      final Query valuesQuery = getCurrentSession().getNamedQuery("allValues")//
//          .setParameter("valueTableId", valueTable.getValueTableState().getId())//
//          .setParameter("variableId", ensureVariableId());
//      return new Iterable<Value>() {
//
//        @Override
//        public Iterator<Value> iterator() {
//
//          return new Iterator<Value>() {
//
//            private final ScrollableResults results;
//
//            private final Iterator<VariableEntity> resultEntities;
//
//            private boolean hasNextResults;
//
//            private boolean closed;
//
//            {
//              resultEntities = entities.iterator();
//              results = valuesQuery.scroll(ScrollMode.FORWARD_ONLY);
//              hasNextResults = results.next();
//            }
//
//            @Override
//            public boolean hasNext() {
//              return resultEntities.hasNext();
//            }
//
//            @Override
//            public Value next() {
//              if(!hasNext()) {
//                throw new NoSuchElementException();
//              }
//
//              String nextEntity = resultEntities.next().getIdentifier();
//
//              // Scroll until we find the required entity or reach the end of the results
//              while(hasNextResults && !results.getString(0).equals(nextEntity)) {
//                hasNextResults = results.next();
//              }
//
//              Value value = null;
//              if(hasNextResults) {
//                value = (Value) results.get(1);
//              }
//              closeCursorIfNecessary();
//
//              return value != null
//                  ? value
//                  : getVariable().isRepeatable() ? getValueType().nullSequence() : getValueType().nullValue();
//            }
//
//            @Override
//            public void remove() {
//              throw new UnsupportedOperationException();
//            }
//
//            private void closeCursorIfNecessary() {
//              if(!closed) {
//                // Close the cursor if we don't have any more results or no more entities to return
//                if(!hasNextResults || !hasNext()) {
//                  closed = true;
//                  results.close();
//                }
//              }
//            }
//
//          };
//        }
//
//      };

    }

    @Nonnull
    @Override
    public ValueType getValueType() {
      return getVariable().getValueType();
    }

    /**
     * Initialises the {@code variable} attribute from the provided state
     *
     * @param state
     */
    private void unmarshall(VariableNode variableNode) {
      variable = VariableConverter.getInstance().unmarshal(variableNode, null);
    }

    private Long ensureGraphId() {
      if(graphId == null) {
        VariableNode variableNode = Iterables.find(getNode().getVariables(), new Predicate<VariableNode>() {
          @Override
          public boolean apply(VariableNode input) {
            getNeo4jTemplate().fetch(input);
            return Objects.equals(input.getName(), variableName);
          }
        }, null);
        if(variableNode == null) throw new IllegalStateException("variable '" + variableName + "' not persisted yet.");
        graphId = variableNode.getGraphId();
      }
      return graphId;
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
      return Objects.equals(((Neo4jVariableValueSource) obj).variableName, variableName);
    }

    @Override
    public int hashCode() {
      return variableName.hashCode();
    }
  }
}
