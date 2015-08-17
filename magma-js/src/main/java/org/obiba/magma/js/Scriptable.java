package org.obiba.magma.js;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

import com.google.common.base.Throwables;
import jdk.nashorn.api.scripting.AbstractJSObject;
import jdk.nashorn.api.scripting.JSObject;
import jdk.nashorn.api.scripting.ScriptObjectMirror;

public abstract class Scriptable extends AbstractJSObject {
  protected static void addMethodProvider(Map<String, JSObject> members, Class<?> clazz) {
    for(Method m : clazz.getMethods()) {
      members.put(m.getName(), new AbstractJSObject() {
        @Override
        public Object call(Object thiz, Object... args) {
          try {
            if(thiz == null || ScriptObjectMirror.isUndefined(thiz)) {
              thiz = MagmaContextFactory.getScriptableContext();
            }

            return m.invoke(null, thiz, args);
          } catch(IllegalAccessException | InvocationTargetException e) {
            Throwables.propagateIfInstanceOf(e.getCause(), MagmaJsEvaluationRuntimeException.class);
            throw Throwables.propagate(e);
          }
        }

        @Override
        public boolean isFunction() {
          return true;
        }
      });
    }
  }
}
