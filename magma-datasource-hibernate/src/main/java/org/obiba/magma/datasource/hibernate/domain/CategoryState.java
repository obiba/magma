/*
 * Copyright (c) 2017 OBiBa. All rights reserved.
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

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "category")
@SuppressWarnings("UnusedDeclaration")
public class CategoryState extends AbstractAttributeAwareEntity implements Timestamped {

  private static final long serialVersionUID = 1L;

  @ManyToOne(optional = false)
  @JoinColumn(name = "variable_id", nullable = false)
  private VariableState variable;

  @Column(nullable = false)
  private String name;

  private String code;

  @Column(nullable = false)
  private boolean missing;

  @ElementCollection // always cascaded
  @CollectionTable(name = "category_attributes", joinColumns = @JoinColumn(name = "category_id"))
  private List<AttributeState> attributes;

  public CategoryState() { }

  public CategoryState(String name, String code, boolean missing) {
    this.name = name;
    this.code = code;
    this.missing = missing;
  }

  public VariableState getVariable() {
    return variable;
  }

  public void setVariable(VariableState variable) {
    this.variable = variable;
  }

  public String getCode() {
    return code;
  }

  public void setCode(String code) {
    this.code = code;
  }

  public void setMissing(Boolean missing) {
    this.missing = missing;
  }

  public String getName() {
    return name;
  }

  public boolean isMissing() {
    return missing;
  }

  @Override
  public List<AttributeState> getAttributes() {
    return attributes == null ? (attributes = new ArrayList<>()) : attributes;
  }

  @Override
  public void setAttributes(List<AttributeState> attributes) {
    this.attributes = attributes;
  }

}
