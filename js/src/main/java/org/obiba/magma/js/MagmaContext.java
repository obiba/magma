package org.obiba.magma.js;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.EmptyStackException;
import java.util.Stack;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.FunctionObject;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.obiba.magma.js.methods.GlobalMethods;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

public class MagmaContext extends Context {

  private static ScriptableObject sharedScope;

  private ScriptableValuePrototypeFactory factory = new ScriptableValuePrototypeFactory();

  MagmaContext(MagmaContextFactory factory) {
    super(factory);
  }

  public ScriptableValuePrototypeFactory getScriptableValuePrototypeFactory() {
    return factory;
  }

  public void setScriptableValuePrototypeFactory(ScriptableValuePrototypeFactory factory) {
    if(factory == null) throw new IllegalArgumentException("factory cannot be null");
    this.factory = factory;
  }

  /**
   * Returns the instance of the {@code sharedScope} configured by {@code #initStandardObjects()}. A call to {@code
   * #initStandardObjects()} must be made prior to invoking this method, otherwise an {@code IllegalStateException} is
   * thrown.
   * @return the {@code sharedScope}
   */
  public ScriptableObject sharedScope() {
    if(sharedScope == null) {
      throw new IllegalStateException();
    }
    return sharedScope;
  }

  /**
   * Creates a new {@code Scriptable} instance for use as a transient scope. The returned {@code Scriptable} has no
   * parent scope and has the {@code sharedScope} has prototype.
   * <p>
   * The purpose of this method is to obtain a scope instance that extends the global scope and into which new objects
   * and properties can be defined without polluting the global scope.
   * 
   * @return a new instance of {@code Scriptable} for use as a top-level scope.
   */
  public Scriptable newLocalScope() {
    // Create a new object within the sharedScope
    Scriptable scope = newObject(sharedScope());
    // Set its prototype
    scope.setPrototype(sharedScope());
    // Remove its parent scope (makes it a top-level scope)
    scope.setParentScope(null);
    return scope;
  }

  @SuppressWarnings("unchecked")
  public <T> void push(Class<T> type, T value) {
    Stack<T> stack = (Stack<T>) getThreadLocal(type);
    if(stack == null) {
      stack = new Stack<T>();
      putThreadLocal(type, stack);
    }
    stack.push(value);
  }

  @SuppressWarnings("unchecked")
  public <T> T pop(Class<T> type) {
    Stack<T> stack = (Stack<T>) getThreadLocal(type);
    if(stack == null) {
      throw new IllegalStateException("Cannot pop stack for type " + type.getName());
    }

    try {
      return stack.pop();
    } catch(EmptyStackException e) {
      throw new IllegalStateException("Cannot pop ValueSet");
    }
  }

  @SuppressWarnings("unchecked")
  public <T> T peek(Class<T> type) {
    Stack<T> stack = (Stack<T>) getThreadLocal(type);
    if(stack == null) {
      throw new IllegalStateException("Cannot pop stack for type " + type.getName());
    }

    try {
      return stack.peek();
    } catch(EmptyStackException e) {
      throw new IllegalStateException("Cannot pop ValueSet");
    }
  }

  /**
   * Overridden to create standard object prototypes and methods.
   */
  @Override
  public ScriptableObject initStandardObjects(ScriptableObject scope, boolean sealed) {
    sharedScope = super.initStandardObjects(scope, sealed);

    Iterable<Method> methods = Iterables.filter(Arrays.asList(GlobalMethods.class.getMethods()), new Predicate<Method>() {
      @Override
      public boolean apply(Method input) {
        return GlobalMethods.GLOBAL_METHODS.contains(input.getName());
      }
    });
    for(Method globalMethod : methods) {
      String name = globalMethod.getName().equals("valueOf") ? "$" : globalMethod.getName();
      FunctionObject fo = new FunctionObject(name, globalMethod, sharedScope);
      sharedScope.defineProperty(name, fo, ScriptableObject.DONTENUM);
    }

    Scriptable valuePrototype = factory.buildPrototype();
    ScriptableObject.putProperty(sharedScope, valuePrototype.getClassName(), valuePrototype);
    sharedScope.sealObject();
    return sharedScope;
  }
}
