package org.obiba.magma.js;

import java.util.EmptyStackException;
import java.util.Stack;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

public class MagmaContext extends Context {

  MagmaContext(MagmaContextFactory factory) {
    super(factory);
  }

  public static MagmaContext getCurrentMagmaContext() {
    return asMagmaContext(getCurrentContext());
  }

  public static MagmaContext asMagmaContext(Context ctx) {
    try {
      return (MagmaContext) ctx;
    } catch(ClassCastException e) {
      throw new MagmaJsRuntimeException("No MagmaContext available. " +
          "Make sure MagmaJsExtension has been initialized before using JavascriptValueSource instances.", e);
    }
  }

  /**
   * Returns the instance of the {@code sharedScope} configured by {@code #initStandardObjects()}. A call to {@code
   * #initStandardObjects()} must be made prior to invoking this method, otherwise an {@code IllegalStateException} is
   * thrown.
   *
   * @return the {@code sharedScope}
   */
  public ScriptableObject sharedScope() {
    return getMagmaContextFactory().sharedScope();
  }

  /**
   * Creates a new {@code Scriptable} instance for use as a transient scope. The returned {@code Scriptable} has no
   * parent scope and has the {@code sharedScope} has prototype.
   * <p/>
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

  /**
   * Removes the object at the top of this stack and returns that object as the value of this function.
   *
   * @param type
   * @param <T>
   * @return The object at the top of this stack (the last item of the <tt>Vector</tt> object).
   */
  @SuppressWarnings("unchecked")
  public <T> T pop(Class<T> type) {
    Stack<T> stack = (Stack<T>) getThreadLocal(type);
    if(stack == null) {
      throw new IllegalStateException("Cannot pop stack for type " + type.getName());
    }

    try {
      return stack.pop();
    } catch(EmptyStackException e) {
      throw new IllegalStateException("Cannot pop stack for type " + type.getName());
    }
  }

  /**
   * Looks at the object at the top of this stack without removing it from the stack.
   *
   * @param type
   * @param <T>
   * @return the object at the top of this stack (the last item of the <tt>Vector</tt> object).
   */
  @SuppressWarnings("unchecked")
  public <T> T peek(Class<T> type) {
    Stack<T> stack = (Stack<T>) getThreadLocal(type);
    if(stack == null) {
      throw new IllegalStateException("Cannot peek stack for type " + type.getName());
    }

    try {
      return stack.peek();
    } catch(EmptyStackException e) {
      return null;
    }
  }

  public <T> boolean has(Class<T> type) {
    return getThreadLocal(type) != null && peek(type) != null;
  }

  protected MagmaContextFactory getMagmaContextFactory() {
    return (MagmaContextFactory) getFactory();
  }

}
