/*
 * Copyright (c) 2016 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.magma.beans;

import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.easymock.EasyMock;
import org.junit.Test;
import org.obiba.magma.Category;
import org.obiba.magma.Value;
import org.obiba.magma.ValueSet;
import org.obiba.magma.Variable;
import org.obiba.magma.VariableValueSource;
import org.obiba.magma.VariableValueSourceFactory;
import org.obiba.magma.test.AbstractMagmaTest;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;

import static org.fest.assertions.api.Assertions.assertThat;

/**
 *
 */
public class BeanVariableProviderTest extends AbstractMagmaTest {

  @Test
  public void testSimpleProperties() {
    Set<String> properties = Sets
        .newHashSet("firstName", "lastName", "integer", "decimal", "enumProperty", "language", "state");
    BeanVariableValueSourceFactory<TestBean> bvp = new BeanVariableValueSourceFactory<>("Test", TestBean.class);
    bvp.setProperties(properties);

    assertVariablesFromProperties(bvp, properties);
  }

  @Test
  public void test_correctValueTypesAreInferred() {
    Set<String> properties = Sets.newLinkedHashSet(
        ImmutableList.of("firstName", "lastName", "integer", "decimal", "enumProperty", "language", "state", "date"));
    List<String> types = ImmutableList
        .of("text", "text", "integer", "decimal", "text", "locale", "boolean", "datetime");
    BeanVariableValueSourceFactory<TestBean> bvp = new BeanVariableValueSourceFactory<>("Test", TestBean.class);
    bvp.setProperties(properties);
    Iterator<String> i = types.iterator();
    for(VariableValueSource source : bvp.createSources()) {
      assertThat(i.next()).isEqualTo(source.getValueType().getName())
          .overridingErrorMessage("wrong type for property " + source.getVariable().getName());
    }
  }

  @Test
  public void testEnumHasCategories() {
    Set<String> properties = Sets.newHashSet("enumProperty");
    BeanVariableValueSourceFactory<TestBean> bvp = new BeanVariableValueSourceFactory<>("Test", TestBean.class);
    bvp.setProperties(properties);

    Set<VariableValueSource> sources = assertVariablesFromProperties(bvp, properties);
    Set<Category> categories = Iterables.get(sources, 0).getVariable().getCategories();
    assertThat(categories).isNotNull();
    assertThat(categories).isNotEmpty();
    for(Category c : categories) {
      TestEnum e = TestEnum.valueOf(c.getName());
      assertThat(e).isNotNull();
    }
  }

  @Test
  public void testPropertyWithoutField() {
    Set<String> properties = Sets.newHashSet("composedProperty");
    BeanVariableValueSourceFactory<TestBean> bvp = new BeanVariableValueSourceFactory<>("Test", TestBean.class);
    bvp.setProperties(properties);

    assertVariablesFromProperties(bvp, properties);
  }

  @Test
  public void testNestedProperties() {
    Set<String> properties = Sets.newHashSet("nestedBean.decimal", "nestedBean.data");
    BeanVariableValueSourceFactory<TestBean> bvp = new BeanVariableValueSourceFactory<>("Test", TestBean.class);
    bvp.setProperties(properties);
    assertVariablesFromProperties(bvp, properties);
  }

  @Test
  public void testOverriddenVariableName() {
    Set<String> properties = Sets.newHashSet("nestedBean.decimal", "firstName");
    Map<String, String> nameOverride = new ImmutableMap.Builder<String, String>()
        .put("nestedBean.decimal", "NestedDecimal").put("firstName", "FirstName").build();

    BeanVariableValueSourceFactory<TestBean> bvp = new BeanVariableValueSourceFactory<>("Test", TestBean.class);
    bvp.setProperties(properties);
    bvp.setPropertyNameToVariableName(nameOverride);
    assertVariablesFromProperties(bvp, properties, nameOverride);
  }

