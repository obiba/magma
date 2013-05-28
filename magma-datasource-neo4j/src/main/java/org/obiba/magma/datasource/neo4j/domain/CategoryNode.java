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

import org.obiba.magma.Category;
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
  private Set<VariableNode> variables;

  public CategoryNode() {
  }

  public CategoryNode(Category category) {
    copyCategoryFields(category);
  }

  public void copyCategoryFields(Category category) {
    name = category.getName();
    code = category.getCode();
    missing = category.isMissing();
  }

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

  public Set<VariableNode> getVariables() {
    return variables;
  }

  public void setVariables(Set<VariableNode> variables) {
    this.variables = variables;
  }
}
