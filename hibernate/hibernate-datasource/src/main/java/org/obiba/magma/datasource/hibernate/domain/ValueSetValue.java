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

import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.hibernate.annotations.Columns;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.obiba.magma.Value;
import org.obiba.magma.hibernate.type.ValueHibernateType;

import com.google.common.collect.Sets;

@SuppressWarnings("UnusedDeclaration")
@Entity
@Table(name = "value_set_value",
    uniqueConstraints = @UniqueConstraint(columnNames = {"value_set_id", "variable_id"}))
@TypeDef(name = "value", typeClass = ValueHibernateType.class)
@NamedQuery(name = "findValuesByTable",
    query = "SELECT vsv FROM ValueSetValue vsv WHERE vsv.valueSet.id " + //
        "IN (SELECT vs.id FROM ValueSetState vs WHERE vs.valueTable.id = :valueTableId)")
public class ValueSetValue extends AbstractTimestampedEntity {

  private static final long serialVersionUID = 4356913652103162813L;

  @ManyToOne(optional = false)
  @JoinColumn(name = "value_set_id", referencedColumnName = "id")
  private ValueSetState valueSet;

  @ManyToOne(optional = false)
  @JoinColumn(name = "variable_id", referencedColumnName = "id")
  private VariableState variable;

  @Type(type = "value")
  @Columns(columns = { //
      @Column(name = "value_type", nullable = false), //
      @Column(name = "is_sequence", nullable = false), //
      @Column(name = "value", length = Integer.MAX_VALUE, nullable = false)})
  private Value value;

  @OrderBy("occurrence")
  @OneToMany(cascade = CascadeType.ALL, mappedBy = "valueSetValue", orphanRemoval = true, fetch = FetchType.LAZY)
  private Set<ValueSetBinaryValue> binaryValues;

  @SuppressWarnings("UnusedDeclaration")
  public ValueSetValue() {
  }

  public ValueSetValue(VariableState variable, ValueSetState valueSet) {
    if(variable == null) throw new IllegalArgumentException("variable cannot be null");
    if(valueSet == null) throw new IllegalArgumentException("valueSet cannot be null");
    this.variable = variable;
    this.valueSet = valueSet;
  }

  public void setValue(Value value) {
    if(value.isNull()) {
      throw new IllegalArgumentException("cannot persist null values");
    }
    this.value = value;
  }

  public Value getValue() {
    return value;
  }

  public Set<ValueSetBinaryValue> getBinaryValues() {
    return binaryValues;
  }

  public void addBinaryValue(ValueSetBinaryValue binaryValue) {
    if(binaryValues == null) {
      binaryValues = Sets.newLinkedHashSet();
    }
    if(binaryValues.add(binaryValue)) {
      binaryValue.setValueSetValue(this);
    }
  }

  public void removeBinaryValue(ValueSetBinaryValue binaryValue) {
    if(binaryValues != null && binaryValues.remove(binaryValue)) {
      binaryValue.setValueSetValue(null);
    }
  }

  public ValueSetState getValueSet() {
    return valueSet;
  }

  public VariableState getVariable() {
    return variable;
  }

}
