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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.Index;

@Entity
@Table(name = "value_set_binary_value")
public class ValueSetBinaryValue extends AbstractTimestampedEntity {

  private static final long serialVersionUID = -7767999255949547929L;

  @Lob
  private byte[] value;

  private int size;

  @ManyToOne(optional = false)
  @JoinColumn(name = "value_set_value_id", referencedColumnName = "id")
  private ValueSetValue valueSetValue;

  @Index(name = "occurrenceIndex")
  private int occurrence;

  @SuppressWarnings("UnusedDeclaration")
  public ValueSetBinaryValue() {
  }

  public ValueSetBinaryValue(ValueSetValue valueSetValue, int occurrence) {
    if(valueSetValue == null) throw new IllegalArgumentException("valueSetValue cannot be null");
    this.valueSetValue = valueSetValue;
    this.occurrence = occurrence;
  }

  public int getOccurrence() {
    return occurrence;
  }

  public byte[] getValue() {
    return value;
  }

  public ValueSetValue getValueSetValue() {
    return valueSetValue;
  }

  public void setValueSetValue(ValueSetValue valueSetValue) {
    this.valueSetValue = valueSetValue;
  }

  public void setValue(byte[] value) {
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
