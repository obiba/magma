/*
 * Copyright (c) 2016 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.magma.js;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;

import javax.validation.constraints.NotNull;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.ContextAction;
import org.mozilla.javascript.ContextFactory;
import org.mozilla.javascript.FunctionObject;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.obiba.magma.Initialisable;
import org.obiba.magma.js.methods.GlobalMethods;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;

/**
 * Creates instances of {@code MagmaContext}
 */
public class MagmaContextFactory extends ContextFactory implements Initialisable {

  /**
   * The global scope shared by all evaluated scripts. Should contain top-level functions and prototypes.
   */
  private ScriptableObject sharedScope;

  @NotNull
  private ScriptableValuePrototypeFactory scriptableValuePrototypeFactory = new ScriptableValuePrototypeFactory();

  private final ScriptableVariablePrototypeFactory scriptableVariablePrototypeFactory
      = new ScriptableVariablePrototypeFactory();

  @NotNull
  private Set<GlobalMethodProvider> globalMethodProviders = Collections.emptySet();

  @Override
  protected Context makeContext() {
    return new MagmaContext(this);
  }

  public ScriptableObject sharedScope() {
    if(sharedScope == null) {
      throw new MagmaJsRuntimeException(
          "Shared scope not initialised. Make sure the MagmaJsExtension has been added to the MagmaEngine before evaluating scripts.");
    }
    return sharedScope;
  }

  public void setGlobalMethodProviders(@NotNull Collection<GlobalMethodProvider> globalMethodProviders) {
    //noinspection ConstantConditions
    if(globalMethodProviders == null) throw new IllegalArgumentException("globalMethodProviders cannot be null");
    this.globalMethodProviders = ImmutableSet.copyOf(globalMethodProviders);
  }

  @NotNull
  public ScriptableValuePrototypeFactory getScriptableValuePrototypeFactory() {
    return scriptableValuePrototypeFactory;
  }

  public void setScriptableValuePrototypeFactory(@NotNull ScriptableValuePrototypeFactory factory) {
    //noinspection ConstantConditions
    if(factory == null) throw new IllegalArgumentException("factory cannot be null");
    scriptableValuePrototypeFactory = factory;
  }

  @Override
  public void initialise() {
    sharedScope = (ScriptableObject) ContextFactory.getGlobal().call(new ContextAction() {
      @Override
      public Object run(Context cx) {
        ScriptableObject scriptableObject = cx.initStandardObjects(null, true);

        // Register Global methods
        for(GlobalMethodProvider provider : Iterables
            .concat(ImmutableSet.of(new GlobalMethods()), globalMethodProviders)) {
          for(Method globalMethod : provider.getJavaScriptExtensionMethods()) {
            String name = provider.getJavaScriptMethodName(globalMethod);
            FunctionObject fo = new FunctionObject(name, globalMethod, scriptableObject);
            scriptableObject.defineProperty(name, fo, ScriptableObject.DONTENUM);
          }
        }

        Scriptable valuePrototype = scriptableValuePrototypeFactory.buildPrototype();
        ScriptableObject.putProperty(scriptableObject, valuePrototype.getClassName(), valuePrototype);

        Scriptable variablePrototype = scriptableVariablePrototypeFactory.buildPrototype();
        ScriptableObject.putProperty(scriptableObject, variablePrototype.getClassName(), variablePrototype);

        scriptableObject.sealObject();
        return scriptableObject;
      }
    });
  }
}
