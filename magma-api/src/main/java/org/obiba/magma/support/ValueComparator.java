package org.obiba.magma.support;

import java.util.Comparator;

import org.obiba.magma.MagmaDate;
import org.obiba.magma.Value;

@SuppressWarnings("Singleton")
public class ValueComparator implements Comparator<Value> {

  public static final ValueComparator INSTANCE = new ValueComparator();

  private ValueComparator() {
  }

  @Override
  @SuppressWarnings({ "rawtypes", "unchecked" })
  public int compare(Value o1, Value o2) {
    if(o1.isNull() && o2.isNull()) return 0;
    if(o1.isNull()) return -1;
    if(o2.isNull()) return 1;
    Comparable l1 = (Comparable) normalizeValue(o1.getValue());
    Comparable l2 = (Comparable) normalizeValue(o2.getValue());
    return l1 == l2 ? 0 : l1.compareTo(l2);
  }

  private Object normalizeValue(Object o) {
    if (o instanceof MagmaDate) {
      return ((MagmaDate)o).asDate();
    }
    return o;
  }

}
