package org.obiba.magma.lang;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class BooleansTest {

  private final Boolean _null = null;

  @Test
  public void test_ternaryOr_implementsTruthTableCorrectly() {
    assertThat(Booleans.ternaryOr(true, true), is(true));
    assertThat(Booleans.ternaryOr(true, null), is(true));
    assertThat(Booleans.ternaryOr(true, false), is(true));

    assertThat(Booleans.ternaryOr(null, true), is(true));
    assertThat(Booleans.ternaryOr(null, null), is(_null));
    assertThat(Booleans.ternaryOr(null, false), is(_null));

    assertThat(Booleans.ternaryOr(false, true), is(true));
    assertThat(Booleans.ternaryOr(false, null), is(_null));
    assertThat(Booleans.ternaryOr(false, false), is(false));
  }

  @Test
  public void test_ternaryAnd_implementsTruthTableCorrectly() {
    assertThat(Booleans.ternaryAnd(true, true), is(true));
    assertThat(Booleans.ternaryAnd(true, null), is(_null));
    assertThat(Booleans.ternaryAnd(true, false), is(false));

    assertThat(Booleans.ternaryAnd(null, true), is(_null));
    assertThat(Booleans.ternaryAnd(null, null), is(_null));
    assertThat(Booleans.ternaryAnd(null, false), is(false));

    assertThat(Booleans.ternaryAnd(false, true), is(false));
    assertThat(Booleans.ternaryAnd(false, null), is(false));
    assertThat(Booleans.ternaryAnd(false, false), is(false));
  }

  @Test
  public void test_ternaryNot_implementsTruthTableCorrectly() {
    assertThat(Booleans.ternaryNot(true), is(false));
    assertThat(Booleans.ternaryNot(null), is(_null));
    assertThat(Booleans.ternaryNot(false), is(true));
  }

  @Test
  public void test_isTrue_implementsTruthTableCorrectly() {
    assertThat(Booleans.isTrue(true), is(true));
    assertThat(Booleans.isTrue(null), is(false));
    assertThat(Booleans.isTrue(false), is(false));
  }

  @Test
  public void test_isFalse_implementsTruthTableCorrectly() {
    assertThat(Booleans.isFalse(true), is(false));
    assertThat(Booleans.isFalse(null), is(false));
    assertThat(Booleans.isFalse(false), is(true));
  }
}
