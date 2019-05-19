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

import java.util.Locale;

import javax.validation.constraints.NotNull;

import org.obiba.magma.type.TextType;

import com.google.common.base.Preconditions;

public interface Attribute {

  class Builder {

    private final AttributeBean attribute;

    private Builder(String name) {
      Preconditions.checkArgument(name != null, "name cannot be null");
      attribute = new AttributeBean(name);
    }

    private Builder() {
      attribute = new AttributeBean();
    }

    public static Builder newAttribute(String name) {
      return new Builder(name);
    }

    public static Builder newAttribute() {
      return new Builder();
    }

    public Builder withNamespace(String namespace) {
      attribute.namespace = namespace;
      return this;
    }

    public Builder withName(String name) {
      Preconditions.checkArgument(name != null, "name cannot be null");
      attribute.name = name;
      return this;
    }

    public Builder withValue(String value) {
      attribute.value = TextType.get().valueOf(value);
      return this;
    }

    public Builder withValue(Locale locale, String value) {
      attribute.locale = locale;
      attribute.value = TextType.get().valueOf(value);
      return this;
    }

    public Builder withValue(Value value) {
      attribute.value = value;
      return this;
    }

    public Builder withLocale(Locale locale) {
      attribute.locale = locale;
      return this;
    }

    public Builder withLocale(String locale) {
      attribute.locale = new Locale(locale);
      return this;
    }

    public Attribute build() {
      if(attribute.value == null) {
        attribute.value = TextType.get().nullValue();
      }
      return attribute;
    }

  }

  @NotNull
  String getNamespace();

  boolean hasNamespace();

  String getName();

  @NotNull
  Locale getLocale();

  boolean isLocalised();

  ValueType getValueType();

  Value getValue();

}
