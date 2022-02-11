/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.magma.type;

import java.io.IOException;
import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.util.List;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

import org.json.JSONObject;
import org.obiba.magma.MagmaEngine;
import org.obiba.magma.Value;
import org.obiba.magma.ValueLoaderFactory;
import org.obiba.magma.ValueSequence;

import com.google.common.collect.Lists;

public class BinaryType extends AbstractValueType {

  private static final long serialVersionUID = -5271259966499174607L;

  @SuppressWarnings("StaticNonFinalField")
  private static WeakReference<BinaryType> instance;

  private BinaryType() {

  }

  @SuppressWarnings("ConstantConditions")
  @NotNull
  public static BinaryType get() {
    if(instance == null || instance.get() == null) {
      instance = MagmaEngine.get().registerInstance(new BinaryType());
    }
    return instance.get();
  }

  @NotNull
  @Override
  public String getName() {
    return "binary";
  }

  @Override
  public Class<?> getJavaClass() {
    return byte[].class;
  }

  @Override
  public boolean acceptsJavaClass(@NotNull Class<?> clazz) {
    return byte[].class.isAssignableFrom(clazz);
  }

  @Override
  public boolean isDateTime() {
    return false;
  }

  @Override
  public boolean isNumeric() {
    return false;
  }

  @Override
  public boolean isBinary() {
    return true;
  }

  @Override
  protected String toString(Object object) {
    return Base64.encodeBytes((byte[]) object);
  }

  @NotNull
  @Override
  public Value valueOf(@Nullable String string) {
    if(string == null) {
      return nullValue();
    }
    try {
      return Factory.newValue(this, Base64.decode(string, Base64.DONT_GUNZIP));
    } catch(IOException e) {
      throw new IllegalArgumentException("Invalid Base64 encoding. Cannot construct binary Value instance.", e);
    }
  }

  @NotNull
  @Override
  public Value valueOf(@Nullable Object object) {
    // input type is expected to be byte[]
    if(object == null || object.equals(JSONObject.NULL)) {
      return nullValue();
    }
    Class<?> type = object.getClass();
    if(byte[].class.equals(type)) {
      return Factory.newValue(this, (Serializable) object);
    }
    if(String.class.isAssignableFrom(type)) {
      return valueOf((String) object);
    }
    if(object instanceof Value) {
      return convert((Value)object);
    }
    throw new IllegalArgumentException("Cannot construct " + getClass().getSimpleName() + " from type " + type + ".");
  }

  public ValueSequence sequenceOfReferences(ValueLoaderFactory factory, String string) {
    Value refValues = TextType.get().sequenceOf(string);
    return sequenceOfReferences(factory, refValues);
  }

  public ValueSequence sequenceOfReferences(ValueLoaderFactory factory, Value refValues) {
    List<Value> values = Lists.newArrayList();
    int occurrence = 0;
    for(Value refValue : refValues.asSequence().getValues()) {
      if(refValue.isNull()) {
        values.add(get().nullValue());
      } else {
        values.add(valueOf(factory.create(refValue, occurrence)));
      }
      occurrence++;
    }
    return get().sequenceOf(values);
  }

  public Value valueOfReference(ValueLoaderFactory factory, String string) {
    return valueOfReference(factory, TextType.get().valueOf(string));
  }

  public Value valueOfReference(ValueLoaderFactory factory, Value refValue) {
    return valueOf(factory.create(refValue, null));
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
