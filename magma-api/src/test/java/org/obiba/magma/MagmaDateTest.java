/*
 * Copyright (c) 2017 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.magma;

import java.util.Calendar;
import java.util.Date;

import org.junit.Test;

import static org.fest.assertions.api.Assertions.assertThat;

public class MagmaDateTest {

  @Test
  public void test_ctor_calendar() {
    Calendar c = Calendar.getInstance();
    MagmaDate magmaDate = new MagmaDate(c);
    assertThat(magmaDate.getYear()).isEqualTo(c.get(Calendar.YEAR));
    assertThat(magmaDate.getMonth()).isEqualTo(c.get(Calendar.MONTH));
    assertThat(magmaDate.getDayOfMonth()).isEqualTo(c.get(Calendar.DAY_OF_MONTH));
  }

  @Test(expected = IllegalArgumentException.class)
  public void test_ctor_calendarWithNull() {
    new MagmaDate((Calendar) null);
  }

  @Test
  public void test_ctor_date() {
    Calendar c = Calendar.getInstance();
    MagmaDate magmaDate = new MagmaDate(c.getTime());
    assertThat(magmaDate.getYear()).isEqualTo(c.get(Calendar.YEAR));
    assertThat(magmaDate.getMonth()).isEqualTo(c.get(Calendar.MONTH));
    assertThat(magmaDate.getDayOfMonth()).isEqualTo(c.get(Calendar.DAY_OF_MONTH));
  }

  @Test(expected = IllegalArgumentException.class)
  public void test_ctor_dateWithNull() {
    new MagmaDate((Date) null);
  }

  @Test
  public void test_ctor_values() {
    MagmaDate magmaDate = new MagmaDate(3048, 5, 26);
    assertThat(magmaDate.getYear()).isEqualTo(3048);
    assertThat(magmaDate.getMonth()).isEqualTo(5);
    assertThat(magmaDate.getDayOfMonth()).isEqualTo(26);
  }

  @Test
  public void test_comparable_equal() {
    MagmaDate one = new MagmaDate(3048, 5, 26);
    MagmaDate two = new MagmaDate(3048, 5, 26);
    assertThat(one.compareTo(two)).isEqualTo(0);
    assertThat(two.compareTo(one)).isEqualTo(0);
  }

  @Test
  public void test_comparable_notEqual() {
    assertLessThan(new MagmaDate(2011, 1, 1), new MagmaDate(2012, 1, 1));
    assertLessThan(new MagmaDate(2011, 1, 1), new MagmaDate(2011, 2, 1));
    assertLessThan(new MagmaDate(2011, 1, 1), new MagmaDate(2011, 1, 2));

    assertLessThan(new MagmaDate(2011, 0, 31), new MagmaDate(2011, 1, 1));
    assertLessThan(new MagmaDate(2011, 11, 31), new MagmaDate(2012, 0, 1));
  }

  @Test
  public void test_equal() {
    assertEqual(new MagmaDate(2011, 1, 1), new MagmaDate(2011, 1, 1));

    // January 32nd == February 1st
    assertEqual(new MagmaDate(2011, 1, 1), new MagmaDate(2011, 0, 32));

    // 1st day of 13th month == January 1st
    assertEqual(new MagmaDate(2011, 0, 1), new MagmaDate(2010, 12, 1));
  }

  @Test
  public void test_notEqual() {
    assertNotEqual(new MagmaDate(2011, 1, 1), new MagmaDate(2011, 1, 2));
  }

  @Test
  public void test_hashCode() {
    assertHashCode(new MagmaDate(2011, 1, 1), new MagmaDate(2011, 1, 1));
  }

  private void assertHashCode(MagmaDate lhs, MagmaDate rhs) {
    if(lhs.equals(rhs)) {
      assertThat(lhs.hashCode() == rhs.hashCode()).isEqualTo(true);
    }
  }

  private void assertEqual(MagmaDate lhs, MagmaDate rhs) {
    assertThat(lhs.equals(rhs)).isTrue();
    assertThat(rhs.equals(lhs)).isTrue();
  }

  private void assertNotEqual(MagmaDate lhs, MagmaDate rhs) {
    assertThat(lhs.equals(rhs)).isFalse();
    assertThat(rhs.equals(lhs)).isFalse();
  }

  private void assertLessThan(MagmaDate less, MagmaDate more) {
    assertThat(less.compareTo(more)).isLessThan(0);
    assertThat(more.compareTo(less)).isGreaterThan(0);
  }
}
