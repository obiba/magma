/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.magma.math.summary;

import com.google.common.collect.ImmutableList;
import org.junit.Test;
import org.mockito.Mockito;
import org.obiba.magma.*;
import org.obiba.magma.math.CategoricalSummary;
import org.obiba.magma.math.Frequency;
import org.obiba.magma.support.NullTimestamps;
import org.obiba.magma.support.Values;
import org.obiba.magma.test.AbstractMagmaTest;
import org.obiba.magma.type.DecimalType;
import org.obiba.magma.type.IntegerType;
import org.obiba.magma.type.TextType;

import java.util.ArrayList;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 *
 */
public class CategoricalVariableSummaryTest extends AbstractMagmaTest {

  @Test
  public void test_withTextType() {
    Variable variable = Variable.Builder.newVariable("mock", TextType.get(), "mock")
        .addCategories("YES", "NO", "DNK", "PNA").build();
    CategoricalVariableSummary summary = computeFromTable(variable,
        Values.asValues(TextType.get(), "YES", "NO", "YES", "PNA", "DNK"));
    assertThat(summary.getMode()).isEqualTo("YES");
  }

  @Test
  public void test_withIntegerType() {
    Variable variable = Variable.Builder.newVariable("mock", IntegerType.get(), "mock")
        .addCategories("1", "2", "3", "9").build();
    CategoricalVariableSummary summary = computeFromTable(variable,
        Values.asValues(IntegerType.get(), "1", "2", "1", "9", "3"));
    assertThat(summary.getMode()).isEqualTo("1");
    for (Frequency frequency : summary.getFrequencies()) {
      switch (frequency.getValue()) {
        case "1":
          assertThat(frequency.getFreq()).isEqualTo(2);
          break;
        case "2":
          assertThat(frequency.getFreq()).isEqualTo(1);
          break;
        case "3":
          assertThat(frequency.getFreq()).isEqualTo(1);
          break;
        case "9":
          assertThat(frequency.getFreq()).isEqualTo(1);
          break;
      }
    }
  }

  @Test
  public void test_withDecimalType() {
    Variable variable = Variable.Builder.newVariable("mock", IntegerType.get(), "mock")
        .addCategories("1", "2", "3", "9").build();
    CategoricalVariableSummary summary = computeFromTable(variable,
        Values.asValues(DecimalType.get(), "1", "2", "1", "9", "3"));
    assertThat(summary.getMode()).isEqualTo("1");
    for (Frequency frequency : summary.getFrequencies()) {
      switch (frequency.getValue()) {
        case "1":
          assertThat(frequency.getFreq()).isEqualTo(2);
          break;
        case "2":
          assertThat(frequency.getFreq()).isEqualTo(1);
          break;
        case "3":
          assertThat(frequency.getFreq()).isEqualTo(1);
          break;
        case "9":
          assertThat(frequency.getFreq()).isEqualTo(1);
          break;
      }
    }
  }

  @Test
  public void test_withNullValue() {
    Variable variable = Variable.Builder.newVariable("mock", TextType.get(), "mock")
        .addCategories("YES", "NO", "DNK", "PNA").build();
    CategoricalVariableSummary summary = computeFromTable(variable,
        Values.asValues(TextType.get(), "YES", "NO", null, null));
    assertThat(summary.getMode()).isEqualTo(CategoricalSummary.NULL_NAME);
  }

  @Test
  public void test_withSequence() {
    Variable variable = Variable.Builder.newVariable("mock", TextType.get(), "mock").addCategories("CAT1", "CAT2")
        .build();
    CategoricalVariableSummary summary = computeFromTable(variable,
        ImmutableList.of(Values.asSequence(TextType.get(), "CAT1", "CAT2"), Values.asSequence(TextType.get(), "CAT1")));
    assertThat(summary.getMode()).isEqualTo("CAT1");
  }

  @Test
  public void test_withSequenceThatContainsNullValue() {
    Variable variable = Variable.Builder.newVariable("mock", TextType.get(), "mock").addCategories("CAT1", "CAT2")
        .build();
    CategoricalVariableSummary summary = computeFromTable(variable, ImmutableList
        .of(Values.asSequence(TextType.get(), "CAT1", "CAT2"), Values.asSequence(TextType.get(), "CAT1", null)));
    assertThat(summary.getMode()).isEqualTo("CAT1");
  }

  @Test
  public void test_withNullSequence() {
    Variable variable = Variable.Builder.newVariable("mock", TextType.get(), "mock").addCategories("CAT1", "CAT2")
        .build();
    CategoricalVariableSummary summary = computeFromTable(variable,
        ImmutableList.of(TextType.get().nullSequence(), Values.asSequence(TextType.get(), "CAT1")));
    assertThat(summary.getMode()).isEqualTo("CAT1");
  }

  private CategoricalVariableSummary computeFromTable(Variable variable, Iterable<Value> values) {

    VectorSource vectorSource = mock(VectorSource.class);
    when(vectorSource.getValues(Mockito.any())).thenReturn(values);

    VariableValueSource valueSource = mock(VariableValueSource.class);
    when(valueSource.supportVectorSource()).thenReturn(true);
    when(valueSource.asVectorSource()).thenReturn(vectorSource);

    ValueTable table = mock(ValueTable.class);
    when(table.getTimestamps()).thenReturn(NullTimestamps.get());
    when(table.getVariableEntities()).thenReturn(new ArrayList<>());
    when(table.getVariableValueSource(variable.getName())).thenReturn(valueSource);

    CategoricalVariableSummary summary = new CategoricalVariableSummary.Builder(variable)
        .addTable(table, table.getVariableValueSource(variable.getName())).build();
    assertThat(summary).isNotNull();
    return summary;
  }

}
