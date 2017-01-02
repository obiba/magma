/*
 * Copyright (c) 2017 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.magma.type;

import java.util.Locale;

import org.obiba.magma.ValueType;

import com.google.common.collect.ImmutableList;

public class LocaleTypeTest extends BaseValueTypeTest {

  @Override
  ValueType getValueType() {
    return LocaleType.get();
  }

  @Override
  Object getObjectForType() {
    return Locale.CANADA_FRENCH;
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
    return ImmutableList.<Class<?>>of(Locale.class);
  }

}
