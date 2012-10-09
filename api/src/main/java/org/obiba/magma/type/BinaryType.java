package org.obiba.magma.type;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.List;

import org.obiba.magma.MagmaEngine;
import org.obiba.magma.Value;
import org.obiba.magma.ValueLoaderFactory;
import org.obiba.magma.ValueSequence;

import com.google.common.collect.Lists;

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
    Class<?> type = object.getClass();
    if(byte[].class.equals(type)) {
      return Factory.newValue(this, (byte[]) object);
    }
    if(String.class.isAssignableFrom(type)) {
      return valueOf((String) object);
    }
    throw new IllegalArgumentException("Cannot construct " + getClass().getSimpleName() + " from type " + type + ".");
  }

  public ValueSequence sequenceOfReferences(ValueLoaderFactory factory, String string) {
    List<Value> values = Lists.newArrayList();
    Value refValues = TextType.get().sequenceOf(string);
    int occurrence = 0;
    for(Value refValue : refValues.asSequence().getValues()) {
      if(refValue.isNull()) {
        values.add(BinaryType.get().nullValue());
      } else {
        values.add(valueOf(factory.create(refValue.toString(), occurrence)));
      }
      occurrence++;
    }
    return BinaryType.get().sequenceOf(values);
  }

  public Value valueOfReference(ValueLoaderFactory factory, String string) {
    return valueOf(factory.create(string, null));
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
