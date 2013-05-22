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
import org.obiba.magma.Value;
import org.obiba.magma.ValueTable;
import org.obiba.magma.ValueTableWriter;
import org.obiba.magma.datasource.neo4j.converter.AttributeAwareConverter;
import org.obiba.magma.datasource.neo4j.converter.Neo4jMarshallingContext;
import org.obiba.magma.datasource.neo4j.converter.ValueConverter;
import org.obiba.magma.datasource.neo4j.domain.AttributeNode;
import org.obiba.magma.datasource.neo4j.domain.DatasourceNode;
import org.obiba.magma.datasource.neo4j.domain.ValueTableNode;
import org.obiba.magma.datasource.neo4j.repository.DatasourceRepository;
import org.obiba.magma.datasource.neo4j.repository.ValueRepository;
import org.obiba.magma.datasource.neo4j.repository.ValueSetRepository;
import org.obiba.magma.datasource.neo4j.repository.ValueTableRepository;
import org.obiba.magma.datasource.neo4j.repository.VariableEntityRepository;
import org.obiba.magma.datasource.neo4j.repository.VariableRepository;
import org.obiba.magma.support.AbstractDatasource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.neo4j.support.Neo4jTemplate;

import static org.springframework.util.Assert.notNull;

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
  private VariableEntityRepository variableEntityRepository;

  @Resource
  private ValueRepository valueRepository;

  @Resource
  private ValueSetRepository valueSetRepository;

  @Resource
  private Neo4jTemplate neo4jTemplate;

  private Long graphId;

  public Neo4jDatasource(String name) {
    super(name, TYPE);
  }

  @Override
  protected void onInitialise() {
    DatasourceNode datasourceNode = datasourceRepository.findByName(getName());
    Neo4jMarshallingContext context = createContext();
    if(datasourceNode == null) {
      datasourceNode = neo4jTemplate.save(new DatasourceNode(getName()));
      AttributeAwareConverter.getInstance().addAttributes(this, datasourceNode, context);
    }
    graphId = datasourceNode.getGraphId();

    ValueConverter valueConverter = ValueConverter.getInstance();
    neo4jTemplate.fetch(datasourceNode.getAttributes());
    for(AttributeNode attributeNode : datasourceNode.getAttributes()) {
      neo4jTemplate.fetch(attributeNode);
      neo4jTemplate.fetch(attributeNode.getValue());
      Value value = valueConverter.unmarshal(attributeNode.getValue(), context);
//      Attribute attribute = Attribute.Builder.newAttribute().withName(attributeNode.getName())
//          .withNamespace(attributeNode.getNamespace()).withLocale(attributeNode.getLocale()).withValue(value).build();
      setAttributeValue(attributeNode.getName(), value);
    }
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
      neo4jTemplate.fetch(datasourceNode.getAttributes());
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
    notNull(tableName, "tableName cannot be null");
    notNull(entityType, "entityType cannot be null");

    Neo4jValueTable valueTable = null;
    if(hasValueTable(tableName)) {
      valueTable = (Neo4jValueTable) getValueTable(tableName);
    } else {
      ValueTableNode tableNode = neo4jTemplate.save(new ValueTableNode(tableName, entityType, getNode()));
      neo4jTemplate.fetch(tableNode);
      valueTable = new Neo4jValueTable(this, tableNode);
      addValueTable(valueTable);
    }
    return new Neo4jValueTableWriter(valueTable);
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
    try {
      return super.getValueTable(tableName);
    } catch(NoSuchValueTableException e) {
      ValueTableNode tableNode = getValueTableNode(tableName);
      if(tableNode == null) throw new NoSuchValueTableException(tableName);
      return new Neo4jValueTable(this, tableNode);
    }
  }

  @Override
  protected ValueTable initialiseValueTable(@Nonnull String tableName) {
    return new Neo4jValueTable(this, getValueTableNode(tableName));
  }

  Neo4jTemplate getNeo4jTemplate() {
    return neo4jTemplate;
  }

  DatasourceRepository getDatasourceRepository() {
    return datasourceRepository;
  }

  ValueTableRepository getValueTableRepository() {
    return valueTableRepository;
  }

  VariableRepository getVariableRepository() {
    return variableRepository;
  }

  ValueRepository getValueRepository() {
    return valueRepository;
  }

  public ValueSetRepository getValueSetRepository() {
    return valueSetRepository;
  }

  public VariableEntityRepository getVariableEntityRepository() {
    return variableEntityRepository;
  }

  Neo4jMarshallingContext createContext() {
    return Neo4jMarshallingContext.create(neo4jTemplate, variableRepository, null, null);
  }

  Neo4jMarshallingContext createContext(ValueTableNode valueTableNode) {
    return Neo4jMarshallingContext.create(neo4jTemplate, variableRepository, getNode(), valueTableNode);
  }

}