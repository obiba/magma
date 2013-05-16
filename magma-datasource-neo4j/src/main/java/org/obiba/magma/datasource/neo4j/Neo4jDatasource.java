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

import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.obiba.magma.NoSuchValueTableException;
import org.obiba.magma.ValueTable;
import org.obiba.magma.ValueTableWriter;
import org.obiba.magma.datasource.neo4j.domain.DatasourceNode;
import org.obiba.magma.datasource.neo4j.domain.ValueTableNode;
import org.obiba.magma.datasource.neo4j.repository.DatasourceRepository;
import org.obiba.magma.support.AbstractDatasource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.neo4j.support.Neo4jTemplate;
import org.springframework.util.Assert;

public class Neo4jDatasource extends AbstractDatasource {

  private static final Logger log = LoggerFactory.getLogger(Neo4jDatasource.class);

  public static final String TYPE = "neo4j";

  @Autowired
  private DatasourceRepository datasourceRepository;

  @Autowired
  private Neo4jTemplate neo4jTemplate;

  private Long graphId;

  public Neo4jDatasource(String name) {
    super(name, TYPE);
  }

  @Override
  protected void onInitialise() {
    DatasourceNode datasourceNode = datasourceRepository.findByName(getName());
    if(datasourceNode == null) {
      datasourceNode = neo4jTemplate.save(new DatasourceNode(getName()));
    }
    graphId = datasourceNode.getGraphId();
    log.debug("Datasource: {}, graphId: {}", datasourceNode.getName(), graphId);
  }

  @Override
  protected void onDispose() {
    DatasourceNode datasourceNode = getNode();
    //TODO set attributes if needed
    neo4jTemplate.save(datasourceNode);
  }

  DatasourceNode getNode() {
    return neo4jTemplate.findOne(graphId, DatasourceNode.class);
  }

  @Nullable
  private ValueTableNode getValueTableNode(String tableName) {
    // TODO replace by a query
    Set<ValueTableNode> valueTables = getNode().getValueTables();
    neo4jTemplate.fetch(valueTables); // fetch tables properties (name)
    for(ValueTableNode tableNode : valueTables) {
      if(Objects.equals(tableName, tableNode.getName())) {
        return tableNode;
      }
    }
    return null;
  }

  @Nonnull
  @Override
  public ValueTableWriter createWriter(@Nonnull String tableName, @Nonnull String entityType) {
    Assert.notNull(tableName, "tableName cannot be null");
    Assert.notNull(entityType, "entityType cannot be null");

    ValueTableNode valueTableNode = getValueTableNode(tableName);
    if(valueTableNode == null) {
      valueTableNode = neo4jTemplate.save(new ValueTableNode(tableName, entityType, getNode()));
    }
    return new Neo4jValueTableWriter(new Neo4jValueTable(this, valueTableNode));
  }

  @Override
  public boolean hasValueTable(String tableName) {
    return getValueTableNode(tableName) != null;
  }

  @Override
  protected Set<String> getValueTableNames() {
    Set<String> names = new LinkedHashSet<String>();
    for(ValueTableNode tableNode : getNode().getValueTables()) {
      names.add(tableNode.getName());
    }
    return names;
  }

  @Override
  public ValueTable getValueTable(String tableName) throws NoSuchValueTableException {
    ValueTableNode tableNode = getValueTableNode(tableName);
    if(tableNode == null) throw new NoSuchValueTableException(tableName);
    return new Neo4jValueTable(this, tableNode);
  }

  @Override
  protected ValueTable initialiseValueTable(@Nonnull String tableName) {
    return new Neo4jValueTable(this, getValueTableNode(tableName));
  }

  public Neo4jTemplate getNeo4jTemplate() {
    return neo4jTemplate;
  }

  public DatasourceRepository getDatasourceRepository() {
    return datasourceRepository;
  }
}