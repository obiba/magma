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
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;
import org.obiba.meta.IValueSetReference;
import org.obiba.meta.IVariable;
import org.obiba.meta.IVariableData;
import org.obiba.meta.IVariableEntity;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;

/**
 *
 */
public class BeanVariableProviderTest {

  @Test
  public void testSimpleProperties() {
    Set<String> properties = Sets.newHashSet("firstName", "lastName", "integer", "enumProperty");
    BeanVariableProvider bvp = new BeanVariableProvider(getResolver());
    bvp.setProperties(properties);

    assertVariablesFromProperties(bvp, properties);
  }

  @Test
  public void testPropertyWithoutField() {
    Set<String> properties = Sets.newHashSet("composedProperty");
    BeanVariableProvider bvp = new BeanVariableProvider(getResolver());
    bvp.setProperties(properties);

    assertVariablesFromProperties(bvp, properties);
  }

  @Test
  public void testNestedProperties() {
    Set<String> properties = Sets.newHashSet("nestedBean.decimal", "nestedBean.data");
    BeanVariableProvider bvp = new BeanVariableProvider(getResolver());
    bvp.setProperties(properties);
    assertVariablesFromProperties(bvp, properties);
  }

  @Test
  public void testOverridenVariableName() {
    Set<String> properties = Sets.newHashSet("nestedBean.decimal", "firstName");
    Map<String, String> nameOverride = new ImmutableMap.Builder<String, String>().put("nestedBean.decimal", "NestedDecimal").put("firstName", "FirstName").build();

    BeanVariableProvider bvp = new BeanVariableProvider(getResolver());
    bvp.setProperties(properties);
    bvp.setPropertyNameToVariableName(nameOverride);
    assertVariablesFromProperties(bvp, properties, nameOverride);
  }

  @Test
  public void testAdaptedBean() {
    Set<String> properties = Sets.newHashSet("decimal", "data");
    BeanVariableProvider bvp = new BeanVariableProvider(NestedTestBean.class);
    bvp.setProperties(properties);
    List<IVariable> variables = assertVariablesFromProperties(bvp, properties);
    NestedTestBean nb = new NestedTestBean();
    nb.setDecimal(42.0);
    nb.setData(new byte[] { 0x01, 0x02 });

    VariableEntityBeanAdaptor<NestedTestBean> adaptor = new VariableEntityBeanAdaptor<NestedTestBean>(nb) {

      @Override
      public String getType() {
        return "MyType";
      }

      @Override
      public String getIdentifier() {
        return getAdaptedBean().getDecimal().toString();
      }
    };
    for(IVariable variable : variables) {
      IVariableData value = bvp.getData(variable, adaptor);
      Assert.assertNotNull(value);
      Assert.assertNotNull(value.getDataType());
      Assert.assertNotNull(value.getData());
    }
  }

  protected IValueSetReferenceBeanResolver getResolver() {
    return new IValueSetReferenceBeanResolver() {

      @Override
      public Object resolveReference(IValueSetReference reference) {
        return new TestBean("1234");
      }

      @Override
      public Class<?> getResolvedBeanClass() {
        return TestBean.class;
      }
    };
  }

  protected List<IVariable> assertVariablesFromProperties(BeanVariableProvider bvp, Set<String> properties) {
    return assertVariablesFromProperties(bvp, properties, null);
  }

  protected List<IVariable> assertVariablesFromProperties(BeanVariableProvider bvp, Set<String> properties, Map<String, String> nameOverride) {
    List<IVariable> variables = bvp.getVariables();
    // There are no more and no less than what was specified
    Assert.assertEquals(properties.size(), variables.size());
    Collection<String> nameSet = nameOverride != null ? nameOverride.values() : properties;
    for(IVariable variable : variables) {
      String variableName = variable.getName();
      Assert.assertTrue("Unexpected variable name " + variable.getName(), nameSet.contains(variableName));
    }
    return variables;
  }

  public static class TestBean implements IVariableEntity {

    private String id;

    private String firstName;

    private String lastName;

    private TestEnum enumProperty;

    private boolean state;

    private Date date;

    private Integer integer;

    private NestedTestBean nestedBean;

    public TestBean(String id) {
      this.id = id;
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

    public String getIdentifier() {
      return id;
    }

    public String getType() {
      return "TestEntity";
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
