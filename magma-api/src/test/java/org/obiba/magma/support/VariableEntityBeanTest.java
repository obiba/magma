package org.obiba.magma.support;

import org.junit.Test;
import org.obiba.magma.VariableEntity;

import static org.fest.assertions.api.Assertions.assertThat;

public class VariableEntityBeanTest {

  @SuppressWarnings("ConstantConditions")
  @edu.umd.cs.findbugs.annotations.SuppressWarnings("NP_NONNULL_PARAM_VIOLATION")
  @Test(expected = IllegalArgumentException.class)
  public void test_ctor_preconditions_1() {
    new VariableEntityBean(null, "1");
  }

  @SuppressWarnings("ConstantConditions")
  @edu.umd.cs.findbugs.annotations.SuppressWarnings("NP_NONNULL_PARAM_VIOLATION")
  @Test(expected = IllegalArgumentException.class)
  public void test_ctor_preconditions_2() {
    new VariableEntityBean("type", null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void test_ctor_preconditions_3() {
    new VariableEntityBean("type", "");
  }

  @Test(expected = IllegalArgumentException.class)
  public void test_ctor_preconditions_4() {
    new VariableEntityBean("type", "   ");
  }

  @Test
  public void test_getters() {
    VariableEntity veb = new VariableEntityBean("type", "1");
    assertThat(veb.getType()).isEqualTo("type");
    assertThat(veb.getIdentifier()).isEqualTo("1");
  }

  @Test
  public void test_equals_same_instance() {
    VariableEntityBean veb = new VariableEntityBean("type", "1");
    assertThat(veb.equals(veb)).isTrue();
  }

  @Test
  public void test_equals_notEquivalent() {
    VariableEntityBean lhs = new VariableEntityBean("type", "1");
    VariableEntityBean rhs = new VariableEntityBean("type", "2");
    assertThat(lhs.equals(rhs)).isFalse();
  }

  @Test
  public void test_equals_hashCode_equivalent() {
    VariableEntityBean lhs = new VariableEntityBean("type", "1");
    VariableEntityBean rhs = new VariableEntityBean("type", "1");
    assertThat(lhs.equals(rhs)).isTrue();
    assertThat(lhs.hashCode() == rhs.hashCode()).isTrue();
  }

  @Test
  public void test_equals_otherType() {
    VariableEntityBean lhs = new VariableEntityBean("type", "1");
    assertThat(lhs.equals(new Object())).isFalse();
  }

  @Test
  public void test_toString_containsTypeAndIdentifier() {
    VariableEntityBean veb = new VariableEntityBean("type", "1234");
    assertThat(veb.toString().contains("type")).isTrue();
    assertThat(veb.toString().contains("1234")).isTrue();
  }
}
