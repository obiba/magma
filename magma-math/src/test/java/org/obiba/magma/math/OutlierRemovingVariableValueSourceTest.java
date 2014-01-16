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

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.fest.assertions.api.Assertions.assertThat;

public class OutlierRemovingVariableValueSourceTest {

  ValueTable mockTable = createMock(ValueTable.class);

  VariableValueSource mockSource = createMock(VariableValueSource.class);

  VectorSource mockVector = createMock(VectorSource.class);

  ValueSet mockValueSet = createMock(ValueSet.class);

  SortedSet<VariableEntity> emptySet = new TreeSet<>();

  Variable testVariable;

  @Before
  public void startYourEngine() {
    new MagmaEngine();
    testVariable = Variable.Builder.newVariable("test-variable", IntegerType.get(), "test").build();
  }

  @After
  public void stopYourEngine() {
    MagmaEngine.get().shutdown();
  }

  @Test(expected = IllegalArgumentException.class)
  public void test_ctor_throwsIllegalArgumentNullTable() {
    new OutlierRemovingVariableValueSource(null, mockSource);
  }

  @Test(expected = IllegalArgumentException.class)
  public void test_ctor_throwsIllegalArgumentNullSource() {
    new OutlierRemovingVariableValueSource(mockTable, null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void test_ctor_throwsIllegalArgumentNullVectorSource() {
    expect(mockSource.asVectorSource()).andReturn(null).anyTimes();
    new OutlierRemovingVariableValueSource(mockTable, mockSource);
  }

  @Test(expected = IllegalArgumentException.class)
  public void test_ctor_throwsIllegalArgumentNullStatsProvider() {
    expect(mockSource.asVectorSource()).andReturn(mockVector).anyTimes();
    replay(mockSource);
    new OutlierRemovingVariableValueSource(mockTable, mockSource, null);
  }

  @Test
  public void test_getters_delegates() {
    expect(mockSource.getVariable()).andReturn(testVariable).anyTimes();
    expect(mockSource.getValueType()).andReturn(testVariable.getValueType()).anyTimes();
    expect(mockSource.asVectorSource()).andReturn(mockVector).anyTimes();
    replay(mockSource);
    VariableValueSourceWrapper testedSource = new OutlierRemovingVariableValueSource(mockTable, mockSource);
    assertThat(testedSource.getVariable()).isEqualTo(testVariable);
    assertThat(testedSource.getValueType()).isEqualTo(testVariable.getValueType());
    assertThat(testedSource.getWrapped()).isEqualTo(mockSource);
    verify(mockSource);
  }

  @Test
  public void test_getValue_returnsNonOutlierAsIs() {
    // Make a non-outlier value to test
    Value testValue = IntegerType.get().valueOf(1);
    expect(mockSource.getValue(mockValueSet)).andReturn(testValue).anyTimes();
    setupForStatsCompute(Values.asValues(IntegerType.get(), 1, 2, 3, 4));

    ValueSource testedSource = new OutlierRemovingVariableValueSource(mockTable, mockSource,
        new DefaultDescriptiveStatisticsProvider());
    assertThat(testedSource.getValue(mockValueSet)).isEqualTo(testValue);

    verify(mockSource);
  }

  @Test
  public void test_getValue_returnsNullForOutliers() {
    // Make an outlier value to test
    Value testValue = IntegerType.get().valueOf(1000);
    expect(mockSource.getValue(mockValueSet)).andReturn(testValue).anyTimes();
    setupForStatsCompute(Values.asValues(IntegerType.get(), 1, 2, 3, 4));

    ValueSource testedSource = new OutlierRemovingVariableValueSource(mockTable, mockSource,
        new DefaultDescriptiveStatisticsProvider());
    assertThat(testedSource.getValue(mockValueSet)).isEqualTo(IntegerType.get().nullValue());

    verify(mockSource);
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
    DescriptiveStatisticsProvider mockProvider = createMock(DescriptiveStatisticsProvider.class);
    expect(mockProvider.compute(mockSource, emptySet)).andReturn(ds);
    replay(mockProvider);
    setupForStatsCompute(testValues);

    ValueSource testedSource = new OutlierRemovingVariableValueSource(mockTable, mockSource, mockProvider);

    assertThat(Iterables.elementsEqual(testedSource.asVectorSource().getValues(emptySet), expectedValues)).isTrue();

    verify(mockSource);
  }

  @Test
  public void test_getValue_nullsNotAffected() {
    expect(mockSource.getValue(mockValueSet)).andReturn(IntegerType.get().nullValue()).anyTimes();
    setupForStatsCompute(Values.asValues(IntegerType.get(), 1, 2, 3, 4));

    ValueSource testedSource = new OutlierRemovingVariableValueSource(mockTable, mockSource,
        new DefaultDescriptiveStatisticsProvider());
    assertThat(testedSource.getValue(mockValueSet)).isEqualTo(IntegerType.get().nullValue());

    verify(mockSource);

  }

  private void setupForStatsCompute(Iterable<Value> values) {
    expect(mockSource.asVectorSource()).andReturn(mockVector).anyTimes();
    expect(mockSource.getValueType()).andReturn(IntegerType.get()).anyTimes();
    expect(mockTable.getVariableEntities()).andReturn(emptySet).anyTimes();
    expect(mockVector.getValues(emptySet)).andReturn(values).anyTimes();
    replay(mockSource, mockTable, mockVector);
  }

}
