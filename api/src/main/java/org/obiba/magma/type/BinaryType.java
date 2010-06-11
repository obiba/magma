package org.obiba.magma.type;

import java.io.IOException;
import java.lang.ref.WeakReference;

import org.obiba.magma.MagmaEngine;
import org.obiba.magma.Value;

public class BinaryType extends AbstractValueType {

  private static final long serialVersionUID = -5271259966499174607L;

  private static WeakReference<BinaryType> instance;

  private BinaryType() {

  }

  public static BinaryType get() {
    if(instance == null || instance.get() == null) {
      instance = MagmaEngine.get().registerInstance(new BinaryType());
    }
    return instance.get();
  }

  @Override
  public String getName() {
    return "binary";
  }

  public Class<?> getJavaClass() {
    return byte[].class;
  }

  @Override
  public boolean acceptsJavaClass(Class<?> clazz) {
    return byte[].class.isAssignableFrom(clazz);
  }

  public boolean isDateTime() {
    return false;
  }

  public boolean isNumeric() {
    return false;
  }

  @Override
  protected String toString(Object object) {
    return Base64.encodeBytes((byte[]) object);
  }

  @Override
  public Value valueOf(String string) {
    if(string == null) {
      return nullValue();
    }
    try {
      return Factory.newValue(this, Base64.decode(string, Base64.DONT_GUNZIP));
    } catch(IOException e) {
      throw new IllegalArgumentException("Invalid Base64 encoding. Cannot construct binary Value instance.", e);
    }
  }

  @Override
  public Value valueOf(Object object) {
    // input type is expected to be byte[]
    if(object == null) {
      return nullValue();
    }
    if(byte[].class.equals(object.getClass())) {
      return Factory.newValue(this, (byte[]) object);
    }
    throw new IllegalArgumentException("Cannot construct " + getClass().getSimpleName() + " from type " + object.getClass() + ".");
  }

  @Override
  public int compare(Value o1, Value o2) {
    if(o1 == null) throw new NullPointerException();
    if(o2 == null) throw new NullPointerException();
    if(!o1.getValueType().equals(this)) throw new ClassCastException();
    if(!o2.getValueType().equals(this)) throw new ClassCastException();
    // All byte[] are considered equal when sorting.
    return 0;
  }
}
