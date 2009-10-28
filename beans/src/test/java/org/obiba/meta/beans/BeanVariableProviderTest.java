/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.meta.beans;

import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.Set;

import org.easymock.EasyMock;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.obiba.meta.MetaEngine;
import org.obiba.meta.Value;
import org.obiba.meta.ValueSetReference;
import org.obiba.meta.ValueSetReferenceResolver;
import org.obiba.meta.VariableValueSource;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;

/**
 *
 */
public class BeanVariableProviderTest {

  ValueSetReferenceResolver referenceResolverMock = EasyMock.createMock(ValueSetReferenceResolver.class);

  @Before
  public void createMetaEngine() {
    new MetaEngine();
    EasyMock.reset(referenceResolverMock);
  }

  @After
  public void shutdownMetaEngine() {
    MetaEngine.get().shutdown();
  }

  @Test
  public void testSimpleProperties() {
    Set<String> properties = Sets.newHashSet("firstName", "lastName", "integer", "enumProperty");
    BeanVariableValueSourceFactory bvp = new BeanVariableValueSourceFactory(TestBean.class);
    bvp.setProperties(properties);

    assertVariablesFromProperties(bvp, properties);
  }

  @Test
  public void testPropertyWithoutField() {
    Set<String> properties = Sets.newHashSet("composedProperty");
    BeanVariableValueSourceFactory bvp = new BeanVariableValueSourceFactory(TestBean.class);
    bvp.setProperties(properties);

    assertVariablesFromProperties(bvp, properties);
  }

  @Test
  public void testNestedProperties() {
    Set<String> properties = Sets.newHashSet("nestedBean.decimal", "nestedBean.data");
    BeanVariableValueSourceFactory bvp = new BeanVariableValueSourceFactory(TestBean.class);
    bvp.setProperties(properties);
    assertVariablesFromProperties(bvp, properties);
  }

  @Test
  public void testOverridenVariableName() {
    Set<String> properties = Sets.newHashSet("nestedBean.decimal", "firstName");
    Map<String, String> nameOverride = new ImmutableMap.Builder<String, String>().put("nestedBean.decimal", "NestedDecimal").put("firstName", "FirstName").build();

    BeanVariableValueSourceFactory bvp = new BeanVariableValueSourceFactory(TestBean.class);
    bvp.setProperties(properties);
    bvp.setPropertyNameToVariableName(nameOverride);
    assertVariablesFromProperties(bvp, properties, nameOverride);
  }

  @Test
  public void testValues() {
    Set<String> properties = Sets.newHashSet("firstName", "nestedBean.decimal", "nestedBean.data");
    BeanVariableValueSourceFactory bvp = new BeanVariableValueSourceFactory(TestBean.class);
    bvp.setProperties(properties);

    Set<VariableValueSource> variableValueSources = assertVariablesFromProperties(bvp, properties);

    TestBean tb = new TestBean();
    tb.setFirstName("TestBean");
    NestedTestBean nb = new NestedTestBean();
    nb.setDecimal(42.0);
    nb.setData(new byte[] { 0x01, 0x02 });
    tb.setNestedBean(nb);

    EasyMock.expect(referenceResolverMock.canResolve((ValueSetReference) EasyMock.anyObject())).andReturn(true).anyTimes();
    EasyMock.expect(referenceResolverMock.resolve((ValueSetReference) EasyMock.anyObject())).andReturn(tb).anyTimes();
    EasyMock.replay(referenceResolverMock);
    for(VariableValueSource source : variableValueSources) {
      Value value = source.getValue(null);
      Assert.assertNotNull("Value cannot be null " + source.getVariable().getName(), value);
      Assert.assertNotNull("ValueType cannot be null " + source.getVariable().getName(), value.getValueType());
      Assert.assertNotNull("Value's value cannot be null " + source.getVariable().getName(), value.getValue());
    }
    EasyMock.verify(referenceResolverMock);
  }

