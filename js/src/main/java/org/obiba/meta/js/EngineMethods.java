package org.obiba.meta.js;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Date;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.FunctionObject;
import org.mozilla.javascript.ScriptRuntime;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.obiba.meta.MetaEngine;
import org.obiba.meta.Value;
import org.obiba.meta.ValueSetReference;
import org.obiba.meta.Variable;
import org.obiba.meta.VariableValueSource;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

public final class EngineMethods {

  public static void registerMethods(ScriptableObject so) {
    Method method = Iterables.find(Arrays.asList(EngineMethods.class.getMethods()), new Predicate<Method>() {

      @Override
      public boolean apply(Method input) {
        return "valueOf".equals(input.getName());
      }
    });
    FunctionObject fo = new FunctionObject("$", method, so);
    so.defineProperty("$", fo, ScriptableObject.DONTENUM);
  }

  /**
   * Accessed as $ in js.
   * 
   * <pre>
   *   var value = $('Participant.firstName');
   *   var value2 = $('other-collection:SMOKER_STATUS');
   * </pre>
   * @param name
   * @return
   */
  public static Scriptable valueOf(Context ctx, Scriptable thisObj, Object[] args, Function funObj) {
    String name = (String) args[0];
    ValueSetReference reference = (ValueSetReference) ctx.getThreadLocal(ValueSetReference.class);
    Variable variable = (Variable) ctx.getThreadLocal(Variable.class);
    if(name.indexOf(':') < 0) {
      name = variable.getCollection() + ':' + name;
    }

    VariableValueSource source = lookupSource(reference, name);
    Value value = source.getValue(reference);
    if(source.getValueType().isDateTime()) {
      Date date = (Date) value.getValue();
      return Context.toObject(ScriptRuntime.wrapNumber(date.getTime()), thisObj);
    }
    return ScriptRuntime.toObject(thisObj, value.getValue());
  }

  private static VariableValueSource lookupSource(ValueSetReference reference, String name) {
    return MetaEngine.get().lookupVariable(reference.getVariableEntity().getType(), name);
  }

}
