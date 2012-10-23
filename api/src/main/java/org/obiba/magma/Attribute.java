package org.obiba.magma;

import java.util.Locale;

import javax.annotation.Nullable;

import org.obiba.magma.type.TextType;

import com.google.common.base.Preconditions;

public interface Attribute {

  public static class Builder {

    private AttributeBean attribute;

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

    public Attribute build() {
      if(attribute.value == null) {
        attribute.value = TextType.get().nullValue();
      }
      return attribute;
    }

  }

  @Nullable
  public String getNamespace();

  public boolean hasNamespace();

  public String getName();

  @Nullable
  public Locale getLocale();

  public boolean isLocalised();

  public ValueType getValueType();

  public Value getValue();

}
