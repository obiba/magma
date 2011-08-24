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
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.hibernate.annotations.IndexColumn;

@Entity
@Table(name = "value_table", uniqueConstraints = { @UniqueConstraint(columnNames = { "datasource_id", "name" }) })
public class ValueTableState extends AbstractTimestampedEntity {

  private static final long serialVersionUID = 1L;

  @Column(nullable = false)
  private String name;

  @Column(nullable = false)
  private String entityType;

  @ManyToOne(optional = false)
  @JoinColumn(name = "datasource_id")
  private DatasourceState datasource;

  @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
  // Creates a column to store the category's index within the list
  @IndexColumn(name = "variable_index", nullable = false)
  // Used to prevent an association table from being created
  @JoinColumn(name = "value_table_id", nullable = false)
  // Not supported: https://hibernate.onjira.com/browse/HHH-6382
  // @OnDelete(action = OnDeleteAction.CASCADE)
  private List<VariableState> variables;

  public ValueTableState() {
    super();
  }

  public ValueTableState(String name, String entityType, DatasourceState datasource) {
    super();
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

  public DatasourceState getDatasource() {
    return datasource;
  }

  public List<VariableState> getVariables() {
    return variables != null ? variables : (variables = new ArrayList<VariableState>());
  }

}
