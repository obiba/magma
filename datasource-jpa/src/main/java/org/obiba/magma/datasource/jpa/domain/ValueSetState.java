package org.obiba.magma.datasource.jpa.domain;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.obiba.core.domain.AbstractEntity;

@Entity
@Table(name = "value_set", uniqueConstraints = { @UniqueConstraint(columnNames = { "value_table_id", "variable_entity_id" }) })
public class ValueSetState extends AbstractEntity {

  private static final long serialVersionUID = 1L;

  @ManyToOne(optional = false)
  @JoinColumn(name = "value_table_id")
  private ValueTableState valueTable;

  @ManyToOne(optional = false)
  @JoinColumn(name = "variable_entity_id")
  private VariableEntityState variableEntity;

  public ValueSetState() {
    super();
  }

  public ValueSetState(ValueTableState valueTable, VariableEntityState variableEntity) {
    super();
    this.valueTable = valueTable;
    this.variableEntity = variableEntity;
  }

  public VariableEntityState getVariableEntity() {
    return variableEntity;
  }

  public ValueTableState getValueTable() {
    return valueTable;
  }

}
