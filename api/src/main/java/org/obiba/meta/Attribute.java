package org.obiba.meta;

import java.util.Locale;

import org.obiba.meta.type.TextType;

public interface Attribute {

  public static class Builder {

    private AttributeBean attribute;

    private Builder(String name) {
      attribute = new AttributeBean(name);
    }

    public static Builder newAttribute(String name) {
      return new Builder(name);
    }

    public Builder withValue(String value) {
      attribute.value = TextType.get().valueOf(value);
      return this;
    }

    public Builder withValue(Locale locale, String value) {
      attribute.locale = locale;
      attribute.value = MetaEngine.get().getValueFactory().newValue(TextType.get(), value);
      return this;
    }

    public Builder withValue(Value value) {
      attribute.value = value;
      return this;
    }

    public Attribute build() {
      return attribute;
    }

  }

  public String getName();

  public Locale getLocale();

  public boolean isLocalised();

  public ValueType getValueType();

  public Value getValue();

}
