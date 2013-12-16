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

import java.io.Serializable;
import java.util.Locale;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.Columns;
import org.hibernate.annotations.Parent;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.obiba.magma.Attribute;
import org.obiba.magma.Value;
import org.obiba.magma.ValueType;
import org.obiba.magma.datasource.hibernate.type.ValueHibernateType;

@Embeddable
@TypeDef(name = "value", typeClass = ValueHibernateType.class)
@SuppressWarnings("UnusedDeclaration")
public class AttributeState implements Attribute, Serializable {

  private static final long serialVersionUID = 1L;

  @Parent
  private AbstractAttributeAwareEntity parent;

  @Column(nullable = false)
  private String name;

  private String namespace;

  private Locale locale;

  @Type(type = "value")
  @Columns(columns = { @Column(name = "value_type", nullable = false), //
      @Column(name = "is_sequence", nullable = false), //
      @Column(name = "value", length = Integer.MAX_VALUE, nullable = false) })
  private Value value;

  public AttributeState() {
  }

  public AttributeState(String name, String namespace, Locale locale, Value value) {
    this.name = name;
    this.namespace = namespace;
    this.locale = locale;
    setValue(value);
  }

  public AbstractAttributeAwareEntity getParent() {
    return parent;
  }

  public void setParent(AbstractAttributeAwareEntity parent) {
    this.parent = parent;
  }

  public void setValue(Value value) {
    this.value = value;
  }

  @NotNull
  @Override
  public Locale getLocale() {
    if(locale == null) {
      throw new NullPointerException("Locale is null");
    }
    return locale;
  }

  @Override
  public String getName() {
    return name;
  }

  @NotNull
  @Override
  public String getNamespace() {
    if(namespace == null) {
      throw new NullPointerException("Namespace is null");
    }
    return namespace;
  }

  @Override
  public boolean hasNamespace() {
    return namespace != null;
  }

  @Override
  public Value getValue() {
    return value;
  }

  @Override
  public ValueType getValueType() {
    return value.getValueType();
  }

  @Override
  public boolean isLocalised() {
    return locale != null;
  }

}
