/*
 * Copyright (c) 2011 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.magma.datasource.hibernate.domain;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.IndexColumn;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.obiba.magma.ValueType;
import org.obiba.magma.Variable;
import org.obiba.magma.datasource.hibernate.type.ValueTypeHibernateType;

@Entity
@Table(name = "variable", uniqueConstraints = @UniqueConstraint(columnNames = { "value_table_id", "name" }))
@TypeDef(name = "value_type", typeClass = ValueTypeHibernateType.class)
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@NamedQuery(name = "allValues",
    query = "select vs.variableEntity.identifier, vsv.value, vs.id from ValueSetState as vs " + //
        "left outer join vs.values as vsv with vsv.id.variable.id = :variableId " + //
        "where vs.valueTable.id = :valueTableId " + //
        "order by vs.variableEntity.identifier")
public class VariableState extends AbstractAttributeAwareEntity implements Timestamped {

  private static final long serialVersionUID = 1L;

  @Column(nullable = false)
  private String name;

  @ManyToOne(optional = false)
  @JoinColumn(name = "value_table_id", insertable = false, updatable = false)
  private ValueTableState valueTable;

  @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
  // Creates a column to store the category's index within the list
  @IndexColumn(name = "category_index", nullable = false)
  // Used to prevent an association table from being created
  @JoinColumn(name = "variable_id", nullable = false)
  private List<CategoryState> categories;

  @Column(nullable = false)
  private String entityType;

  private String mimeType;

  private String occurrenceGroup;

  private String referencedEntityType;

  private String unit;

  @Type(type = "value_type")
  @Column(nullable = false)
  private ValueType valueType;

  @Column(nullable = false)
  private boolean repeatable;

  @SuppressWarnings("UnusedDeclaration")
  public VariableState() {
  }

  public VariableState(ValueTableState valueTable, Variable variable) {
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

  public ValueTableState getValueTable() {
    return valueTable;
  }

  public void setValueTable(ValueTableState valueTable) {
    this.valueTable = valueTable;
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

  public void setRepeatable(boolean repeatable) {
    this.repeatable = repeatable;
  }

  public boolean isRepeatable() {
    return repeatable;
  }

  public List<CategoryState> getCategories() {
    return categories != null ? categories : (categories = new ArrayList<>());
  }

  public void addCategory(CategoryState state) {
    getCategories().add(state);
  }

  public CategoryState getCategory(String categoryName) {
    for(CategoryState state : getCategories()) {
      if(categoryName.equals(state.getName())) {
        return state;
      }
    }
    return null;
  }

  public int getCategoryIndex(String categoryName) {
    int index = 0;
    for(CategoryState state : getCategories()) {
      if(categoryName.equals(state.getName())) {
        return index;
      }
      index++;
    }
    return -1;
  }
}
