package org.obiba.meta;

public interface Category {

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

    public Category build() {
      return category;
    }

  }

  public String getName();

  public String getCode();

}
