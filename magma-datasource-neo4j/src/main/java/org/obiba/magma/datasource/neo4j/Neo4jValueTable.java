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

import javax.annotation.Nonnull;

import org.obiba.magma.Datasource;
import org.obiba.magma.NoSuchValueSetException;
import org.obiba.magma.NoSuchValueTableException;
import org.obiba.magma.Timestamps;
import org.obiba.magma.Value;
import org.obiba.magma.ValueSet;
import org.obiba.magma.VariableEntity;
import org.obiba.magma.datasource.neo4j.domain.ValueTableNode;
import org.obiba.magma.support.AbstractValueTable;
import org.obiba.magma.type.DateTimeType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Neo4jValueTable extends AbstractValueTable {

  private static final Logger log = LoggerFactory.getLogger(Neo4jValueTable.class);

  private final Long graphId;

  public Neo4jValueTable(Datasource datasource, @Nonnull ValueTableNode valueTableNode) {
    super(datasource, valueTableNode.getName());
    graphId = valueTableNode.getGraphId();
    log.debug("ValueTable: {}, graphId: {}", valueTableNode.getName(), graphId);
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
  private ValueTableNode getNode() throws NoSuchValueTableException {
    try {
      return getDatasource().getNeo4jTemplate().findOne(graphId, ValueTableNode.class);
    } catch(Exception e) {
      throw new NoSuchValueTableException(getName());
    }
  }
}
