package org.obiba.magma.type;

import java.lang.ref.WeakReference;

import javax.xml.namespace.QName;

import org.obiba.magma.MetaEngine;
import org.obiba.magma.Value;

public class BinaryType extends AbstractValueType {

  private static final long serialVersionUID = -5271259966499174607L;

  private static WeakReference<BinaryType> instance;

  private BinaryType() {

  }

  public static BinaryType get() {
    if(instance == null || instance.get() == null) {
      instance = MetaEngine.get().registerInstance(new BinaryType());
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

  public QName getXsdType() {
    return new QName("xsd", "base64Binary");
  }

  public boolean isDateTime() {
    return false;
  }

  public boolean isNumeric() {
    return false;
  }

  @Override
  public String toString(Value value) {
    // TODO: Base64 encode
    throw new UnsupportedOperationException("method not implemented");
  }

  @Override
  public Value valueOf(String string) {
    // TODO: Base64 decode
    throw new UnsupportedOperationException("method not implemented");
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
    throw new UnsupportedOperationException("method not implemented");
  }

}
