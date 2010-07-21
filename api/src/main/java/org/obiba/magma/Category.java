package org.obiba.magma;

import com.google.common.collect.ListMultimap;

public interface Category extends AttributeAware {

  public static class Builder extends AttributeAwareBuilder<Builder> {

    private CategoryBean category;

    private Builder(String name) {
      category = new CategoryBean(name, null);
    }

    @Override
    protected ListMultimap<String, Attribute> getAttributes() {
      return category.attributes;
    }

    @Override
    protected Builder getBuilder() {
      return this;
    }

    public static Builder newCategory(String name) {
      return new Builder(name);
    }

    public static Builder sameAs(Category c) {
      Builder builder = newCategory(c.getName());
      builder.category.code = c.getCode();
      if(c.isMissing()) builder.category.missing = true;
      for(Attribute a : c.getAttributes()) {
        builder.category.attributes.put(a.getName(), a);
      }
      return builder;
    }

    public Builder clearAttributes() {
      getAttributes().clear();
      return this;
    }

    public Builder withCode(String code) {
      category.code = code;
      return this;
    }

    public Builder name(String name) {
      category.name = name;
      return this;
    }

    public Builder missing(boolean missing) {
      category.missing = missing;
      return this;
    }

    public Category build() {
      return category;
    }

    /**
     * Accepts a {@code BuilderVisitor} to allow it to visit this {@code Builder} instance.
     * @param visitor the visitor to accept; cannot be null.
     * @return this
     */
    public Builder accept(BuilderVisitor visitor) {
      visitor.visit(this);
      return this;
    }

    /**
     * Accepts a collection of visitors and calls {@code #accept(BuilderVisitor)} on each instance.
     * @param visitors the collection of visitors to accept
     * @return this
     */
    public Builder accept(Iterable<? extends BuilderVisitor> visitors) {
      for(BuilderVisitor visitor : visitors) {
        accept(visitor);
      }
      return this;
    }

  }

  /**
   * Visitor pattern for contributing to a {@code Builder} instance through composition.
   */
  public interface BuilderVisitor {

    /**
     * Visit a builder instance and contribute to the category being built.
     * @param builder the instance to contribute to.
     */
    public void visit(Builder builder);

  }

  public String getName();

  public String getCode();

  public boolean isMissing();

}
