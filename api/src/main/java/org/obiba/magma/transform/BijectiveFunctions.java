package org.obiba.magma.transform;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.io.Serializable;

import com.google.common.collect.BiMap;

public final class BijectiveFunctions {

  /**
   * Returns the identity function.
   */
  @SuppressWarnings("unchecked")
  public static <E> BijectiveFunction<E, E> identity() {
    return (BijectiveFunction<E, E>) IdentityFunction.INSTANCE;
  }

  // enum singleton pattern
  private enum IdentityFunction implements BijectiveFunction<Object, Object> {
    INSTANCE;

    public Object apply(Object o) {
      return o;
    }

    @Override
    public Object unapply(Object o) {
      return o;
    }

    @Override
    public String toString() {
      return "bijective-identity";
    }
  }

  /**
   * Returns a function which performs a map lookup. The returned function throws an {@link IllegalArgumentException} if
   * given a key that does not exist in the map.
   */
  public static <K, V> BijectiveFunction<K, V> forBiMap(BiMap<K, V> map) {
    return new BijectiveFunctionForMapNoDefault<K, V>(map);
  }

  private static class BijectiveFunctionForMapNoDefault<K, V> implements BijectiveFunction<K, V>, Serializable {

    final BiMap<K, V> map;

    BijectiveFunctionForMapNoDefault(BiMap<K, V> map) {
      this.map = checkNotNull(map);
    }

    public V apply(K key) {
      V result = map.get(key);
      checkArgument(result != null || map.containsKey(key), "Key '%s' not present in map", key);
      return result;
    }

    public K unapply(V key) {
      K result = map.inverse().get(key);
      checkArgument(result != null || map.inverse().containsKey(key), "Key '%s' not present in inverse map", key);
      return result;
    };

    @Override
    public boolean equals(Object o) {
      if(o instanceof BijectiveFunctionForMapNoDefault) {
        BijectiveFunctionForMapNoDefault<?, ?> that = (BijectiveFunctionForMapNoDefault<?, ?>) o;
        return map.equals(that.map);
      }
      return false;
    }

    @Override
    public int hashCode() {
      return map.hashCode();
    }

    @Override
    public String toString() {
      return "forBiMap(" + map + ")";
    }

    private static final long serialVersionUID = 0;
  }

}
