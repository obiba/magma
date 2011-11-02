/*******************************************************************************
 * Copyright (c) 2011 OBiBa. All rights reserved.
 *  
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *  
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.magma.type;

import org.junit.Assert;
import org.junit.Test;
import org.obiba.magma.Value;
import org.obiba.magma.ValueConverter;
import org.obiba.magma.ValueType;
import org.obiba.magma.test.AbstractMagmaTest;

public class IdentityValueConverterTest extends AbstractMagmaTest {

  @Test
  public void test_converterFor_returnsAConverter() {
    ValueConverter converter = ValueType.Factory.conveterFor(DecimalType.get(), DecimalType.get());
    Assert.assertNotNull(converter);
  }

  @Test
  public void test_converterFor_returnsAnIdentityConverter() {
    ValueConverter converter = ValueType.Factory.conveterFor(DecimalType.get(), DecimalType.get());
    Assert.assertNotNull(converter);
    Value value = DecimalType.get().valueOf(1);
    Value converted = converter.convert(value, DecimalType.get());
    // Should be the same instance
    Assert.assertTrue(value == converted);
  }

  @Test
  public void test_convert_fromValueType() {
    Value value = DecimalType.get().valueOf(1);
    Value converted = DecimalType.get().convert(value);
    Assert.assertTrue(value == converted);
  }
}