  @Test
  public void testMapAttributes() {
    Set<String> properties = Sets.newHashSet("attributes[phoneNumber].data");
    Map<String, Class<?>> mappedPropertyType = new ImmutableMap.Builder<String, Class<?>>()
        .put("attributes", NestedTestBean.class).build();

    BeanVariableValueSourceFactory<TestBean> bvp = new BeanVariableValueSourceFactory<>("Test", TestBean.class);
    bvp.setProperties(properties);
    bvp.setMappedPropertyType(mappedPropertyType);
    assertVariablesFromProperties(bvp, properties);
  }

  @Test
  @SuppressWarnings({ "PMD.NcssMethodCount", "OverlyLongMethod" })
  public void testMapAttributesWithDotsInKeyValues() {
    TestBean tb = new TestBean();
    tb.setFirstName("TestBean");
    NestedTestBean nb = new NestedTestBean();
    nb.setDecimal(42.0);
    nb.setData(new byte[] { 0x01, 0x02 });
    tb.setAttributes(ImmutableMap.of("phone.number", nb));

    Set<String> properties = Sets.newHashSet("attributes[phone.number].data");
    Map<String, Class<?>> mappedPropertyType = new ImmutableMap.Builder<String, Class<?>>()
        .put("attributes", NestedTestBean.class).build();
    BeanVariableValueSourceFactory<TestBean> bvp = new BeanVariableValueSourceFactory<>("Test", TestBean.class);
    bvp.setProperties(properties);
    bvp.setMappedPropertyType(mappedPropertyType);

    Set<VariableValueSource> variableValueSources = assertVariablesFromProperties(bvp, properties);

    BeanValueSet bvs = EasyMock.createMock(BeanValueSet.class);
    // "If you use an argument matcher for one argument, you must use an argument matcher for all the arguments."
    EasyMock.expect(
        bvs.resolve((Class<?>) EasyMock.anyObject(), (ValueSet) EasyMock.anyObject(), (Variable) EasyMock.anyObject()))
        .andReturn(tb).anyTimes();
    EasyMock.replay(bvs);

    for(VariableValueSource source : variableValueSources) {
      Value value = source.getValue(bvs);
      String name = source.getVariable().getName();
      assertThat(value).isNotNull().overridingErrorMessage("Value cannot be null " + name);
      assertThat(value.getValueType()).isNotNull().overridingErrorMessage("ValueType cannot be null " + name);
      assertThat(value.getValue()).isNotNull().overridingErrorMessage("Value's value cannot be null " + name);
    }
  }

  @Test
  @SuppressWarnings({ "PMD.NcssMethodCount", "OverlyLongMethod" })
  public void testValues() {
    TestBean tb = new TestBean();
    tb.setFirstName("TestBean");
    NestedTestBean nb = new NestedTestBean();
    nb.setDecimal(42.0);
    nb.setData(new byte[] { 0x01, 0x02 });
    tb.setNestedBean(nb);

    Set<String> properties = Sets.newHashSet("firstName", "nestedBean.decimal", "nestedBean.data");
    BeanVariableValueSourceFactory<TestBean> bvp = new BeanVariableValueSourceFactory<>("Test", TestBean.class);
    bvp.setProperties(properties);

    Set<VariableValueSource> variableValueSources = assertVariablesFromProperties(bvp, properties);

    BeanValueSet bvs = EasyMock.createMock(BeanValueSet.class);
    // "If you use an argument matcher for one argument, you must use an argument matcher for all the arguments."
    EasyMock.expect(
        bvs.resolve((Class<?>) EasyMock.anyObject(), (ValueSet) EasyMock.anyObject(), (Variable) EasyMock.anyObject()))
        .andReturn(tb).anyTimes();
    EasyMock.replay(bvs);

    for(VariableValueSource source : variableValueSources) {
      Value value = source.getValue(bvs);
      String name = source.getVariable().getName();
      assertThat(value).isNotNull().overridingErrorMessage("Value cannot be null " + name);
      assertThat(value.getValueType()).isNotNull().overridingErrorMessage("ValueType cannot be null " + name);
      assertThat(value.getValue()).isNotNull().overridingErrorMessage("Value's value cannot be null " + name);
    }
  }

