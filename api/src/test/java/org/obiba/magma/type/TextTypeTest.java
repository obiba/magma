package org.obiba.magma.type;

import junit.framework.Assert;

import org.junit.Test;
import org.obiba.magma.MagmaRuntimeException;
import org.obiba.magma.Value;
import org.obiba.magma.ValueSequence;
import org.obiba.magma.ValueType;

import com.google.common.collect.ImmutableList;

public class TextTypeTest extends BaseValueTypeTest {

  @Override
  ValueType getValueType() {
    return TextType.get();
  }

  @Override
  Object getObjectForType() {
    return "This is a, \"test\"";
  }

  @Override
  boolean isDateTime() {
    return false;
  }

  @Override
  boolean isNumeric() {
    return false;
  }

  @Override
  Iterable<Class<?>> validClasses() {
    return ImmutableList.<Class<?>> of(String.class);
  }

  @Test(expected = MagmaRuntimeException.class)
  public void testUnterminatedQuotedString() {
    TextType.get().sequenceOf("\"An unterminated, value.");
  }

  @Test
  public void testSequenceOfOne() {
    ValueSequence sequence = TextType.get().sequenceOf("\"A value\"");
    assertSequence(sequence, "A value");
  }

  @Test
  public void testSequenceOfOneNull() {
    ValueSequence sequence = TextType.get().sequenceOf("\"\"");
    assertSequence(sequence, new String[] { null });
  }

  @Test
  public void testSequenceOfNulls() {
    ValueSequence sequence = TextType.get().sequenceOf("\"\",\"\"");
    assertSequence(sequence, null, null);
  }

  @Test
  public void testSequenceOfNullsAndValues() {
    ValueSequence sequence = TextType.get().sequenceOf("\"\",\"Not Null\",\"\"");
    assertSequence(sequence, null, "Not Null", null);
  }

  @Test
  public void testSequenceOfValueWithQuotes() {
    ValueSequence sequence = TextType.get().sequenceOf("\"\"\"Not Null\"\"\"");
    assertSequence(sequence, "\"Not Null\"");
  }

  @Test
  public void testSequenceOfValueWithQuotesAndSeparator() {
    ValueSequence sequence = TextType.get().sequenceOf("\"\"\"Not, Null\"\"\"");
    assertSequence(sequence, "\"Not, Null\"");
  }

  @Test
  public void testSequenceOfAQuote() {
    ValueSequence sequence = TextType.get().sequenceOf("\"\"\"\"");
    assertSequence(sequence, "\"");
  }

  @Test
  public void testSequenceOfQuotes() {
    ValueSequence sequence = TextType.get().sequenceOf("\"\"\"\",\"\"\"\"\"\",\"\"\"\"\"\"\"\"");
    assertSequence(sequence, "\"", "\"\"", "\"\"\"");
  }

  private void assertSequence(ValueSequence sequence, String... strings) {
    Assert.assertNotNull(sequence);
    Assert.assertEquals(strings.length, sequence.getValues().size());
    int index = 0;
    for(Value value : sequence.getValue()) {
      String string = strings[index];
      if(string == null) {
        Assert.assertTrue(value.isNull());
      } else {
        Assert.assertEquals(string, value.getValue());
      }
      index++;
    }
  }

}
