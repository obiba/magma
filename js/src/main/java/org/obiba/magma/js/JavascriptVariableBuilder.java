package org.obiba.magma.js;

import org.obiba.magma.Variable.Builder;

/**
 * A {@code Variable.Builder} extension for building javascript variables. To obtain an instance of this builder, use
 * the {@link Builder#extend(Class)} method by passing this type:
 * 
 * <pre>
 * Variable.Builder builder = ...;
 * 
 * builder.extend(JavascriptVariableBuilder.class).setScript(&quot;'Hello World!'&quot;);
 * ...
 * </pre>
 * 
 */
public class JavascriptVariableBuilder extends Builder {

  static final String SCRIPT_ATTRIBUTE_NAME = "script";

  public JavascriptVariableBuilder(Builder builder) {
    super(builder);
  }

  public JavascriptVariableBuilder setScript(String script) {
    addAttribute(SCRIPT_ATTRIBUTE_NAME, script);
    return this;
  }

}
