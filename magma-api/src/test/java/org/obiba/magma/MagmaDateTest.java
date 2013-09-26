/*******************************************************************************
 * Copyright (c) 2011 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.magma;

import java.util.Calendar;
import java.util.Date;

import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;

public class MagmaDateTest {

  @Test
  public void test_ctor_calendar() {
    Calendar c = Calendar.getInstance();
    MagmaDate magmaDate = new MagmaDate(c);
    Assert.assertThat(magmaDate.getYear(), is(c.get(Calendar.YEAR)));
    Assert.assertThat(magmaDate.getMonth(), is(c.get(Calendar.MONTH)));
    Assert.assertThat(magmaDate.getDayOfMonth(), is(c.get(Calendar.DAY_OF_MONTH)));
  }

  @Test(expected = IllegalArgumentException.class)
  public void test_ctor_calendarWithNull() {
    new MagmaDate((Calendar) null);
  }

  @Test
  public void test_ctor_date() {
    Calendar c = Calendar.getInstance();
    MagmaDate magmaDate = new MagmaDate(c.getTime());
    Assert.assertThat(magmaDate.getYear(), is(c.get(Calendar.YEAR)));
    Assert.assertThat(magmaDate.getMonth(), is(c.get(Calendar.MONTH)));
    Assert.assertThat(magmaDate.getDayOfMonth(), is(c.get(Calendar.DAY_OF_MONTH)));
  }

  @Test(expected = IllegalArgumentException.class)
  public void test_ctor_dateWithNull() {
    new MagmaDate((Date) null);
  }

  @Test
  public void test_ctor_values() {
    MagmaDate magmaDate = new MagmaDate(3048, 5, 26);
    Assert.assertThat(magmaDate.getYear(), is(3048));
    Assert.assertThat(magmaDate.getMonth(), is(5));
    Assert.assertThat(magmaDate.getDayOfMonth(), is(26));
  }

  @Test
  public void test_comparable_equal() {
    MagmaDate one = new MagmaDate(3048, 5, 26);
    MagmaDate two = new MagmaDate(3048, 5, 26);
    Assert.assertThat(one.compareTo(two), is(0));
    Assert.assertThat(two.compareTo(one), is(0));
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
      Assert.assertThat(lhs.hashCode() == rhs.hashCode(), is(true));
    }
  }

  private void assertEqual(MagmaDate lhs, MagmaDate rhs) {
    Assert.assertThat(lhs.equals(rhs), is(true));
    Assert.assertThat(rhs.equals(lhs), is(true));
  }

  private void assertNotEqual(MagmaDate lhs, MagmaDate rhs) {
    Assert.assertThat(lhs.equals(rhs), is(false));
    Assert.assertThat(rhs.equals(lhs), is(false));
  }

  private void assertLessThan(MagmaDate less, MagmaDate more) {
    Assert.assertThat(less.compareTo(more), is(Matchers.lessThan(0)));
    Assert.assertThat(more.compareTo(less), is(Matchers.greaterThan(0)));
  }
}
