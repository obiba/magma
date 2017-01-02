/*
 * Copyright (c) 2017 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.magma.js;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.mozilla.javascript.FunctionObject;
import org.mozilla.javascript.NativeObject;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;

public abstract class AbstractPrototypeFactory {

//  private static final Logger log = LoggerFactory.getLogger(AbstractPrototypeFactory.class);

  private static final Set<String> EXCLUDED_METHODS = ImmutableSet
      .of("wait", "toString", "getClass", "equals", "hashCode", "notify", "notifyAll", "$$YJP$$wait");

  private final Collection<Class<?>> methodProviders = new HashSet<>();

  public void addMethodProvider(Class<?> methodProvider) {
    methodProviders.add(methodProvider);
  }

  public Scriptable buildPrototype() {
    Scriptable ctor = newPrototype();
    ScriptableObject prototype = new NativeObject();
    createMethods(prototype);
    ScriptableObject.putConstProperty(ctor, "prototype", prototype);
    return ctor;
  }

  protected void createMethods(ScriptableObject scriptableObject) {
    Iterable<Method> methods = Iterables
        .concat(Iterables.transform(methodProviders, new Function<Class<?>, Iterable<Method>>() {
          @Override
          public Iterable<Method> apply(Class<?> from) {
            return Iterables.filter(Arrays.asList(from.getMethods()), new Predicate<Method>() {
              @Override
              public boolean apply(Method input) {
                return !EXCLUDED_METHODS.contains(input.getName());
              }
            });
          }
        }));

    for(Method method : methods) {
//      log.trace("Define JS method {}", method.getName());
      scriptableObject.defineProperty(method.getName(), new FunctionObject(method.getName(), method, scriptableObject),
          ScriptableObject.DONTENUM);
    }
  }

  protected abstract Scriptable newPrototype();
}
