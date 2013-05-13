/*
 * Copyright (c) 2013 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.magma.datasource.neo4j.domain;

import java.util.List;

import org.springframework.data.neo4j.annotation.GraphId;
import org.springframework.data.neo4j.annotation.Indexed;
import org.springframework.data.neo4j.annotation.NodeEntity;
import org.springframework.data.neo4j.annotation.RelatedTo;
import org.springframework.data.neo4j.support.index.IndexType;

@NodeEntity
public class ValueTableNode {

  @GraphId
  private Long nodeId;

  @Indexed(indexType = IndexType.FULLTEXT, indexName = "table")
  private String name;

  private String entityType;

  @RelatedTo
  private DatasourceNode datasource;

  //  @RelatedTo
  private List<VariableNode> variables;

  public Long getNodeId() {
    return nodeId;
  }

  public void setNodeId(Long nodeId) {
    this.nodeId = nodeId;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getEntityType() {
    return entityType;
  }

  public void setEntityType(String entityType) {
    this.entityType = entityType;
  }

  public DatasourceNode getDatasource() {
    return datasource;
  }

  public void setDatasource(DatasourceNode datasource) {
    this.datasource = datasource;
  }

  public List<VariableNode> getVariables() {
    return variables;
  }

  public void setVariables(List<VariableNode> variables) {
    this.variables = variables;
  }

  @Override
  public boolean equals(Object o) {
    if(this == o) return true;
    if(o == null || getClass() != o.getClass()) return false;

    ValueTableNode node = (ValueTableNode) o;
    if(nodeId == null) return super.equals(o);
    return nodeId.equals(node.nodeId);

  }

  @Override
  public int hashCode() {
    return nodeId == null ? super.hashCode() : nodeId.hashCode();
  }
}
