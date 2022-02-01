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

import com.thoughtworks.xstream.security.AnyTypePermission;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.obiba.magma.MagmaEngine;
import org.obiba.magma.Value;
import org.obiba.magma.ValueType;
import org.obiba.magma.type.TextType;

import com.google.common.collect.ImmutableList;
import com.thoughtworks.xstream.XStream;

import static org.fest.assertions.api.Assertions.assertThat;

public class ValueSequenceConverterTest {

  private XStream xstream;

  @Before
  public void startYourEngine() {
    new MagmaEngine();
    xstream = new XStream();
    xstream.addPermission(AnyTypePermission.ANY);
    xstream.registerConverter(new ValueConverter());
    xstream.registerConverter(new ValueSequenceConverter());
  }

  @After
  public void stopYourEngine() {
    MagmaEngine.get().shutdown();
  }

  @Test
  public void testValueSequenceWithNotNullValues() {
    ValueType type = TextType.get();
    Value value = type.sequenceOf(ImmutableList.of(type.valueOf("First Value"), type.valueOf("Second Value")));

    String xml = xstream.toXML(value);
    Value unmarshalled = (Value) xstream.fromXML(xml);

    assertThat(value).isEqualTo(unmarshalled);
    assertThat(unmarshalled.isSequence()).isTrue();
    assertThat(unmarshalled.asSequence().getValues()).hasSize(2);
  }

  @Test
  public void testNullValueSequence() {
    Value value = TextType.get().nullSequence();

    String xml = xstream.toXML(value);
    Value unmarshalled = (Value) xstream.fromXML(xml);

    assertThat(value).isEqualTo(unmarshalled);
    assertThat(unmarshalled.isNull()).isTrue();
  }

}
