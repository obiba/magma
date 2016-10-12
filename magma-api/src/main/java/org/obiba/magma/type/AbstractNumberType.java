/*
 * Copyright (c) 2016 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.magma.type;

import org.obiba.magma.Value;
import org.obiba.magma.support.ValueComparator;

public abstract class AbstractNumberType extends AbstractValueType {

  private static final long serialVersionUID = -5271259966499174607L;

  protected AbstractNumberType() {

  }

  @Override
  public boolean isDateTime() {
    return false;
  }

  @Override
  public boolean isNumeric() {
    return true;
  }

  @Override
  public boolean isGeo() {
    return false;
  }

  @Override
  public int compare(Value o1, Value o2) {
    return ValueComparator.INSTANCE.compare(o1, o2);
  }

}
