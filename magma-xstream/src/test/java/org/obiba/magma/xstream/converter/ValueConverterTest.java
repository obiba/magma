/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.magma.xstream.converter;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.obiba.magma.MagmaEngine;
import org.obiba.magma.Value;
import org.obiba.magma.type.TextType;

import com.thoughtworks.xstream.XStream;

import static org.fest.assertions.api.Assertions.assertThat;

public class ValueConverterTest {

  XStream xstream;

  @Before
  public void startYourEngine() {
    new MagmaEngine();
    xstream = new XStream();
    xstream.registerConverter(new ValueConverter());
  }

  @After
  public void stopYourEngine() {
    MagmaEngine.get().shutdown();
  }

  @Test
  public void testNotNullValue() {
    Value value = TextType.get().valueOf("The Value");
    String xml = xstream.toXML(value);
    Value unmarshalled = (Value) xstream.fromXML(xml);
    assertThat(value).isEqualTo(unmarshalled);
  }

  @Test
  public void testNullValue() {
    Value value = TextType.get().nullValue();

    String xml = xstream.toXML(value);
    Value unmarshalled = (Value) xstream.fromXML(xml);

    assertThat(value).isEqualTo(unmarshalled);
    assertThat(unmarshalled.isNull()).isTrue();
  }

}
