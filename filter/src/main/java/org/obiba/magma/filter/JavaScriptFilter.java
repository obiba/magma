package org.obiba.magma.filter;

import org.obiba.magma.Value;
import org.obiba.magma.ValueSet;
import org.obiba.magma.js.JavascriptValueSource;
import org.obiba.magma.type.BooleanType;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("script")
public class JavaScriptFilter extends AbstractFilter<ValueSet> {

  private static final String SCRIPT_NAME = "JAVASCRIPT_FILTER_SCRIPT";

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
    JavascriptValueSource javascriptSource = new JavascriptValueSource();
    javascriptSource.setScript(javascript);
    javascriptSource.setScriptName(SCRIPT_NAME);
    javascriptSource.setValueType(BooleanType.get());
    javascriptSource.initialise();
    Value value = javascriptSource.getValue(item);
    Boolean booleanValue = (Boolean) value.getValue();
    return booleanValue.booleanValue();
  }

  private Object readResolve() {
    validateArguments(javascript);
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
