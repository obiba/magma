/*
 * Copyright (c) 2017 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.magma.js.methods;

import org.junit.Ignore;
import org.junit.Test;
import org.obiba.magma.js.AbstractJsTest;
import org.obiba.magma.js.ScriptableValue;
import org.obiba.magma.type.BooleanType;
import org.obiba.magma.type.DecimalType;
import org.obiba.magma.type.IntegerType;
import org.obiba.magma.type.TextType;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mozilla.javascript.Context.getCurrentContext;

@SuppressWarnings("ReuseOfLocalVariable")
public class TextMethodsTest extends AbstractJsTest {

  @Test
  public void testTrim() {
    ScriptableValue value = newValue(TextType.get().valueOf(" Value  "));
    ScriptableValue result = TextMethods.trim(getCurrentContext(), value, null, null);
    assertThat(result).isNotNull();
    assertThat(result.getValue()).isEqualTo(TextType.get().valueOf("Value"));
  }

  @Test
  public void testTrimValueSequence() {
    ScriptableValue value = newValue(TextType.get().sequenceOf("\" Value1  \",\"  Value2   \""));
    ScriptableValue result = TextMethods.trim(getCurrentContext(), value, null, null);
    assertThat(result).isNotNull();
    assertThat(result.getValue().asSequence().getSize()).isEqualTo(2);
    assertThat(result.getValue().asSequence().get(0)).isEqualTo(TextType.get().valueOf("Value1"));
    assertThat(result.getValue().asSequence().get(1)).isEqualTo(TextType.get().valueOf("Value2"));
  }

  @Test
  public void testMatches() {
    ScriptableValue value = newValue(TextType.get().valueOf(" Value  "));
    ScriptableValue result = TextMethods.matches(getCurrentContext(), value, new Object[] { "lue" }, null);
    assertThat(result).isNotNull();
    assertThat(result.getValue()).isEqualTo(BooleanType.get().valueOf(true));
  }

  @Test
  public void testMatchesNull() {
    ScriptableValue value = newValue(TextType.get().nullValue());
    ScriptableValue result = TextMethods.matches(getCurrentContext(), value, new Object[] { "lue" }, null);
    assertThat(result).isNotNull();
    assertThat(result.getValue()).isEqualTo(BooleanType.get().valueOf(false));
  }

  @Test
  public void testMatchesValueSequence() {
    ScriptableValue value = newValue(TextType.get().sequenceOf("\"Value\",\"Patate\""));
    ScriptableValue result = TextMethods.matches(getCurrentContext(), value, new Object[] { "lue" }, null);
    assertThat(result).isNotNull();
    assertThat(result.getValueType()).isEqualTo(BooleanType.get());
    assertThat(result.getValue().asSequence().getSize()).isEqualTo(2);
    assertThat(result.getValue().asSequence().get(0).getValueType()).isEqualTo(BooleanType.get());
    assertThat(result.getValue().asSequence().get(0)).isEqualTo(BooleanType.get().valueOf(true));
    assertThat(result.getValue().asSequence().get(1).getValueType()).isEqualTo(BooleanType.get());
    assertThat(result.getValue().asSequence().get(1)).isEqualTo(BooleanType.get().valueOf(false));
  }

  @Test
  public void testMatchesNullValueSequence() {
    ScriptableValue value = newValue(TextType.get().nullSequence());
    ScriptableValue result = TextMethods.matches(getCurrentContext(), value, new Object[] { "lue" }, null);
    assertThat(result).isNotNull();
    assertThat(result.getValueType()).isEqualTo(BooleanType.get());
    assertThat(result.getValue().isNull()).isTrue();
    assertThat(result.getValue().asSequence().getSize()).isEqualTo(0);
  }

  @Test
  public void testUpperCase() {
    ScriptableValue value = newValue(TextType.get().valueOf("value"));
    ScriptableValue result = TextMethods.upperCase(getCurrentContext(), value, null, null);
    assertThat(result).isNotNull();
    assertThat(result.getValue()).isEqualTo(TextType.get().valueOf("VALUE"));
  }

  @Ignore
  @Test
  public void testUpperCaseWithLocale() {
    ScriptableValue value = newValue(TextType.get().valueOf("français"));
    ScriptableValue result = TextMethods.upperCase(getCurrentContext(), value, new Object[] { "fr" }, null);
    assertThat(result).isNotNull();
    assertThat(result.getValue()).isEqualTo(TextType.get().valueOf("FRANÇAIS"));
  }

  @Test
  public void testUpperCaseValueSequence() {
    ScriptableValue value = newValue(TextType.get().sequenceOf("\"Value1\",\"Value2\""));
    ScriptableValue result = TextMethods.upperCase(getCurrentContext(), value, null, null);
    assertThat(result).isNotNull();
    assertThat(result.getValue().asSequence().getSize()).isEqualTo(2);
    assertThat(result.getValue().asSequence().get(0)).isEqualTo(TextType.get().valueOf("VALUE1"));
    assertThat(result.getValue().asSequence().get(1)).isEqualTo(TextType.get().valueOf("VALUE2"));
  }

  @Test
  public void testLowerCase() {
    ScriptableValue value = newValue(TextType.get().valueOf("VALUE"));
    ScriptableValue result = TextMethods.lowerCase(getCurrentContext(), value, null, null);
    assertThat(result).isNotNull();
    assertThat(result.getValue()).isEqualTo(TextType.get().valueOf("value"));
  }

  @Test
  public void testLowerCaseValueSequence() {
    ScriptableValue value = newValue(TextType.get().sequenceOf("\"Value1\",\"Value2\""));
    ScriptableValue result = TextMethods.lowerCase(getCurrentContext(), value, null, null);
    assertThat(result).isNotNull();
    assertThat(result.getValue().asSequence().getSize()).isEqualTo(2);
    assertThat(result.getValue().asSequence().get(0)).isEqualTo(TextType.get().valueOf("value1"));
    assertThat(result.getValue().asSequence().get(1)).isEqualTo(TextType.get().valueOf("value2"));
  }

  @Test
  public void testCapitalize() {
    ScriptableValue value = newValue(TextType.get().valueOf("  value foo bar"));
    ScriptableValue result = TextMethods.capitalize(getCurrentContext(), value, null, null);
    assertThat(result).isNotNull();
    assertThat(result.getValue()).isEqualTo(TextType.get().valueOf("  Value Foo Bar"));
  }

  @Test
  public void testCapitalizeWithDelimiters() {
    ScriptableValue value = newValue(TextType.get().valueOf("value:foo;bar_patate (toto) one"));
    ScriptableValue result = TextMethods.capitalize(getCurrentContext(), value, new String[] { ":", ";_", "(" }, null);
    assertThat(result).isNotNull();
    assertThat(result.getValue()).isEqualTo(TextType.get().valueOf("Value:Foo;Bar_Patate (Toto) one"));
  }

  @Test
  public void testCapitalizeValueSequence() {
    ScriptableValue value = newValue(TextType.get().sequenceOf("\"value1\",\"value2\""));
    ScriptableValue result = TextMethods.capitalize(getCurrentContext(), value, null, null);
    assertThat(result).isNotNull();
    assertThat(result.getValue().asSequence().getSize()).isEqualTo(2);
    assertThat(result.getValue().asSequence().get(0)).isEqualTo(TextType.get().valueOf("Value1"));
    assertThat(result.getValue().asSequence().get(1)).isEqualTo(TextType.get().valueOf("Value2"));
  }

  @Test
  public void testReplace() {
    ScriptableValue value = newValue(TextType.get().valueOf("H2R 2E1"));
    ScriptableValue result = TextMethods.replace(getCurrentContext(), value, new Object[] { " 2E1", "" }, null);
    assertThat(result).isNotNull();
    assertThat(result.getValue()).isEqualTo(TextType.get().valueOf("H2R"));
  }

  @Test
  public void testStringConcatString() throws Exception {
    ScriptableValue hello = newValue(TextType.get().valueOf("Hello "));
    ScriptableValue world = newValue(TextType.get().valueOf("World!"));
    ScriptableValue result = TextMethods.concat(getCurrentContext(), hello, new ScriptableValue[] { world }, null);
    assertThat(result.getValue()).isEqualTo(TextType.get().valueOf("Hello World!"));
  }

  @Test
  public void testStringConcatInteger() throws Exception {
    ScriptableValue hello = newValue(TextType.get().valueOf("Hello "));
    ScriptableValue twentyThree = newValue(IntegerType.get().valueOf(23));
    ScriptableValue result = TextMethods
        .concat(getCurrentContext(), hello, new ScriptableValue[] { twentyThree }, null);
    assertThat(result.getValue()).isEqualTo(TextType.get().valueOf("Hello 23"));
  }

  @Test
  public void testDecimalConcatString() throws Exception {
    ScriptableValue twentyThreePointThirtyTwo = newValue(DecimalType.get().valueOf(23.32));
    ScriptableValue hello = newValue(TextType.get().valueOf(" Hello"));
    ScriptableValue result = TextMethods
        .concat(getCurrentContext(), twentyThreePointThirtyTwo, new ScriptableValue[] { hello }, null);
    assertThat(result.getValue()).isEqualTo(TextType.get().valueOf("23.32 Hello"));
  }

  @Test
  public void testDecimalConcatTrue() throws Exception {
    ScriptableValue twentyThreePointThirtyTwo = newValue(DecimalType.get().valueOf(23.32));
    ScriptableValue trueOperand = newValue(BooleanType.get().trueValue());
    ScriptableValue result = TextMethods
        .concat(getCurrentContext(), twentyThreePointThirtyTwo, new ScriptableValue[] { trueOperand }, null);
    assertThat(result.getValue()).isEqualTo(TextType.get().valueOf("23.32true"));
  }

  @Test
  public void testDecimalConcatNull() throws Exception {
    ScriptableValue twentyThreePointThirtyTwo = newValue(DecimalType.get().valueOf(23.32));
    ScriptableValue nullOperand = newValue(BooleanType.get().nullValue());
    ScriptableValue result = TextMethods
        .concat(getCurrentContext(), twentyThreePointThirtyTwo, new ScriptableValue[] { nullOperand }, null);
    assertThat(result.getValue()).isEqualTo(TextType.get().valueOf("23.32null"));
  }

  @Test
  public void testNullConcatString() throws Exception {
    ScriptableValue nullOperand = newValue(TextType.get().nullValue());
    ScriptableValue world = newValue(TextType.get().valueOf("World!"));
    ScriptableValue result = TextMethods
        .concat(getCurrentContext(), nullOperand, new ScriptableValue[] { world }, null);
    assertThat(result.getValue()).isEqualTo(TextType.get().valueOf("nullWorld!"));
  }

  @Test
  public void testConcatMultipleArguments() throws Exception {
    ScriptableValue hello = newValue(TextType.get().valueOf("Hello "));
    ScriptableValue world = newValue(TextType.get().valueOf("World!"));
    ScriptableValue greet = newValue(TextType.get().valueOf("How are you, "));
    ScriptableValue result = TextMethods
        .concat(getCurrentContext(), hello, new Object[] { world, " ", greet, "Mr. Potato Head", "?" }, null);
    assertThat(result.getValue()).isEqualTo(TextType.get().valueOf("Hello World! How are you, Mr. Potato Head?"));
  }

  @Test
  public void testMapWithSimpleMappingAndNormalInput() {
    ScriptableValue value = evaluate("map({'YES':1, 'NO':2})", TextType.get().valueOf("YES"));
    assertThat(value).isNotNull();
    assertThat(value.getValue()).isEqualTo(TextType.get().valueOf(1));

    value = evaluate("map({'YES':1, 'NO':2})", TextType.get().valueOf("NO"));
    assertThat(value).isNotNull();
    assertThat(value.getValue()).isEqualTo(TextType.get().valueOf(2));
  }

  @Test
  public void testMapWithMappingThatHasIntegerKeyAndStringLookup() {
    ScriptableValue value = evaluate("map({'YES':1, '996':2})", TextType.get().valueOf("996"));
    assertThat(value).isNotNull();
    assertThat(value.getValue()).isEqualTo(TextType.get().valueOf(2));
  }

  @Test
  public void testMapWithMappingThatHasIntegerKeyAndIntegerLookup() {
    ScriptableValue value = evaluate("map({'YES':1, '996':2})", IntegerType.get().valueOf("996"));
    assertThat(value).isNotNull();
    assertThat(value.getValue()).isEqualTo(TextType.get().valueOf(2));
  }

  @Test
  public void testMapWithMappingThatHasIntegerKeysOnly() {
    ScriptableValue value = evaluate("map({999:1, 996:2})", IntegerType.get().valueOf("996"));
    assertThat(value).isNotNull();
    assertThat(value.getValue()).isEqualTo(TextType.get().valueOf(2));
  }

  @Test
  public void testMapWithMappingThatHasIntegerKeyAndNotFoundValue() {
    ScriptableValue value = evaluate("map({999:1, 996:2})", IntegerType.get().valueOf("998"));
    assertThat(value).isNotNull();
    assertThat(value.getValue()).isEqualTo(TextType.get().nullValue());
  }

  @Test
  public void testMapWithSimpleMappingAndNullInput() {
    ScriptableValue value = evaluate("map({'YES':1, 'NO':2})", TextType.get().nullValue());
    assertThat(value).isNotNull();
    assertThat(value.getValue()).isEqualTo(TextType.get().nullValue());
  }

  @Test
  public void testMapAcceptsNullValueAsOutput() {
    ScriptableValue value = evaluate("map({'YES':null, 'NO':2}, 'DEFAULT')", TextType.get().valueOf("YES"));
    assertThat(value).isNotNull();
    assertThat(value.getValue()).isEqualTo(TextType.get().nullValue());
  }

  @Test
  public void testMapWithSimpleMappingAndMapNotDefinedValue() {
    ScriptableValue value = evaluate("map({'YES':1, 'NO':2})", TextType.get().valueOf("NOT IN MAP"));
    assertThat(value).isNotNull();
    assertThat(value.getValue()).isEqualTo(TextType.get().nullValue());
  }

  @Test
  public void testMapWithTextMappingAndMapNotDefinedValue() {
    ScriptableValue value = evaluate("map({'YES':'1', 'NO':'2'})", TextType.get().valueOf("NOT IN MAP"));
    assertThat(value).isNotNull();
    assertThat(value.getValue()).isEqualTo(TextType.get().nullValue());
  }

  @Test
  public void testMapWithSimpleMappingAndMapNotDefinedValueWithDefaultValue() {
    ScriptableValue value = evaluate("map({'YES':1, 'NO':2}, 9999)", TextType.get().valueOf("NOT IN MAP"));
    assertThat(value).isNotNull();
    assertThat(value.getValue()).isEqualTo(TextType.get().valueOf(9999));
  }

  @Test
  public void testMapWithSimpleMappingAndNullValueWithNullValueMap() {
    ScriptableValue value = evaluate("map({'YES':1, 'NO':2}, 9999, 8888)", TextType.get().nullValue());
    assertThat(value).isNotNull();
    assertThat(value.getValue()).isEqualTo(TextType.get().valueOf(8888));
  }

  @Test
  public void testMapWithSimpleMappingAndNullValueWithDefaultValueMap() {
    ScriptableValue value = evaluate("map({'YES':1, 'NO':2}, 9999)", TextType.get().nullValue());
    assertThat(value).isNotNull();
    assertThat(value.getValue()).isEqualTo(TextType.get().valueOf(9999));
  }

  @Test
  public void testMapWithSimpleMappingAndNullValueWithNullSequenceMap() {
    ScriptableValue value = evaluate("map({'YES':1, 'NO':2}, 9999, 8888)", TextType.get().nullSequence());
    assertThat(value).isNotNull();
    assertThat(value.getValue().isNull()).isTrue();
    assertThat(value.getValue().isSequence()).isTrue();
    assertThat(value.getValue().asSequence()).isEqualTo(TextType.get().nullSequence());
  }

  @Test
  public void testMapWithSimpleMappingAndNullSequenceWithDefaultValueMap() {
    ScriptableValue value = evaluate("map({'YES':1, 'NO':2}, 9999)", TextType.get().nullSequence());
    assertThat(value).isNotNull();
    assertThat(value.getValue().isNull()).isTrue();
    assertThat(value.getValue().isSequence()).isTrue();
    assertThat(value.getValue().asSequence()).isEqualTo(TextType.get().nullSequence());
  }

  @Test
  public void testMapWithSimpleMappingAndSequenceInput() {
    ScriptableValue value = evaluate("map({'YES':1, 'NO':2})", TextType.get().sequenceOf("YES,NO"));
    assertThat(value).isNotNull();
    assertThat(value.getValue().isSequence()).isTrue();
    assertThat(value.getValue().asSequence()).isEqualTo(TextType.get().sequenceOf("1,2"));
  }

  @Test
  public void testMapWithFunctionMapping() {
    ScriptableValue value = evaluate("map({'YES':function(value){return value.concat('-YES');}, 'NO':2})",
        TextType.get().valueOf("YES"));
    assertThat(value).isNotNull();
    assertThat(value.getValue()).isEqualTo(TextType.get().valueOf("YES-YES"));
  }

  @Test
  public void testMapWithDefaultFunctionMapping() {
    ScriptableValue value = evaluate("map({'YES':1, 'NO':2},function(value){return value.concat('-0');})",
        TextType.get().valueOf("PERHAPS"));
    assertThat(value).isNotNull();
    assertThat(value.getValue()).isEqualTo(TextType.get().valueOf("PERHAPS-0"));

    value = evaluate("map({'YES':1, 'NO':2},function(value){return value == null ? '0' : value.concat('-0');})",
        TextType.get().nullValue());
    assertThat(value).isNotNull();
    assertThat(value.getValue()).isEqualTo(TextType.get().valueOf("0"));
  }

  @Test
  public void testMapWithDefaultAndNullFunctionsMapping() {
    ScriptableValue value = evaluate("map({'YES':1, 'NO':2},function(value){return value.concat('-0');},function(){return 0;})",
        TextType.get().nullValue());
    assertThat(value).isNotNull();
    assertThat(value.getValue()).isEqualTo(TextType.get().valueOf("0"));
  }

  @Test
  public void testDateConvertWithFormat() {
    ScriptableValue value = newValue(TextType.get().valueOf("10/23/12"));
    ScriptableValue date = TextMethods.date(getCurrentContext(), value, new Object[] { "MM/dd/yy" }, null);
    assertThat(date.getValue().toString()).isEqualTo("2012-10-23");
  }

  @Test
  public void testEmptyDateConvertWithFormat() {
    ScriptableValue value = newValue(TextType.get().valueOf(""));
    ScriptableValue date = TextMethods.date(getCurrentContext(), value, new Object[] { "MM/dd/yy" }, null);
    assertThat(date.getValue().isNull()).isTrue();
  }

  @Test
  public void testTrimEmptyDateConvertWithFormat() {
    ScriptableValue value = newValue(TextType.get().valueOf(" "));
    ScriptableValue date = TextMethods.date(getCurrentContext(), value, new Object[] { "MM/dd/yy" }, null);
    assertThat(date.getValue().isNull()).isTrue();
  }

  @Test
  public void testDatetimeConvertWithFormat() {
    ScriptableValue value = newValue(TextType.get().valueOf("10/23/12 10:59 PM"));
    ScriptableValue date = TextMethods.datetime(getCurrentContext(), value, new Object[] { "MM/dd/yy h:mm a" }, null);
    String str = date.getValue().toString();
    // exclude timezone from the test
    assertThat(str).isNotNull();
    //noinspection ConstantConditions
    assertThat(str.substring(0, str.lastIndexOf(':'))).isEqualTo("2012-10-23T22:59");
  }

  @Test
  public void testEmptyDatetimeConvertWithFormat() {
    ScriptableValue value = newValue(TextType.get().valueOf(""));
    ScriptableValue date = TextMethods.datetime(getCurrentContext(), value, new Object[] { "MM/dd/yy h:mm a" }, null);
    assertThat(date.getValue().isNull()).isTrue();
  }

  @Test
  public void testTrimEmptyDatetimeConvertWithFormat() {
    ScriptableValue value = newValue(TextType.get().valueOf(" "));
    ScriptableValue date = TextMethods.datetime(getCurrentContext(), value, new Object[] { "MM/dd/yy h:mm a" }, null);
    assertThat(date.getValue().isNull()).isTrue();
  }

}
