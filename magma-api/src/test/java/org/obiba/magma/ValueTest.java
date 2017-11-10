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

import org.junit.Test;
import org.obiba.magma.type.TextType;

import static org.fest.assertions.api.Assertions.assertThat;

public class ValueTest extends AbstractValueTest {

  @Test
  public void test_asSequence_NotASequence() {
    Value value = testValue();
    assertThat(value.isSequence()).isFalse();
    ValueSequence sequence = value.asSequence();
    assertThat(sequence.isSequence()).isTrue();
    assertThat(sequence.getSize()).isEqualTo(1);
  }

  @Override
  protected Value testValue() {
    return TextType.get().valueOf(testObject());
  }

  @Override
  protected Object testObject() {
    return "A Test";
  }
}
