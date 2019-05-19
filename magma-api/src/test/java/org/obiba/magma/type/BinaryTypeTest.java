/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.magma.type;

import org.junit.Ignore;
import org.obiba.magma.ValueType;

import com.google.common.collect.ImmutableList;

public class BinaryTypeTest extends BaseValueTypeTest {

  @Override
  ValueType getValueType() {
    return BinaryType.get();
  }

  @Override
  Object getObjectForType() {
    return new byte[] { 1, 2, 3, 4 };
  }

  @Override
  boolean isDateTime() {
    return false;
  }

  @Override
  boolean isNumeric() {
    return false;
  }

  @Override
  Iterable<Class<?>> validClasses() {
    return ImmutableList.<Class<?>>of(byte[].class);
  }

  @Override
  @Ignore("equals() on arrays does not compare array contents. " +
      "We need to override the value.equals() method for BinaryType.")
  public void testValueOfToStringIsEqual() {
  }

  @Override
  @Ignore("equals() on arrays does not compare array contents. " +
      "We need to override the value.equals() method for BinaryType.")
  public void testValueOfToStringSequence() {
  }
}
