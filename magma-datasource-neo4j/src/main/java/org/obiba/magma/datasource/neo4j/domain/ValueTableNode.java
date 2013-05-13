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

import org.neo4j.graphdb.Direction;
import org.springframework.data.neo4j.annotation.Indexed;
import org.springframework.data.neo4j.annotation.NodeEntity;
import org.springframework.data.neo4j.annotation.RelatedTo;
import org.springframework.data.neo4j.support.index.IndexType;

@NodeEntity
public class ValueTableNode extends AbstractGraphItem {

  @Indexed(indexType = IndexType.FULLTEXT, indexName = "table")
  private String name;

  @Indexed(indexType = IndexType.FULLTEXT, indexName = "table")
  private String entityType;

  @RelatedTo(type = "HAS_TABLES", direction = Direction.INCOMING)
  private DatasourceNode datasource;

  @RelatedTo(type = "HAS_VARIABLES", direction = Direction.OUTGOING)
  private List<VariableNode> variables;

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

}
