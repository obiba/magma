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

import java.util.Set;

import org.springframework.data.neo4j.annotation.Indexed;
import org.springframework.data.neo4j.annotation.NodeEntity;
import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.annotation.RelatedTo;

import static org.neo4j.graphdb.Direction.INCOMING;
import static org.neo4j.graphdb.Direction.OUTGOING;
import static org.springframework.data.neo4j.support.index.IndexType.FULLTEXT;

@NodeEntity
public class ValueTableNode extends AbstractTimestampedGraphItem {

  @Indexed(indexType = FULLTEXT, indexName = "table")
  private String name;

  private String entityType;

  @RelatedTo(type = "HAS_TABLES", direction = INCOMING)
  private DatasourceNode datasource;

  @RelatedTo(type = "HAS_VARIABLES", direction = OUTGOING)
  private Set<VariableNode> variables;

  @RelatedTo(type = "HAS_VALUE_SETS", direction = OUTGOING)
  private Set<ValueSetNode> valueSets;

  @Query("start table=node({self}) " +
      "match (table)-[:HAS_VALUE_SETS]->(valueSets)<-[:ENTITIES_HAS_VALUE_SETS]-(variableEntity) " +
      "return variableEntity")
  private Iterable<VariableEntityNode> variableEntities;

  public ValueTableNode() {
  }

  public ValueTableNode(String name, String entityType, DatasourceNode datasource) {
    this.name = name;
    this.entityType = entityType;
    this.datasource = datasource;
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

  public Set<VariableNode> getVariables() {
    return variables;
  }

  public void setVariables(Set<VariableNode> variables) {
    this.variables = variables;
  }

  public Set<ValueSetNode> getValueSets() {
    return valueSets;
  }

  public void setValueSets(Set<ValueSetNode> valueSets) {
    this.valueSets = valueSets;
  }

  public Iterable<VariableEntityNode> getVariableEntities() {
    return variableEntities;
  }

  public void setVariableEntities(Iterable<VariableEntityNode> variableEntities) {
    this.variableEntities = variableEntities;
  }
}
