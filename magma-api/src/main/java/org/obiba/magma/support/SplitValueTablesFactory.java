package org.obiba.magma.support;

import com.google.common.collect.Lists;
import org.obiba.magma.ValueTable;
import org.obiba.magma.VariableEntity;

import java.util.List;

/**
 * Split a value table by entities, in order to have a maximum of data points per table. Then if there are a lot variables,
 * the number of entities will be smaller.
 */
public class SplitValueTablesFactory {

  private static final int MAX_DATA_POINTS = 100000;

  public static List<ValueTable> split(ValueTable valueTable) {
    return split(valueTable, MAX_DATA_POINTS);
  }

  public static List<ValueTable> split(ValueTable valueTable, int maxDataPoints) {
    List<ValueTable> splitValueTables = Lists.newArrayList();
    int totalRowCount = valueTable.getValueSetCount();
    int varCount = valueTable.getVariableCount();

    // if bigger than max data points, the table to assign will be split in smaller pieces
    // by rows
    int chunks = (totalRowCount * varCount) / maxDataPoints;
    if (chunks > 0) {
      int maxRowCount = maxDataPoints / varCount;
      List<VariableEntity> entities = Lists.newArrayList(valueTable.getVariableEntities());

      int from = 0;
      int to = maxRowCount;
      while (from < totalRowCount) {
        splitValueTables.add(new SplitValueTable(valueTable, entities.subList(from, to)));
        from = to;
        to = to + maxRowCount;
        if (to > totalRowCount)
          to = totalRowCount - 1;
      }
    } else {
      splitValueTables.add(valueTable);
    }
    return splitValueTables;
  }

}
