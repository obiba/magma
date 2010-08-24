package org.obiba.magma.math;

import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;
import org.obiba.magma.ValueTable;
import org.obiba.magma.Variable;

public interface StatsComputingValueTable extends ValueTable {

  public DescriptiveStatistics getStats(Variable variable);

}