  @Test
  public void testNullValueInPropertyPath() {
    Set<String> properties = Sets.newHashSet("anotherNestedBean.data");
    BeanVariableValueSourceFactory bvp = new BeanVariableValueSourceFactory(TestBean.class);
    bvp.setProperties(properties);

    Set<VariableValueSource> variableValueSources = assertVariablesFromProperties(bvp, properties);

    EasyMock.expect(referenceResolverMock.canResolve((ValueSetReference) EasyMock.anyObject())).andReturn(true).anyTimes();
    EasyMock.expect(referenceResolverMock.resolve((ValueSetReference) EasyMock.anyObject())).andReturn(new TestBean()).anyTimes();
    EasyMock.replay(referenceResolverMock);
    for(VariableValueSource source : variableValueSources) {
      Value value = source.getValue(null);
      Assert.assertNotNull("Value cannot be null " + source.getVariable().getName(), value);
      Assert.assertNotNull("ValueType cannot be null " + source.getVariable().getName(), value.getValueType());

      // The value's value should be null
      Assert.assertNull("Value's value should be null " + source.getVariable().getName(), value.getValue());
    }
    EasyMock.verify(referenceResolverMock);
  }

  protected Set<VariableValueSource> assertVariablesFromProperties(BeanVariableValueSourceFactory bvp, Set<String> properties) {
    return assertVariablesFromProperties(bvp, properties, null);
  }

  protected Set<VariableValueSource> assertVariablesFromProperties(BeanVariableValueSourceFactory bvp, Set<String> properties, Map<String, String> nameOverride) {
    Set<VariableValueSource> variables = bvp.createSources(referenceResolverMock);
    // There are no more and no less than what was specified
    Assert.assertEquals(properties.size(), variables.size());
    Collection<String> nameSet = nameOverride != null ? nameOverride.values() : properties;
    for(VariableValueSource variableValueSource : variables) {
      String variableName = variableValueSource.getVariable().getName();
      Assert.assertTrue("Unexpected variable name " + variableName, nameSet.contains(variableName));
    }
    return variables;
  }

  public static class TestBean {

    private String firstName;

    private String lastName;

    private TestEnum enumProperty;

    private boolean state;

    private Date date;

    private Integer integer;

    private NestedTestBean nestedBean;

    private NestedTestBean anotherNestedBean;

    public TestBean() {
    }

    public final String getFirstName() {
      return firstName;
    }

    public final void setFirstName(String firstName) {
      this.firstName = firstName;
    }

    public final String getLastName() {
      return lastName;
    }

    public final void setLastName(String lastName) {
      this.lastName = lastName;
    }

    // A property that has no underlying class field
    public final String getComposedProperty() {
      return firstName + lastName;
    }

    public final TestEnum getEnumProperty() {
      return enumProperty;
    }

    public final void setEnumProperty(TestEnum enumProperty) {
      this.enumProperty = enumProperty;
    }

    public final boolean isState() {
      return state;
    }

    public final void setState(boolean state) {
      this.state = state;
    }

    public final Date getDate() {
      return date;
    }

    public final void setDate(Date date) {
      this.date = date;
    }

    public final NestedTestBean getNestedBean() {
      return nestedBean;
    }

    public final void setNestedBean(NestedTestBean nestedBean) {
      this.nestedBean = nestedBean;
    }

    public final Integer getInteger() {
      return integer;
    }

    public final void setInteger(Integer integer) {
      this.integer = integer;
    }

    public NestedTestBean getAnotherNestedBean() {
      return anotherNestedBean;
    }

    public void setAnotherNestedBean(NestedTestBean anotherNestedBean) {
      this.anotherNestedBean = anotherNestedBean;
    }

  }

  public static class NestedTestBean {
    private Double decimal;

    private byte[] data;

    public final Double getDecimal() {
      return decimal;
    }

    public final void setDecimal(Double decimal) {
      this.decimal = decimal;
    }

    public final byte[] getData() {
      return data;
    }

    public final void setData(byte[] data) {
      this.data = data;
    }

  }

  public static enum TestEnum {
    VALUE1, VALUE2
  }
}
