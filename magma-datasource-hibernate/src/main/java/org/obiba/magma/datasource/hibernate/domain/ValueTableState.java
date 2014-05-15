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
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderColumn;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

@Entity
@Table(name = "value_table", uniqueConstraints = @UniqueConstraint(columnNames = { "datasource_id", "name" }))
@SuppressWarnings("UnusedDeclaration")
public class ValueTableState extends AbstractTimestampedEntity {

  private static final long serialVersionUID = 1L;

  @Column(nullable = false)
  private String name;

  @Column(nullable = false)
  private String entityType;

  @ManyToOne(optional = false)
  @JoinColumn(name = "datasource_id")
  private DatasourceState datasource;

  @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true, mappedBy = "valueTable")
  @OrderColumn(name = "variable_index")
  private List<VariableState> variables;

  public ValueTableState() { }

  public ValueTableState(String name, String entityType, DatasourceState datasource) {
    this.name = name;
    this.entityType = entityType;
    this.datasource = datasource;
  }

  public String getEntityType() {
    return entityType;
  }

  public void setEntityType(String entityType) {
    this.entityType = entityType;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public DatasourceState getDatasource() {
    return datasource;
  }

  public List<VariableState> getVariables() {
    return variables == null ? (variables = new ArrayList<>()) : variables;
  }

}
