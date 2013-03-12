package org.obiba.magma.support;

import org.obiba.magma.Decorator;

import com.google.common.base.Function;

public final class Decorators {

  /**
   * The no-op decorator.
   *
   * @param <T>
   * @return an implementation of {@code Decorator} that returns its argument untouched.
   */
  @SuppressWarnings("unchecked")
  public static <T> Decorator<T> identity() {
    return (Decorator<T>) IdentityDecorator.INSTANCE;
  }

  /**
   * Returns a {@code Function} implementation that calls {@code Decorator#decorate(Object)} passing it its argument.
   *
   * @param <T> the type of {@code Function}
   * @param decorator a {@code Decorator} implementation of type {@code <T>}
   * @return a {@code Function} that decorates its argument using {@code decorator}
   */
  public static <T> Function<T, T> decoratingFunction(final Decorator<T> decorator) {
    return new Function<T, T>() {

      @Override
      public T apply(T from) {
        return decorator.decorate(from);
      }

    };
  }

  private static final class IdentityDecorator implements Decorator<Object> {

    private static final IdentityDecorator INSTANCE = new IdentityDecorator();

    @Override
    public Object decorate(Object object) {
      return object;
    }

  }
}
