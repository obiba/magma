/*
 * Copyright (c) 2016 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.magma.js.views;

import java.io.Serializable;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.ContextAction;
import org.mozilla.javascript.ContextFactory;
import org.mozilla.javascript.EvaluatorException;
import org.mozilla.javascript.Script;
import org.mozilla.javascript.Scriptable;
import org.obiba.magma.Initialisable;
import org.obiba.magma.Value;
import org.obiba.magma.ValueSet;
import org.obiba.magma.ValueTable;
import org.obiba.magma.ValueType;
import org.obiba.magma.Variable;
import org.obiba.magma.VariableEntity;
import org.obiba.magma.js.MagmaContext;
import org.obiba.magma.js.ScriptableValue;
import org.obiba.magma.js.ScriptableVariable;
import org.obiba.magma.type.BooleanType;
import org.obiba.magma.views.SelectClause;
import org.obiba.magma.views.View;
import org.obiba.magma.views.WhereClause;

public class JavascriptClause implements Initialisable, SelectClause, WhereClause {
  //
  // Instance Variables
  //

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
  public void initialise() throws EvaluatorException {
    if(script == null) {
      throw new NullPointerException("script cannot be null");
    }

    compiledScript = (Script) ContextFactory.getGlobal().call(new ContextAction() {
      @Override
      public Object run(Context cx) {
        return cx.compileString(getScript(), getScriptName(), 1, null);
      }
    });
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

    return (Boolean) ContextFactory.getGlobal().call(new ContextAction() {
      @Override
      @SuppressWarnings("ChainOfInstanceofChecks")
      public Object run(Context ctx) {
        MagmaContext context = MagmaContext.asMagmaContext(ctx);
        // Don't pollute the global scope
        Scriptable scope = new ScriptableVariable(context.newLocalScope(), variable);

        Object value = compiledScript.exec(ctx, scope);
        if(value instanceof Boolean) {
          return value;
        }
        if(value instanceof ScriptableValue) {
          ScriptableValue scriptable = (ScriptableValue) value;
          if(scriptable.getValueType().equals(BooleanType.get())) {
            return scriptable.getValue().isNull() ? null : scriptable.getValue().getValue();
          }
        }
        return false;
      }
    });
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
    if(compiledScript == null) {
      throw new IllegalStateException("script hasn't been compiled. Call initialise() before calling where().");
    }
    if(valueSet == null) throw new IllegalArgumentException("valueSet cannot be null");

    return (Boolean) ContextFactory.getGlobal().call(new WhereContextAction(valueSet, view));
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

    return (Value) ContextFactory.getGlobal().call(new ContextAction() {
      @SuppressWarnings("IfMayBeConditional")
      @Override
      public Object run(Context ctx) {
        MagmaContext context = MagmaContext.asMagmaContext(ctx);
        // Don't pollute the global scope
        Scriptable scope = new ScriptableVariable(context.newLocalScope(), variable);

        Object value = compiledScript.exec(ctx, scope);

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
    });
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

  /**
   * This method is invoked before evaluating the script. It provides a chance for derived classes to initialise values
   * within the context. This method will add the current {@code ValueSet} as a {@code ThreadLocal} variable with
   * {@code ValueSet#class} as its key. This allows other classes to have access to the current {@code ValueSet} during
   * the script's execution.
   * <p/>
   * Classes overriding this method must call their super class' method
   *
   * @param ctx the current context
   * @param scope the scope of execution of this script
   * @param valueSet the current {@code ValueSet}
   */
  protected void enterContext(MagmaContext ctx, @SuppressWarnings("UnusedParameters") Scriptable scope,
      ValueSet valueSet, View view) {
    ctx.push(ValueSet.class, valueSet);
    ctx.push(VariableEntity.class, valueSet.getVariableEntity());
    ValueTable valueTable = valueSet.getValueTable();
    ctx.push(ValueTable.class, valueTable);
    if(view != null) {
      ctx.push(View.class, view);
    }
  }

  protected void exitContext(MagmaContext ctx, ValueSet valueSet, View view) {
    ctx.pop(ValueSet.class);
    ctx.pop(VariableEntity.class);
    ctx.pop(ValueTable.class);
    if(view != null) {
      ctx.pop(View.class);
    }
  }

  private class WhereContextAction implements ContextAction {
    private final ValueSet valueSet;

    private final View view;

    WhereContextAction(ValueSet valueSet, View view) {
      this.valueSet = valueSet;
      this.view = view;
    }

    @Override
    @SuppressWarnings("ChainOfInstanceofChecks")
    public Object run(Context ctx) {
      MagmaContext context = MagmaContext.asMagmaContext(ctx);
      // Don't pollute the global scope
      Scriptable scope = context.newLocalScope();

      enterContext(context, scope, valueSet, view);
      Object value = compiledScript.exec(ctx, scope);
      exitContext(context, valueSet, view);

      if(value instanceof Boolean) {
        return value;
      }
      if(value instanceof ScriptableValue) {
        return getValue((ScriptableValue) value);
      }
      return false;
    }

    private Object getValue(ScriptableValue scriptable) {
      if (scriptable.getValue().isNull()) return false;
      try {
        return BooleanType.get().valueOf(scriptable.getValue().getValue()).getValue();
      } catch (Exception e) {
        return false;
      }
    }
  }
}
