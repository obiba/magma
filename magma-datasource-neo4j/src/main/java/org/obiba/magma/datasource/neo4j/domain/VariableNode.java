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

import org.obiba.magma.ValueType;
import org.springframework.data.neo4j.annotation.GraphId;
import org.springframework.data.neo4j.annotation.Indexed;
import org.springframework.data.neo4j.annotation.RelatedTo;
import org.springframework.data.neo4j.support.index.IndexType;

//@NodeEntity
public class VariableNode {

  @GraphId
  Long nodeId;

  @Indexed(indexType = IndexType.FULLTEXT, indexName = "variable")
  private String name;

  @RelatedTo
  private ValueTableNode valueTable;

  @RelatedTo
  private List<CategoryNode> categories;

  private String entityType;

  private String mimeType;

  private String occurrenceGroup;

  private String referencedEntityType;

  private String unit;

  private ValueType valueType;

  private boolean repeatable;

  @Override
  public boolean equals(Object o) {
    if(this == o) return true;
    if(o == null || getClass() != o.getClass()) return false;

    VariableNode node = (VariableNode) o;
    if(nodeId == null) return super.equals(o);
    return nodeId.equals(node.nodeId);

  }

  @Override
  public int hashCode() {
    return nodeId == null ? super.hashCode() : nodeId.hashCode();
  }
}
