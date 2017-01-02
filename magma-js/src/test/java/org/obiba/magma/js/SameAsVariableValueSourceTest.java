/*
 * Copyright (c) 2017 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.magma.js;

import java.util.Locale;

import org.junit.Test;
import org.obiba.magma.ValueTable;
import org.obiba.magma.Variable;
import org.obiba.magma.type.IntegerType;
import org.obiba.magma.type.TextType;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.fest.assertions.api.Assertions.assertThat;

public class SameAsVariableValueSourceTest extends AbstractJsTest {

  @Test
  public void testGetScriptWithExplicitScriptProvided() throws Exception {
    ValueTable valueTableMock = createMock(ValueTable.class);
    SameAsVariableValueSource source = new SameAsVariableValueSource(buildSexWithScript(), valueTableMock);
    assertThat(source.getScript()).isEqualTo("$('ExistingVariable.SEX')");
  }

  @Test
  public void testGetScriptWhenScriptAttributesIsNotProvided() throws Exception {
    ValueTable valueTableMock = createMock(ValueTable.class);
    SameAsVariableValueSource source = new SameAsVariableValueSource(buildSexWithOutScriptNoCategories(),
        valueTableMock);
    assertThat(source.getScript()).isEqualTo("$('HealthQuestionnaireIdentification.SEX')");
  }

  @Test
  public void testGetVariableNoOverrides() throws Exception {
    ValueTable valueTableMock = createMock(ValueTable.class);
    SameAsVariableValueSource source = new SameAsVariableValueSource(buildSexWithOutScriptNoCategories(),
        valueTableMock);

    expect(valueTableMock.getVariable("HealthQuestionnaireIdentification.SEX"))
        .andReturn(buildHealthQuestionnaireIdentificationSex()).once();
    expect(valueTableMock.isView()).andReturn(false).atLeastOnce();
    replay(valueTableMock);
    Variable variable = source.getVariable();

    assertThat(variable.getAttributes()).hasSize(4); // Attributes not overridden
    assertThat(variable.getCategories()).hasSize(2); // Categories not overridden
    assertThat(variable.getAttribute("label").getValue()).isEqualTo(TextType.get().valueOf("Sex"));
    // Special case "sameAs" attribute appears regardless
    assertThat(variable.getAttribute("sameAs").getValue())
        .isEqualTo(TextType.get().valueOf("HealthQuestionnaireIdentification.SEX"));
    verify(valueTableMock);
  }

  @Test
  public void testGetVariableWithOverrides() throws Exception {
    ValueTable valueTableMock = createMock(ValueTable.class);
    SameAsVariableValueSource source = new SameAsVariableValueSource(buildSexWithScript(), valueTableMock);

    expect(valueTableMock.getVariable("HealthQuestionnaireIdentification.SEX"))
        .andReturn(buildHealthQuestionnaireIdentificationSex()).once();
    expect(valueTableMock.isView()).andReturn(false).atLeastOnce();
    replay(valueTableMock);
    Variable variable = source.getVariable();

    assertThat(variable.getAttributes()).hasSize(5);
    assertThat(variable.getCategories()).hasSize(2); // Categories not overridden
    assertThat(variable.getAttribute("sameAs").getValue())
        .isEqualTo(TextType.get().valueOf("HealthQuestionnaireIdentification.SEX"));
    assertThat(variable.getAttribute("script").getValue())
        .isEqualTo(TextType.get().valueOf("$('ExistingVariable.SEX')"));
    assertThat(variable.getAttribute("label").getValue()).isEqualTo(TextType.get().valueOf("Sex"));
    assertThat(variable.getAttribute("write").getValue()).isEqualTo(TextType.get().valueOf("pen"));
    assertThat(variable.getAttribute("read").getValue()).isEqualTo(TextType.get().valueOf("paper"));
    verify(valueTableMock);
  }

  private Variable buildSexWithScript() {
    Variable.Builder builder = Variable.Builder.newVariable("SameAsSex", IntegerType.get(), "Participant");
    builder.addAttribute("sameAs", "HealthQuestionnaireIdentification.SEX");
    builder.addAttribute("script", "$('ExistingVariable.SEX')");
    return builder.build();
  }

  private Variable buildSexWithOutScriptNoCategories() {
    Variable.Builder builder = Variable.Builder.newVariable("SameAsSex", IntegerType.get(), "Participant");
    builder.addAttribute("sameAs", "HealthQuestionnaireIdentification.SEX");
    return builder.build();
  }

  private Variable buildHealthQuestionnaireIdentificationSex() {
    Variable.Builder builder = Variable.Builder
        .newVariable("HealthQuestionnaireIdentification.SEX", IntegerType.get(), "Participant");
    builder.addAttribute("label", "Sex", Locale.CANADA);
    builder.addAttribute("write", "pen", Locale.CANADA);
    builder.addAttribute("read", "paper", Locale.CANADA);
    builder.addCategory("category1", "00021");
    builder.addCategory("category2", "00022");
    return builder.build();
  }
}
