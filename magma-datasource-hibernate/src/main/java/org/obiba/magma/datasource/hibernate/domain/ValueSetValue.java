/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.magma.datasource.hibernate.domain;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Version;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.Columns;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.obiba.magma.Value;
import org.obiba.magma.datasource.hibernate.type.ValueHibernateType;

import com.google.common.base.Objects;

@Entity
@Table(name = "value_set_value")
@TypeDef(name = "value", typeClass = ValueHibernateType.class)
@NamedQueries({ //
    @NamedQuery(name = "findValuesByTable",
        query = "SELECT vsv FROM ValueSetValue vsv WHERE vsv.id.valueSet " + //
            "IN (SELECT vs.id FROM ValueSetState vs WHERE vs.valueTable.id = :valueTableId)"),
    @NamedQuery(name = "deleteValueSetValues",
        query = "DELETE FROM ValueSetValue WHERE id.valueSet.id IN (:valueSetIds)"),
    @NamedQuery(name = "deleteVariableValueSetValues",
        query = "DELETE FROM ValueSetValue WHERE id.variable.id = :variableId") })
public class ValueSetValue implements Timestamped, Serializable {

  private static final long serialVersionUID = 4356913652103162813L;

  @EmbeddedId
  private ValueSetValueId id;

  @Type(type = "value")
  @Columns(columns = { //
      @Column(name = "value_type", nullable = false), //
      @Column(name = "is_sequence", nullable = false), //
      @Column(name = "value", length = Integer.MAX_VALUE, nullable = false) })
  private Value value;

  @SuppressWarnings("FieldMayBeFinal")
  @Temporal(TemporalType.TIMESTAMP)
  @Column(insertable = true, updatable = false, nullable = false)
  private Date created = new Date();

  @Version
  @Column(nullable = false)
  private Date updated;

  @SuppressWarnings("UnusedDeclaration")
  public ValueSetValue() {
  }

  public ValueSetValue(@NotNull VariableState variable, @NotNull ValueSetState valueSet) {
    //noinspection ConstantConditions
    if(variable == null) throw new IllegalArgumentException("variable cannot be null");
    //noinspection ConstantConditions
    if(valueSet == null) throw new IllegalArgumentException("valueSet cannot be null");
    id = new ValueSetValueId(variable, valueSet);
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

  @Override
  public Date getCreated() {
    return new Date(created.getTime());
  }

  @Override
  public Date getUpdated() {
    return new Date(updated.getTime());
  }

  public ValueSetState getValueSet() {
    return id.valueSet;
  }

  public VariableState getVariable() {
    return id.variable;
  }

  @Embeddable
  public final static class ValueSetValueId implements Serializable {

    private static final long serialVersionUID = 4020718518680731845L;

    @ManyToOne(optional = false)
    @JoinColumn(name = "value_set_id", referencedColumnName = "id")
    private ValueSetState valueSet;

    @ManyToOne(optional = false)
    @JoinColumn(name = "variable_id", referencedColumnName = "id")
    private VariableState variable;

    public ValueSetValueId() {
    }

    public ValueSetValueId(VariableState variable, ValueSetState valueSet) {
      this.valueSet = valueSet;
      this.variable = variable;
    }

    @Override
    public int hashCode() {
      return Objects.hashCode(valueSet, variable);
    }

    @Override
    public boolean equals(Object obj) {
      if(this == obj) return true;
      if(obj == null || getClass() != obj.getClass()) return false;
      ValueSetValueId other = (ValueSetValueId) obj;
      return Objects.equal(valueSet, other.valueSet) && Objects.equal(variable, other.variable);
    }

  }

}
