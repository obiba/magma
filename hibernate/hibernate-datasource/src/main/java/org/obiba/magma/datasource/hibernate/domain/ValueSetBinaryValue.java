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

import javax.annotation.Nonnull;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.hibernate.annotations.Index;
import org.obiba.core.domain.AbstractEntity;

@Entity
@Table(name = "value_set_binary_value",
    uniqueConstraints = @UniqueConstraint(columnNames = { "value_set_id", "variable_id", "occurrence" }))
@NamedQuery(name = "findBinaryByValueSetValueAndOccurrence", query = //
    "SELECT vb FROM ValueSetBinaryValue vb " + //
        "WHERE vb.valueSet.id = :valueSetId AND vb.variable.id = :variableId AND occurrence = :occurrence")
@SuppressWarnings("UnusedDeclaration")
public class ValueSetBinaryValue extends AbstractEntity {

  private static final long serialVersionUID = -7767999255949547929L;

  @Lob
  private byte[] value;

  private int size;

  @ManyToOne(optional = false)
  @JoinColumn(name = "value_set_id", referencedColumnName = "id")
  private ValueSetState valueSet;

  @ManyToOne(optional = false)
  @JoinColumn(name = "variable_id", referencedColumnName = "id")
  private VariableState variable;

  @Index(name = "occurrenceIndex")
  private int occurrence;

  @SuppressWarnings("UnusedDeclaration")
  public ValueSetBinaryValue() {
  }

  public ValueSetBinaryValue(@Nonnull ValueSetValue valueSetValue, int occurrence) {
    if(valueSetValue == null) throw new IllegalArgumentException("valueSetValue cannot be null");
    valueSet = valueSetValue.getValueSet();
    variable = valueSetValue.getVariable();
    this.occurrence = occurrence;
  }

  public int getOccurrence() {
    return occurrence;
  }

  public byte[] getValue() {
    return value;
  }

  public ValueSetState getValueSet() {
    return valueSet;
  }

  public VariableState getVariable() {
    return variable;
  }

  @SuppressWarnings("MethodCanBeVariableArityMethod")
  public void setValue(@Nonnull byte[] value) {
    if(value == null) {
      throw new IllegalArgumentException("cannot persist null values");
    }
    this.value = value;
    size = value.length;
  }

  public int getSize() {
    return size;
  }

}
