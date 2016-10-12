/*
 * Copyright (c) 2016 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.magma.xstream;

import org.junit.Before;
import org.junit.Test;
import org.obiba.magma.Value;
import org.obiba.magma.Variable;
import org.obiba.magma.support.VariableEntityBean;
import org.obiba.magma.type.TextType;

import com.thoughtworks.xstream.XStream;

import static org.fest.assertions.api.Assertions.assertThat;

public class XStreamValueSetTest extends AbstractMagmaXStreamTest {

  private Variable testVariable;

  @Before
  @Override
  public void before() {
    super.before();
    testVariable = Variable.Builder.newVariable("TestVar", TextType.get(), "TestType").build();
  }

  @Test
  public void testEmptyValueSet() {
    XStreamValueSet valueSet = new XStreamValueSet("testTable", new VariableEntityBean("TestType", "id"));
    assertThat(valueSet.getValue(testVariable).isNull()).isTrue();
  }

  @Test
  public void testGetValueReturnsAddedValue() {
    XStreamValueSet valueSet = new XStreamValueSet("testTable", new VariableEntityBean("TestType", "id"));

    Value testValue = TextType.get().valueOf("TestValue");
    valueSet.setValue(testVariable, testValue);

    Value value = valueSet.getValue(testVariable);
    assertThat(testValue).isEqualTo(value);
  }

  @Test
  public void testUnmarshall() {
    XStreamValueSet valueSet = new XStreamValueSet("testTable", new VariableEntityBean("TestType", "id"));

    Value testValue = TextType.get().valueOf("TestValue");
    valueSet.setValue(testVariable, testValue);

    XStream xstream = getDefaultXStream();

    String xml = xstream.toXML(valueSet);
    XStreamValueSet unmarshalled = (XStreamValueSet) xstream.fromXML(xml);

    Value value = unmarshalled.getValue(testVariable);
    assertThat(testValue).isEqualTo(value);
  }
}
