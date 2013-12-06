package org.obiba.magma.math;

import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.obiba.magma.MagmaEngine;
import org.obiba.magma.Value;
import org.obiba.magma.Variable;
import org.obiba.magma.VariableEntity;
import org.obiba.magma.VariableValueSource;
import org.obiba.magma.VectorSource;
import org.obiba.magma.support.Values;
import org.obiba.magma.type.IntegerType;

import com.google.common.collect.ImmutableList;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

public class ExcludeMissingDescriptiveStatisticsProviderTest {

  SortedSet<VariableEntity> emptySet = new TreeSet<>();

  Variable testVariable;

  @Before
  public void startYourEngine() {
    new MagmaEngine();
    testVariable = Variable.Builder.newVariable("test-variable", IntegerType.get(), "test").addCategories("1", "2")
        .addCategory("88", "88", true).build();
  }

  @After
  public void stopYourEngine() {
    MagmaEngine.get().shutdown();
  }

  @Test
  public void test_compute_handlesNullVectorSource() {
    VariableValueSource mockSource = createMock(VariableValueSource.class);
    expect(mockSource.asVectorSource()).andReturn(null);

    replay(mockSource);

    ExcludeMissingDescriptiveStatisticsProvider provider = new ExcludeMissingDescriptiveStatisticsProvider();
    DescriptiveStatistics ds = provider.compute(mockSource, emptySet);

    assertThat(ds, notNullValue());
    verify(mockSource);
  }

  @Test
  public void test_compute_obtainsVectorOverCorrectSetOfEntities() {
    VariableValueSource mockSource = createMock(VariableValueSource.class);
    VectorSource mockVector = createMock(VectorSource.class);

    expect(mockSource.asVectorSource()).andReturn(mockVector);
    expect(mockVector.getValues(emptySet)).andReturn(ImmutableList.<Value>of());

    replay(mockSource, mockVector);

    ExcludeMissingDescriptiveStatisticsProvider provider = new ExcludeMissingDescriptiveStatisticsProvider();
    DescriptiveStatistics ds = provider.compute(mockSource, emptySet);

    assertThat(ds, notNullValue());

    verify(mockSource, mockVector);
  }

  @Test
  public void test_compute_excludesNullValues() {
    VariableValueSource mockSource = createMock(VariableValueSource.class);
    VectorSource mockVector = createMock(VectorSource.class);

    expect(mockSource.asVectorSource()).andReturn(mockVector);
    expect(mockSource.getVariable()).andReturn(testVariable).anyTimes();

    expect(mockVector.getValues(emptySet))
        .andReturn(Values.asValues(IntegerType.get(), 1, 2, 2, 88, null, null, 88, 2));

    replay(mockSource, mockVector);

    ExcludeMissingDescriptiveStatisticsProvider provider = new ExcludeMissingDescriptiveStatisticsProvider();
    DescriptiveStatistics ds = provider.compute(mockSource, emptySet);

    assertThat(ds, notNullValue());
    assertThat(ds.getN(), is(4l));

    verify(mockSource, mockVector);
  }

}
