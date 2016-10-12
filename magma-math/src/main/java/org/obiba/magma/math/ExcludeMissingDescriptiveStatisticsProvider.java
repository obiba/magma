/*
 * Copyright (c) 2016 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.magma.math;

import java.util.Objects;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.obiba.magma.Category;
import org.obiba.magma.Value;
import org.obiba.magma.Variable;
import org.obiba.magma.VariableValueSource;

/**
 * An implementation of {@code DescriptiveStatisticsProvider} that will exclude all null Values and all values that are
 * equal to the name of a {@code missing} category from the statistical summary.
 */
public class ExcludeMissingDescriptiveStatisticsProvider extends AbstractDescriptiveStatisticsProvider {

  @Override
  protected void processValue(VariableValueSource valueSource, Value value, DescriptiveStatistics stats) {
    if(!isMissing(valueSource.getVariable(), value)) {
      stats.addValue(((Number) value.getValue()).doubleValue());
    }
  }

  /**
   * Returns true when {@code value} is considered {@code missing} for {@code variable}. More formally, this method
   * returns true when {@code value#isNull()} is true or when {@code value#toString()} is equal to the name of any
   * missing category ({@code Category#isMissing()} is {@code true}).
   *
   * @param variable
   * @param value
   * @return
   */
  protected boolean isMissing(Variable variable, Value value) {
    if(value.isNull()) {
      return true;
    }
    for(Category category : variable.getCategories()) {
      if(category.isMissing() && Objects.equals(value.toString(), category.getName())) {
        return true;
      }
    }
    return false;
  }

}
