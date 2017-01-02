/*
 * Copyright (c) 2017 OBiBa. All rights reserved.
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
import org.junit.Before;
import org.junit.Test;
import org.obiba.magma.Value;
import org.obiba.magma.Variable;
import org.obiba.magma.VariableEntity;
import org.obiba.magma.VariableValueSource;
import org.obiba.magma.VectorSource;
import org.obiba.magma.support.Values;
import org.obiba.magma.test.AbstractMagmaTest;
import org.obiba.magma.type.IntegerType;

import com.google.common.collect.ImmutableList;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ExcludeMissingDescriptiveStatisticsProviderTest extends AbstractMagmaTest {

  private final SortedSet<VariableEntity> emptySet = new TreeSet<>();

  private Variable testVariable;

  @Before
  @Override
  public void before() {
    super.before();
    testVariable = Variable.Builder.newVariable("test-variable", IntegerType.get(), "test").addCategories("1", "2")
        .addCategory("88", "88", true).build();
  }

  @Test
  public void test_compute_handlesNullVectorSource() {
    VariableValueSource mockSource = mock(VariableValueSource.class);
    when(mockSource.supportVectorSource()).thenReturn(false);

    DescriptiveStatisticsProvider provider = new ExcludeMissingDescriptiveStatisticsProvider();
    DescriptiveStatistics ds = provider.compute(mockSource, emptySet);

    assertThat(ds).isNotNull();
  }

  @Test
  public void test_compute_obtainsVectorOverCorrectSetOfEntities() {
    VariableValueSource mockSource = mock(VariableValueSource.class);
    VectorSource mockVector = mock(VectorSource.class);

    when(mockSource.asVectorSource()).thenReturn(mockVector);
    when(mockVector.getValues(emptySet)).thenReturn(ImmutableList.<Value>of());

    DescriptiveStatisticsProvider provider = new ExcludeMissingDescriptiveStatisticsProvider();
    DescriptiveStatistics ds = provider.compute(mockSource, emptySet);

    assertThat(ds).isNotNull();

  }

  @Test
  public void test_compute_excludesNullValues() {
    VariableValueSource mockSource = mock(VariableValueSource.class);
    VectorSource mockVector = mock(VectorSource.class);

    when(mockSource.supportVectorSource()).thenReturn(true);
    when(mockSource.asVectorSource()).thenReturn(mockVector);
    when(mockSource.getVariable()).thenReturn(testVariable);

    when(mockVector.getValues(emptySet)).thenReturn(Values.asValues(IntegerType.get(), 1, 2, 2, 88, null, null, 88, 2));

    DescriptiveStatisticsProvider provider = new ExcludeMissingDescriptiveStatisticsProvider();
    DescriptiveStatistics ds = provider.compute(mockSource, emptySet);

    assertThat(ds).isNotNull();

    assertThat(ds.getN()).isEqualTo(4l);

  }

}
