package org.obiba.magma.js.views;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import groovy.lang.Script;
import org.obiba.magma.Initialisable;
import org.obiba.magma.Value;
import org.obiba.magma.ValueSet;
import org.obiba.magma.ValueTable;
import org.obiba.magma.ValueType;
import org.obiba.magma.Variable;
import org.obiba.magma.VariableEntity;
import org.obiba.magma.js.MagmaContext;
import org.obiba.magma.js.MagmaContextFactory;
import org.obiba.magma.js.ScriptableValue;
import org.obiba.magma.js.ScriptableVariable;
import org.obiba.magma.type.BooleanType;
import org.obiba.magma.views.SelectClause;
import org.obiba.magma.views.View;
import org.obiba.magma.views.WhereClause;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JavascriptClause implements Initialisable, SelectClause, WhereClause {

  private static final Logger log = LoggerFactory.getLogger(JavascriptClause.class);

  //
  // Instance Variables
  //
  static Pattern pattern = Pattern.compile("map\\((\\{([^\\}]*)\\})", Pattern.DOTALL);

  private String scriptName = "customScript";

  private String script;

  // need to be transient because of XML serialization
  @SuppressWarnings("TransientFieldInNonSerializableClass")
  private transient Script compiledScript;

  //
  // Constructors
  //

  /**
   * No-arg constructor for XStream.
   */
  public JavascriptClause() {

  }

  public JavascriptClause(String script) {
    this.script = script;
  }

  //
  // Initialisable Methods
  //

  @Override
  public void initialise() {
    if(script == null) {
      throw new NullPointerException("script cannot be null");
    }
    String s = getScript().replaceAll("(^|\\s)var\\s", "");
    Matcher matcher = pattern.matcher(s);
    if(matcher.find()) matcher.replaceAll("map([$2]");

    compiledScript = MagmaContextFactory.getEngine().parse(s);
  }

  //
  // SelectClause Methods
  //

  @Override
  public boolean select(final Variable variable) {
    if(compiledScript == null) {
      throw new IllegalStateException("script hasn't been compiled. Call initialise() before calling select().");
    }

    if(variable == null) throw new IllegalArgumentException("variable cannot be null");

    MagmaContext selectContext = MagmaContextFactory.createContext(new ScriptableVariable(variable));
    Object value = selectContext.exec(() -> {
      compiledScript.setBinding(selectContext);
      return compiledScript.run();
    });

    if(value instanceof Boolean) {
      return (boolean) value;
    }

    if(value instanceof ScriptableValue) {
      ScriptableValue scriptable = (ScriptableValue) value;

      if(scriptable.getValueType().equals(BooleanType.get())) {
        return scriptable.getValue().isNull() ? null : (boolean) scriptable.getValue().getValue();
      }
    }

    return false;
  }

  //
  // WhereClause Methods
  //

  @Override
  public boolean where(final ValueSet valueSet) {
    return where(valueSet, null);
  }

  @Override
  public boolean where(final ValueSet valueSet, final View view) {
    if(compiledScript == null)
      throw new IllegalStateException("script hasn't been compiled. Call initialise() before calling where().");

    if(valueSet == null) throw new IllegalArgumentException("valueSet cannot be null");

    final ValueTable valueTable = valueSet.getValueTable();
    final VariableEntity variableEntity = valueSet.getVariableEntity();

    Map<Object, Object> shared = new HashMap<Object, Object>() {
      {
        put(ValueSet.class, valueSet);
        put(VariableEntity.class, variableEntity);
        put(ValueTable.class, valueTable);
        if(view != null) put(View.class, view);
      }
    };

    MagmaContext magmaContext = MagmaContextFactory.createContext();
    Object value = magmaContext.exec(() -> {
      compiledScript.setBinding(magmaContext);
      return compiledScript.run();
    }, shared);

    if(value instanceof Boolean) return (boolean) value;

    if(value instanceof ScriptableValue) {
      ScriptableValue scriptable = (ScriptableValue) value;

      if(scriptable.getValue().isNull()) return false;

      try {
        return (boolean) BooleanType.get().valueOf(scriptable.getValue().getValue()).getValue();
      } catch(Exception e) {
        return false;
      }
    }

    return false;
  }

  //
  // Query Methods
  //

  @SuppressWarnings("UnusedDeclaration")
  public Value query(final Variable variable) {
    if(compiledScript == null) {
      throw new IllegalStateException("script hasn't been compiled. Call initialise() before calling query().");
    }

    if(variable == null) throw new IllegalArgumentException("variable cannot be null");

    MagmaContext magmaContext = MagmaContextFactory.createContext();
    Object value = magmaContext.exec(()-> {
      try {
        compiledScript.setBinding(magmaContext);
        return compiledScript.run();
      } catch (Exception e) {
        return null;
      }
    });

    if(value instanceof ScriptableValue) {
      ScriptableValue scriptable = (ScriptableValue) value;
      return scriptable.getValue();
    } else if(value != null) {
      return ValueType.Factory.newValue((Serializable) value);
    } else {
      // TODO: Determine what to return in case of null. Currently returning false (BooleanType).
      return BooleanType.get().falseValue();
    }
  }

  //
  // Methods
  //

  public String getScriptName() {
    return scriptName;
  }

  public void setScriptName(String name) {
    scriptName = name;
  }

  public String getScript() {
    return script;
  }

  public void setScript(String script) {
    this.script = script;
  }

}
