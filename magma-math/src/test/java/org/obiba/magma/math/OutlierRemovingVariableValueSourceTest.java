/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.magma.math;

import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.obiba.magma.MagmaEngine;
import org.obiba.magma.Value;
import org.obiba.magma.ValueSet;
import org.obiba.magma.ValueSource;
import org.obiba.magma.ValueTable;
import org.obiba.magma.Variable;
import org.obiba.magma.VariableEntity;
import org.obiba.magma.VariableValueSource;
import org.obiba.magma.VariableValueSourceWrapper;
import org.obiba.magma.VectorSource;
import org.obiba.magma.support.Values;
import org.obiba.magma.type.IntegerType;

import com.google.common.collect.Iterables;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class OutlierRemovingVariableValueSourceTest {

  private final ValueTable mockTable = mock(ValueTable.class);

  private final VariableValueSource mockSource = mock(VariableValueSource.class);

  private final VectorSource mockVector = mock(VectorSource.class);

  private final ValueSet mockValueSet = mock(ValueSet.class);

  private final SortedSet<VariableEntity> emptySet = new TreeSet<>();

  private Variable testVariable;

  @Before
  public void startYourEngine() {
    new MagmaEngine();
    testVariable = Variable.Builder.newVariable("test-variable", IntegerType.get(), "test").build();
  }

  @After
  public void stopYourEngine() {
    MagmaEngine.get().shutdown();
  }

  @SuppressWarnings("ConstantConditions")
  @Test(expected = IllegalArgumentException.class)
  public void test_ctor_throwsIllegalArgumentNullTable() {
    new OutlierRemovingVariableValueSource(null, mockSource);
  }

  @SuppressWarnings("ConstantConditions")
  @Test(expected = IllegalArgumentException.class)
  public void test_ctor_throwsIllegalArgumentNullSource() {
    new OutlierRemovingVariableValueSource(mockTable, null);
  }

  @SuppressWarnings("ConstantConditions")
  @Test(expected = IllegalArgumentException.class)
  public void test_ctor_throwsIllegalArgumentNullStatsProvider() {
    when(mockSource.supportVectorSource()).thenReturn(true);
    when(mockSource.asVectorSource()).thenReturn(mockVector);
    new OutlierRemovingVariableValueSource(mockTable, mockSource, null);
  }

  @Test
  public void test_getters_delegates() {
    when(mockSource.getVariable()).thenReturn(testVariable);
    when(mockSource.getValueType()).thenReturn(testVariable.getValueType());
    when(mockSource.supportVectorSource()).thenReturn(true);
    when(mockSource.asVectorSource()).thenReturn(mockVector);
    VariableValueSourceWrapper testedSource = new OutlierRemovingVariableValueSource(mockTable, mockSource);
    assertThat(testedSource.getVariable()).isEqualTo(testVariable);
    assertThat(testedSource.getValueType()).isEqualTo(testVariable.getValueType());
    assertThat(testedSource.getWrapped()).isEqualTo(mockSource);
  }

  @Test
  public void test_getValue_returnsNonOutlierAsIs() {
    // Make a non-outlier value to test
    Value testValue = IntegerType.get().valueOf(1);
    when(mockSource.getValue(mockValueSet)).thenReturn(testValue);
    setupForStatsCompute(Values.asValues(IntegerType.get(), 1, 2, 3, 4));

    ValueSource testedSource = new OutlierRemovingVariableValueSource(mockTable, mockSource,
        new DefaultDescriptiveStatisticsProvider());
    assertThat(testedSource.getValue(mockValueSet)).isEqualTo(testValue);
  }

  @Test
  public void test_getValue_returnsNullForOutliers() {
    // Make an outlier value to test
    Value testValue = IntegerType.get().valueOf(1000);
    when(mockSource.getValue(mockValueSet)).thenReturn(testValue);
    setupForStatsCompute(Values.asValues(IntegerType.get(), 1, 2, 3, 4));

    ValueSource testedSource = new OutlierRemovingVariableValueSource(mockTable, mockSource,
        new DefaultDescriptiveStatisticsProvider());
    assertThat(testedSource.getValue(mockValueSet)).isEqualTo(IntegerType.get().nullValue());
  }

  @Test
  public void test_getValues_returnsNullForOutliers() {
    DescriptiveStatistics ds = new DescriptiveStatistics();
    ds.addValue(1);
    ds.addValue(2);
    ds.addValue(3);
    ds.addValue(4);

    Iterable<Value> testValues = Values.asValues(IntegerType.get(), 1, 2, 3, 4, 10000, -1000);
    Iterable<Value> expectedValues = Values.asValues(IntegerType.get(), 1, 2, 3, 4, null, null);
    DescriptiveStatisticsProvider mockProvider = mock(DescriptiveStatisticsProvider.class);
    when(mockProvider.compute(mockSource, emptySet)).thenReturn(ds);
    setupForStatsCompute(testValues);

    ValueSource testedSource = new OutlierRemovingVariableValueSource(mockTable, mockSource, mockProvider);

    assertThat(Iterables.elementsEqual(testedSource.asVectorSource().getValues(emptySet), expectedValues)).isTrue();
  }

  @Test
  public void test_getValue_nullsNotAffected() {
    when(mockSource.getValue(mockValueSet)).thenReturn(IntegerType.get().nullValue());
    setupForStatsCompute(Values.asValues(IntegerType.get(), 1, 2, 3, 4));

    ValueSource testedSource = new OutlierRemovingVariableValueSource(mockTable, mockSource,
        new DefaultDescriptiveStatisticsProvider());
    assertThat(testedSource.getValue(mockValueSet)).isEqualTo(IntegerType.get().nullValue());
  }

  private void setupForStatsCompute(Iterable<Value> values) {
    when(mockSource.supportVectorSource()).thenReturn(true);
    when(mockSource.asVectorSource()).thenReturn(mockVector);
    when(mockSource.getValueType()).thenReturn(IntegerType.get());
    when(mockTable.getVariableEntities()).thenReturn(emptySet);
    when(mockVector.getValues(emptySet)).thenReturn(values);
  }

}
