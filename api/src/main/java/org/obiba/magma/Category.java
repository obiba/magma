package org.obiba.magma;

public interface Category extends AttributeAware {

  public static class Builder {

    private CategoryBean category;

    private Builder(String name) {
      category = new CategoryBean(name, null);
    }

    public static Builder newCategory(String name) {
      return new Builder(name);
    }

    public Builder withCode(String code) {
      category.code = code;
      return this;
    }

    public Builder missing(boolean missing) {
      category.missing = missing;
      return this;
    }

    public Builder addAttribute(Attribute attribute) {
      category.attributes.put(attribute.getName(), attribute);
      return this;
    }

    public Category build() {
      return category;
    }

  }

  public String getName();

  public String getCode();

  public boolean isMissing();

}
