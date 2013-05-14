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

import org.springframework.data.neo4j.annotation.Indexed;
import org.springframework.data.neo4j.annotation.NodeEntity;
import org.springframework.data.neo4j.annotation.RelatedTo;
import org.springframework.data.neo4j.support.index.IndexType;

import static org.neo4j.graphdb.Direction.INCOMING;

@NodeEntity
public class CategoryNode extends AbstractAttributeAwareNode {

  @Indexed(indexType = IndexType.FULLTEXT, indexName = "category")
  private String name;

  private String code;

  private boolean missing;

  @RelatedTo(type = "HAS_CATEGORIES", direction = INCOMING)
  private List<VariableNode> variables;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getCode() {
    return code;
  }

  public void setCode(String code) {
    this.code = code;
  }

  public boolean isMissing() {
    return missing;
  }

  public void setMissing(boolean missing) {
    this.missing = missing;
  }

  public List<VariableNode> getVariables() {
    return variables;
  }

  public void setVariables(List<VariableNode> variables) {
    this.variables = variables;
  }
}
