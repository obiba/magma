/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.magma.js;

import org.junit.Test;
import org.obiba.magma.Attribute;
import org.obiba.magma.Variable;
import org.obiba.magma.type.BooleanType;

import static org.fest.assertions.api.Assertions.assertThat;

public class JavascriptVariableBuilderTest extends AbstractJsTest {

  @Test
  public void testSimpleVariable() {
    Variable.Builder builder = Variable.Builder.newVariable("myJsTest", BooleanType.get(), "Participant");
    Variable variable = builder.extend(JavascriptVariableBuilder.class).setScript("'Hello World!'").build();
    Attribute attribute = variable.getAttribute(JavascriptVariableBuilder.SCRIPT_ATTRIBUTE_NAME);
    assertThat(attribute).isNotNull();
    assertThat(attribute.getValue()).isNotNull();
    assertThat(attribute.getValue().toString()).isEqualTo("'Hello World!'");
  }
}
