package org.obiba.magma.support;

import org.junit.Test;
import org.obiba.magma.Value;
import org.obiba.magma.test.AbstractMagmaTest;
import org.obiba.magma.type.IntegerType;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class ValuesTest extends AbstractMagmaTest {

  @Test
  public void test_toValueFunction_returnsFunctionForProperType() {
    Function<Object, Value> func = Values.toValueFunction(IntegerType.get());
    assertThat(func.apply(new Integer(1)), is(IntegerType.get().valueOf(1)));
  }

  @Test
  public void test_toValueFunction_handlesNull() {
    Function<Object, Value> func = Values.toValueFunction(IntegerType.get());
    assertThat(func.apply(null), is(IntegerType.get().nullValue()));
  }

  @Test
  public void test_asValues_returnsValuesOfProperType() {
    Iterable<Value> values = Values.asValues(IntegerType.get(), 1, 2);
    assertThat(Iterables
        .elementsEqual(values, ImmutableList.<Value>of(IntegerType.get().valueOf(1), IntegerType.get().valueOf(2))),
        is(true));
  }

  @Test
  public void test_asValues_handlesNull() {
    Iterable<Value> values = Values.asValues(IntegerType.get(), 1, null);
    assertThat(Iterables
        .elementsEqual(values, ImmutableList.<Value>of(IntegerType.get().valueOf(1), IntegerType.get().nullValue())),
        is(true));
  }

  @Test
  public void test_asValues_handlesSingleton() {
    Iterable<Value> values = Values.asValues(IntegerType.get(), 1);
    assertThat(Iterables.elementsEqual(values, ImmutableList.<Value>of(IntegerType.get().valueOf(1))), is(true));
  }

  @Test
  public void test_asValues_handlesEmtpyArray() {
    Iterable<Value> values = Values.asValues(IntegerType.get());
    assertThat(Iterables.elementsEqual(values, ImmutableList.<Value>of()), is(true));
  }

}
