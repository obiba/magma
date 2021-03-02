/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.magma;

import java.io.Serializable;
import java.util.Locale;

import javax.validation.constraints.NotNull;

import com.google.common.base.Objects;

class AttributeBean implements Attribute, Serializable {

  private static final long serialVersionUID = -7732601103831162009L;

  String name;

  String namespace;

  Locale locale;

  Value value;

  AttributeBean(String name) {
    this.name = name;
  }

  AttributeBean() {
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
    return namespace != null && !namespace.isEmpty();
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

  @Override
  public boolean isLocalised() {
    return locale != null && !locale.toString().isEmpty();
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
  public int hashCode() {
    return Objects.hashCode(name, namespace, locale, value);
  }

  @Override
  public boolean equals(Object obj) {
    if(this == obj) return true;
    if(obj == null) return false;
    if(getClass() != obj.getClass()) return false;
    AttributeBean other = (AttributeBean) obj;
    return Objects.equal(name, other.name) && Objects.equal(namespace, other.namespace) &&
        Objects.equal(locale, other.locale) && Objects.equal(value, other.value);
  }

  @Override
  public String toString() {
    return "AttributeBean{" +
        "name='" + name + '\'' +
        ", namespace='" + namespace + '\'' +
        ", locale=" + locale +
        ", value=" + value +
        '}';
  }
}
