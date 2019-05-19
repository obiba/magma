/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

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
