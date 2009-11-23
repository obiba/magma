package org.obiba.magma.filter;

import org.obiba.magma.ValueSet;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("script")
public class JavaScriptFilter extends AbstractFilter<ValueSet> {

  private String javascript;

  JavaScriptFilter(String javascript) {
    validateArguments(javascript);
    this.javascript = javascript;
  }

  private void validateArguments(String javascript) {
    if(javascript == null) throw new IllegalArgumentException("The argument [javascript] cannot be null.");
  }

  @Override
  protected boolean runFilter(ValueSet item) {
    // TODO Auto-generated method stub
    // must make js available to filter!
    // JavascriptValueSource j = new JavascriptValueSource();
    return false;
  }

  private Object readResolve() {
    validateType();
    return this;
  }

  public static class Builder extends AbstractFilter.Builder {

    private String javascript;

    public static Builder newFilter() {
      return new Builder();
    }

    public Builder javascript(String javascript) {
      this.javascript = javascript;
      return this;
    }

    public JavaScriptFilter build() {
      JavaScriptFilter filter = new JavaScriptFilter(javascript);
      filter.setType(type);
      filter.validateType();
      return filter;
    }

    @Override
    public Builder exclude() {
      super.exclude();
      return this;
    }

    @Override
    public Builder include() {
      super.include();
      return this;
    }
  }
}
