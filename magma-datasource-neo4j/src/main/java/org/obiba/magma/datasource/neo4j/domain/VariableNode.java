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

import java.util.Objects;
import java.util.Set;

import javax.annotation.Nullable;

import org.obiba.magma.ValueType;
import org.obiba.magma.Variable;
import org.springframework.data.neo4j.annotation.Indexed;
import org.springframework.data.neo4j.annotation.NodeEntity;
import org.springframework.data.neo4j.annotation.RelatedTo;

import static org.neo4j.graphdb.Direction.INCOMING;
import static org.neo4j.graphdb.Direction.OUTGOING;
import static org.springframework.data.neo4j.support.index.IndexType.FULLTEXT;

@NodeEntity
public class VariableNode extends AbstractAttributeAwareNode {

  @Indexed(indexType = FULLTEXT, indexName = "variable")
  private String name;

  @RelatedTo(type = "HAS_VARIABLES", direction = INCOMING)
  private ValueTableNode valueTable;

  @RelatedTo(type = "HAS_CATEGORIES", direction = OUTGOING)
  private Set<CategoryNode> categories;

  @RelatedTo(type = "HAS_VALUE_SET_VALUES", direction = OUTGOING)
  private Set<ValueSetValueNode> valueSetValues;

  private String entityType;

  private String mimeType;

  private String occurrenceGroup;

  private String referencedEntityType;

  private String unit;

  private ValueType valueType;

  private boolean repeatable;

  public VariableNode() {}

  public VariableNode(ValueTableNode valueTable, Variable variable) {
    this.valueTable = valueTable;
    name = variable.getName();
    copyVariableFields(variable);
  }

  /**
   * Copies all fields of the specified {@link Variable} (but not its name).
   *
   * @param variable variable
   */
  public void copyVariableFields(Variable variable) {
    entityType = variable.getEntityType();
    valueType = variable.getValueType();
    mimeType = variable.getMimeType();
    occurrenceGroup = variable.getOccurrenceGroup();
    referencedEntityType = variable.getReferencedEntityType();
    unit = variable.getUnit();
    repeatable = variable.isRepeatable();
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public ValueTableNode getValueTable() {
    return valueTable;
  }

  public void setValueTable(ValueTableNode valueTable) {
    this.valueTable = valueTable;
  }

  public Set<CategoryNode> getCategories() {
    return categories;
  }

  public void setCategories(Set<CategoryNode> categories) {
    this.categories = categories;
  }

  @Nullable
  public CategoryNode getCategory(String categoryName) {
    for(CategoryNode category : getCategories()) {
      if(Objects.equals(categoryName, category.getName())) {
        return category;
      }
    }
    return null;
  }

  public String getEntityType() {
    return entityType;
  }

  public void setEntityType(String entityType) {
    this.entityType = entityType;
  }

  public String getMimeType() {
    return mimeType;
  }

  public void setMimeType(String mimeType) {
    this.mimeType = mimeType;
  }

  public String getOccurrenceGroup() {
    return occurrenceGroup;
  }

  public void setOccurrenceGroup(String occurrenceGroup) {
    this.occurrenceGroup = occurrenceGroup;
  }

  public String getReferencedEntityType() {
    return referencedEntityType;
  }

  public void setReferencedEntityType(String referencedEntityType) {
    this.referencedEntityType = referencedEntityType;
  }

  public String getUnit() {
    return unit;
  }

  public void setUnit(String unit) {
    this.unit = unit;
  }

  public ValueType getValueType() {
    return valueType;
  }

  public void setValueType(ValueType valueType) {
    this.valueType = valueType;
  }

  public boolean isRepeatable() {
    return repeatable;
  }

  public void setRepeatable(boolean repeatable) {
    this.repeatable = repeatable;
  }

  public Set<ValueSetValueNode> getValueSetValues() {
    return valueSetValues;
  }

  public void setValueSetValues(Set<ValueSetValueNode> valueSetValues) {
    this.valueSetValues = valueSetValues;
  }
}