  @Test
  public void testNullValueInPropertyPath() {

    Set<String> properties = Sets.newHashSet("anotherNestedBean.data");
    BeanVariableValueSourceFactory<TestBean> bvp = new BeanVariableValueSourceFactory<>("Test", TestBean.class);
    bvp.setProperties(properties);

    Set<VariableValueSource> variableValueSources = assertVariablesFromProperties(bvp, properties);

    BeanValueSet bvs = EasyMock.createMock(BeanValueSet.class);
    // "If you use an argument matcher for one argument, you must use an argument matcher for all the arguments."
    EasyMock.expect(
        bvs.resolve((Class<?>) EasyMock.anyObject(), (ValueSet) EasyMock.anyObject(), (Variable) EasyMock.anyObject()))
        .andReturn(new TestBean()).anyTimes();
    EasyMock.replay(bvs);

    for(VariableValueSource source : variableValueSources) {
      Value value = source.getValue(bvs);
      String name = source.getVariable().getName();
      assertThat(value).isNotNull().overridingErrorMessage("Value cannot be null " + name);
      assertThat(value.getValueType()).isNotNull().overridingErrorMessage("ValueType cannot be null " + name);
      assertThat(value.isNull()).isTrue().overridingErrorMessage("Value's value should be null " + name);
    }
  }

  protected Set<VariableValueSource> assertVariablesFromProperties(VariableValueSourceFactory bvp,
      Collection<String> properties) {
    return assertVariablesFromProperties(bvp, properties, null);
  }

  protected Set<VariableValueSource> assertVariablesFromProperties(VariableValueSourceFactory bvp,
      Collection<String> properties, Map<String, String> nameOverride) {
    Set<VariableValueSource> variables = bvp.createSources();
    // There are no more and no less than what was specified
    assertThat(properties.size()).isEqualTo(variables.size());
    Collection<String> nameSet = nameOverride != null ? nameOverride.values() : properties;
    for(VariableValueSource variableValueSource : variables) {
      String variableName = variableValueSource.getVariable().getName();
      assertThat(nameSet).contains(variableName).overridingErrorMessage("Unexpected variable name " + variableName);
    }
    return variables;
  }

  @SuppressWarnings({ "UnusedDeclaration", "AssignmentToDateFieldFromParameter" })
  public static class TestBean {

    private String firstName;

    private String lastName;

    private TestEnum enumProperty;

    private boolean state;

    private Date date;

    private Integer integer;

    private Double decimal;

    private NestedTestBean nestedBean;

    private NestedTestBean anotherNestedBean;

    private Locale language;

    private Map<String, NestedTestBean> attributes;

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

    @edu.umd.cs.findbugs.annotations.SuppressWarnings(value = "EI_EXPOSE_REP")
    public final Date getDate() {
      return date;
    }

    @edu.umd.cs.findbugs.annotations.SuppressWarnings(value = "EI_EXPOSE_REP2")
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

    public Double getDecimal() {
      return decimal;
    }

    public void setDecimal(Double decimal) {
      this.decimal = decimal;
    }

    public NestedTestBean getAnotherNestedBean() {
      return anotherNestedBean;
    }

    public void setAnotherNestedBean(NestedTestBean anotherNestedBean) {
      this.anotherNestedBean = anotherNestedBean;
    }

    public final Locale getLanguage() {
      return language;
    }

    public final void setLanguage(Locale language) {
      this.language = language;
    }

    public final Map<String, NestedTestBean> getAttributes() {
      return attributes;
    }

    public final void setAttributes(Map<String, NestedTestBean> attributes) {
      this.attributes = attributes;
    }
  }

  @SuppressWarnings("UnusedDeclaration")
  public static class NestedTestBean {
    private Double decimal;

    private byte[] data;

    public final Double getDecimal() {
      return decimal;
    }

    public final void setDecimal(Double decimal) {
      this.decimal = decimal;
    }

    @edu.umd.cs.findbugs.annotations.SuppressWarnings(value = "EI_EXPOSE_REP")
    public final byte[] getData() {
      return data;
    }

    @edu.umd.cs.findbugs.annotations.SuppressWarnings(value = "EI_EXPOSE_REP2")
    public final void setData(byte... data) {
      this.data = data;
    }

  }

  @SuppressWarnings("UnusedDeclaration")
  public enum TestEnum {
    VALUE1, VALUE2
  }
}
