package org.obiba.magma.js;

import javax.annotation.Nullable;

import groovy.lang.Script;
import org.junit.After;
import org.junit.Before;
import org.obiba.magma.MagmaEngine;
import org.obiba.magma.Value;
import org.obiba.magma.Variable;
import org.obiba.magma.type.DecimalType;
import org.obiba.magma.type.IntegerType;

import static org.fest.assertions.api.Assertions.assertThat;

public abstract class AbstractJsTest {

  protected static final String PARTICIPANT = "Participant";

  @Before
  public void before() {
    MagmaEngine.get().shutdown();
    newEngine().extend(new MagmaJsExtension());
  }

  @After
  public void after() {
    shutdownEngine();
  }

  protected void shutdownEngine() {
    MagmaEngine.get().shutdown();
  }

  protected MagmaContext getMagmaContext() {
    return new MagmaContext();
  }

  protected MagmaEngine newEngine() {
    return new MagmaEngine();
  }

  protected ScriptableValue newValue(Value value, @Nullable String unit) {
    return new ScriptableValue(value, unit);
  }

  protected ScriptableValue newValue(Value value) {
    return newValue(value, null);
  }

  protected Object evaluate(final String script, final Variable variable) {
    ScriptableVariable scope = new ScriptableVariable(variable);
    Script compiledScript = MagmaContextFactory.getEngine().parse(script);
    MagmaContext context = MagmaContextFactory.createContext(scope);
    compiledScript.setBinding(context);
    return compiledScript.run();
  }

  protected ScriptableValue evaluate(String script, Value value) {
    return evaluate(script, value, null);
  }

  protected ScriptableValue evaluate(final String script, final Value value, @Nullable final String unit) {
    MagmaContext context = MagmaContextFactory.createContext(newValue(value, unit));
    Script compiledScript = MagmaContextFactory.getEngine().parse(script);
    compiledScript.setBinding(context);
    return (ScriptableValue) compiledScript.run();
  }

  protected void assertMethod(String script, Value value, Value expected) {
    ScriptableValue result = evaluate(script, value);
    assertThat(result).isNotNull();
    assertThat(result.getValue()).isNotNull();
    assertThat(result.getValue()).isEqualTo(expected);
  }

  protected static Variable createIntVariable(String name, String script) {
    return new Variable.Builder(name, IntegerType.get(), PARTICIPANT).addAttribute("script", script).build();
  }

  protected static Variable createDecimalVariable(String name, String script) {
    return new Variable.Builder(name, DecimalType.get(), PARTICIPANT).addAttribute("script", script).build();
  }
}
