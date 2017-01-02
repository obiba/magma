/*
 * Copyright (c) 2017 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.magma.lang;

import org.junit.Test;

import static org.fest.assertions.api.Assertions.assertThat;

public class BooleansTest {

  @Test
  public void test_ternaryOr_implementsTruthTableCorrectly() {
    assertThat(Booleans.ternaryOr(true, true)).isTrue();
    assertThat(Booleans.ternaryOr(true, null)).isTrue();
    assertThat(Booleans.ternaryOr(true, false)).isTrue();

    assertThat(Booleans.ternaryOr(null, true)).isTrue();
    assertThat(Booleans.ternaryOr(null, null)).isNull();
    assertThat(Booleans.ternaryOr(null, false)).isNull();

    assertThat(Booleans.ternaryOr(false, true)).isTrue();
    assertThat(Booleans.ternaryOr(false, null)).isNull();
    assertThat(Booleans.ternaryOr(false, false)).isFalse();
  }

  @Test
  public void test_ternaryAnd_implementsTruthTableCorrectly() {
    assertThat(Booleans.ternaryAnd(true, true)).isTrue();
    assertThat(Booleans.ternaryAnd(true, null)).isNull();
    assertThat(Booleans.ternaryAnd(true, false)).isFalse();

    assertThat(Booleans.ternaryAnd(null, true)).isNull();
    assertThat(Booleans.ternaryAnd(null, null)).isNull();
    assertThat(Booleans.ternaryAnd(null, false)).isFalse();

    assertThat(Booleans.ternaryAnd(false, true)).isFalse();
    assertThat(Booleans.ternaryAnd(false, null)).isFalse();
    assertThat(Booleans.ternaryAnd(false, false)).isFalse();
  }

  @Test
  public void test_ternaryNot_implementsTruthTableCorrectly() {
    assertThat(Booleans.ternaryNot(true)).isFalse();
    assertThat(Booleans.ternaryNot(null)).isNull();
    assertThat(Booleans.ternaryNot(false)).isTrue();
  }

  @Test
  public void test_isTrue_implementsTruthTableCorrectly() {
    assertThat(Booleans.isTrue(true)).isTrue();
    assertThat(Booleans.isTrue(null)).isFalse();
    assertThat(Booleans.isTrue(false)).isFalse();
  }

  @Test
  public void test_isFalse_implementsTruthTableCorrectly() {
    assertThat(Booleans.isFalse(true)).isFalse();
    assertThat(Booleans.isFalse(null)).isFalse();
    assertThat(Booleans.isFalse(false)).isTrue();
  }
}
