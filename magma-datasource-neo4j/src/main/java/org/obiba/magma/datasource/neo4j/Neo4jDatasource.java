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
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.Resource;

import org.obiba.magma.NoSuchDatasourceException;
import org.obiba.magma.NoSuchValueTableException;
import org.obiba.magma.ValueTable;
import org.obiba.magma.ValueTableWriter;
import org.obiba.magma.datasource.neo4j.converter.Neo4jMarshallingContext;
import org.obiba.magma.datasource.neo4j.domain.DatasourceNode;
import org.obiba.magma.datasource.neo4j.domain.ValueTableNode;
import org.obiba.magma.datasource.neo4j.repository.DatasourceRepository;
import org.obiba.magma.datasource.neo4j.repository.ValueTableRepository;
import org.obiba.magma.datasource.neo4j.repository.VariableRepository;
import org.obiba.magma.support.AbstractDatasource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.neo4j.support.Neo4jTemplate;
import org.springframework.util.Assert;

@SuppressWarnings("SpringJavaAutowiringInspection")
public class Neo4jDatasource extends AbstractDatasource {

  private static final Logger log = LoggerFactory.getLogger(Neo4jDatasource.class);

  public static final String TYPE = "neo4j";

  @Resource
  private DatasourceRepository datasourceRepository;

  @Resource
  private ValueTableRepository valueTableRepository;

  @Resource
  private VariableRepository variableRepository;

  @Resource
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

  DatasourceNode getNode() throws NoSuchDatasourceException {
    try {
      DatasourceNode datasourceNode = neo4jTemplate.findOne(graphId, DatasourceNode.class);
      neo4jTemplate.fetch(datasourceNode);
      return datasourceNode;
    } catch(Exception e) {
      throw new NoSuchDatasourceException(getName());
    }
  }

  @Nullable
  private ValueTableNode getValueTableNode(String tableName) {
    ValueTableNode tableNode = valueTableRepository.findByDatasourceAndName(getNode(), tableName);
    if(tableNode != null) neo4jTemplate.fetch(tableNode);
    return tableNode;
  }

  @Nonnull
  @Override
  public ValueTableWriter createWriter(@Nonnull String tableName, @Nonnull String entityType) {
    Assert.notNull(tableName, "tableName cannot be null");
    Assert.notNull(entityType, "entityType cannot be null");

    ValueTableNode tableNode = getValueTableNode(tableName);
    if(tableNode == null) {
      tableNode = neo4jTemplate.save(new ValueTableNode(tableName, entityType, getNode()));
      neo4jTemplate.fetch(tableNode);
    }
    return new Neo4jValueTableWriter(new Neo4jValueTable(this, tableNode));
  }

  @Override
  public boolean hasValueTable(String tableName) {
    return getValueTableNode(tableName) != null;
  }

  @Override
  protected Set<String> getValueTableNames() {
    Set<String> names = new LinkedHashSet<String>();
    for(ValueTableNode tableNode : getNode().getValueTables()) {
      neo4jTemplate.fetch(tableNode);
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

  Neo4jMarshallingContext createContext(ValueTableNode valueTableNode) {
    return Neo4jMarshallingContext.create(neo4jTemplate, variableRepository, getNode(), valueTableNode);
  }
}