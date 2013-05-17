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
import org.springframework.data.neo4j.annotation.RelatedTo;

import static org.neo4j.graphdb.Direction.OUTGOING;
import static org.springframework.data.neo4j.support.index.IndexType.FULLTEXT;

@NodeEntity
public class VariableEntityNode extends AbstractTimestampedGraphItem {

  @Indexed(indexType = FULLTEXT, indexName = "variable_entity")
  private String identifier;

  @Indexed(indexType = FULLTEXT, indexName = "variable_entity")
  private String type;

  @RelatedTo(type = "HAS_ENTITIES", direction = OUTGOING)
  private Set<ValueSetNode> valueSets;

  public VariableEntityNode() {
  }

  public VariableEntityNode(String identifier, String type) {
    this.identifier = identifier;
    this.type = type;
  }

  public String getIdentifier() {
    return identifier;
  }

  public void setIdentifier(String identifier) {
    this.identifier = identifier;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public Set<ValueSetNode> getValueSets() {
    return valueSets;
  }

  public void setValueSets(Set<ValueSetNode> valueSets) {
    this.valueSets = valueSets;
  }
}
