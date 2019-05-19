/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.magma.js.views;

import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.obiba.magma.MagmaDate;
import org.obiba.magma.NoSuchVariableException;
import org.obiba.magma.Timestamps;
import org.obiba.magma.Value;
import org.obiba.magma.ValueSet;
import org.obiba.magma.ValueTable;
import org.obiba.magma.Variable;
import org.obiba.magma.VariableEntity;
import org.obiba.magma.VariableValueSource;
import org.obiba.magma.js.AbstractJsTest;
import org.obiba.magma.support.Initialisables;
import org.obiba.magma.type.DateTimeType;
import org.obiba.magma.type.DateType;
import org.obiba.magma.type.IntegerType;
import org.obiba.magma.type.TextType;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SuppressWarnings({ "OverlyLongMethod", "PMD.NcssMethodCount" })
public class VariablesClauseTest extends AbstractJsTest {

  private final static Date NINETEEN_FIFTY_FIVE = constructDate(1955);

  private Set<Variable> variables;

  private Value adminParticipantBirthDateValue;

  private Value healthQuestionnaireIdentificationSexValue;

  @Before
  @Override
  public void before() {
    super.before();
    Variable yearVariable = buildYear();
    Variable sex = buildSexWithSameAsAndScript();

    variables = new HashSet<>();
    variables.add(yearVariable);
    variables.add(sex);

    adminParticipantBirthDateValue = DateTimeType.get().valueOf(NINETEEN_FIFTY_FIVE);
    healthQuestionnaireIdentificationSexValue = IntegerType.get().valueOf(5);
  }

  private Variable buildYear() {
    Variable.Builder builder = Variable.Builder.newVariable("GENERIC_128", IntegerType.get(), "Participant");
    builder.addAttribute("label", "Birth Year", Locale.CANADA);
    builder.addAttribute("URI", "http://www.datashaper.org/owl/2009/10/generic.owl#GENERIC_128");
    builder.addAttribute("script", "$('Admin.Participant.birthDate').year()");
    return builder.build();
  }

  private Variable buildSexWithSameAsAndScript() {
    Variable.Builder builder = Variable.Builder.newVariable("GENERIC_129", IntegerType.get(), "Participant");
    builder.addAttribute("sameAs", "HealthQuestionnaireIdentification.SEX");
    builder.addAttribute("script", "$('HealthQuestionnaireIdentification.SEX')");
    return builder.build();
  }

  private Variable buildSexWithSameAs() {
    Variable.Builder builder = Variable.Builder.newVariable("GENERIC_300", IntegerType.get(), "Participant");
    builder.addAttribute("sameAs", "HealthQuestionnaireIdentification.SEX");
    return builder.build();
  }

  private Variable buildHealthQuestionnaireIdentificationSex() {
    Variable.Builder builder = Variable.Builder
        .newVariable("HealthQuestionnaireIdentification.SEX", IntegerType.get(), "Participant");
    builder.addAttribute("label", "Sex", Locale.CANADA);
    builder.addAttribute("URI", "http://www.obiba.org/sex");
    builder.addAttribute("stage", "HealthQuestionnaire");
    return builder.build();
  }

  @Test
  public void test_setVariables_AcceptsNull() {
    VariablesClause clause = new VariablesClause();
    clause.setVariables(null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void test_setValueTable_ThrowsIllegalArgumentWhenNull() {
    //noinspection ConstantConditions
    new VariablesClause().setValueTable(null);
  }

  @Test(expected = IllegalStateException.class)
  public void test_initialize_ThrowsIfValueTableIsNull() {
    new VariablesClause().initialise();
  }

  @Test
  public void test_getVariablesValueSources_ReturnsEmptyIterable() {
    VariablesClause clause = new VariablesClause();
    clause.setValueTable(mock(ValueTable.class));
    clause.initialise();
    Iterable<VariableValueSource> sources = clause.getVariableValueSources();
    assertThat(sources).isNotNull();
    assertThat(sources).isEmpty();
  }

  @Test(expected = IllegalStateException.class)
  public void test_getVariablesValueSources_ThrowsIfNotInitialized() {
    new VariablesClause().getVariableValueSources();
  }

  @Test(expected = NoSuchVariableException.class)
  public void test_getVariablesValueSource_ThrowsNoSuchVariableException() {
    VariablesClause clause = new VariablesClause();
    clause.setValueTable(mock(ValueTable.class));
    clause.initialise();
    clause.getVariableValueSource("test");
  }

  @Test(expected = IllegalStateException.class)
  public void test_getVariablesValueSource_ThrowsIfNotInitialized() {
    new VariablesClause().getVariableValueSource("test");
  }

  @Test
  public void testScriptValue() throws Exception {
    ValueTable table = mock(ValueTable.class);
    ValueSet valueSet = mock(ValueSet.class);
    VariableValueSource variableValueSource = mock(VariableValueSource.class);
    Variable variable = mock(Variable.class);

    when(table.getTableReference()).thenReturn("table");
    when(valueSet.getValueTable()).thenReturn(table);
    when(valueSet.getVariableEntity()).thenReturn(mock(VariableEntity.class));
    when(table.getVariable("HealthQuestionnaireIdentification.SEX"))
        .thenReturn(buildHealthQuestionnaireIdentificationSex());

    when(table.getVariableValueSource("Admin.Participant.birthDate")).thenReturn(variableValueSource);
    when(variableValueSource.getValue(valueSet)).thenReturn(adminParticipantBirthDateValue);
    when(variableValueSource.getVariable()).thenReturn(variable);
    when(table.isView()).thenReturn(false);
    when(variable.getUnit()).thenReturn(null);

    Timestamps tableTimestamps = mock(Timestamps.class);
    when(tableTimestamps.getLastUpdate()).thenReturn(DateType.get().valueOf(new MagmaDate(new Date())));
    when(table.getTimestamps()).thenReturn(tableTimestamps);

    VariablesClause clause = new VariablesClause();
    clause.setVariables(variables);
    clause.setValueTable(table);
    Initialisables.initialise(clause);

    VariableValueSource variableValueSource_generic128 = clause.getVariableValueSource("GENERIC_128");

    assertThat(variableValueSource_generic128).isNotNull();

    Value result = variableValueSource_generic128.getValue(valueSet);

    verify(variableValueSource).getValue(valueSet);
    verify(variable).getUnit();
    verify(table, atLeastOnce()).isView();

    assertThat(result.getValueType()).isEqualTo(IntegerType.get());
    assertThat(result).isEqualTo(IntegerType.get().valueOf(1955));
  }

  @Test
  public void testScriptVariable() throws Exception {
    ValueTable valueTableMock = mock(ValueTable.class);
    when(valueTableMock.getVariable("HealthQuestionnaireIdentification.SEX"))
        .thenReturn(buildHealthQuestionnaireIdentificationSex());
    when(valueTableMock.isView()).thenReturn(false);
    VariablesClause clause = new VariablesClause();
    clause.setValueTable(valueTableMock);
    clause.setVariables(variables);
    Initialisables.initialise(clause);
    VariableValueSource variableValueSource = clause.getVariableValueSource("GENERIC_128");
    Variable variable = variableValueSource.getVariable();

    //verify(valueTableMock, atLeastOnce()).isView();

    assertThat(variable.getAttribute("label").getLocale()).isEqualTo(Locale.CANADA);
    assertThat(variable.getAttribute("label").getValue()).isEqualTo(TextType.get().valueOf("Birth Year"));
    assertThat(variable.getAttribute("URI").getValue())
        .isEqualTo(TextType.get().valueOf("http://www.datashaper.org/owl/2009/10/generic.owl#GENERIC_128"));
    assertThat(variable.getAttribute("script").getValue())
        .isEqualTo(TextType.get().valueOf("$('Admin.Participant.birthDate').year()"));
    assertThat(variable.getName()).isEqualTo("GENERIC_128");
    assertThat(variable.getEntityType()).isEqualTo("Participant");

  }

  @Test
  public void testSameAsWithExplicitScriptValue() throws Exception {
    ValueTable table = mock(ValueTable.class);
    ValueSet valueSet = mock(ValueSet.class);
    VariableValueSource variableSource = mock(VariableValueSource.class);
    Variable variable = mock(Variable.class);

    when(table.getTableReference()).thenReturn("table");
    when(valueSet.getValueTable()).thenReturn(table);
    when(valueSet.getVariableEntity()).thenReturn(mock(VariableEntity.class));
    when(table.getVariable("HealthQuestionnaireIdentification.SEX"))
        .thenReturn(buildHealthQuestionnaireIdentificationSex());
    when(table.getVariableValueSource("HealthQuestionnaireIdentification.SEX")).thenReturn(variableSource);
    when(table.isView()).thenReturn(false);
    when(variableSource.getValue(valueSet)).thenReturn(healthQuestionnaireIdentificationSexValue);
    when(variableSource.getVariable()).thenReturn(variable);
    when(variable.getUnit()).thenReturn(null);

    Timestamps tableTimestamps = mock(Timestamps.class);
    when(tableTimestamps.getLastUpdate()).thenReturn(DateType.get().valueOf(new MagmaDate(new Date())));
    when(table.getTimestamps()).thenReturn(tableTimestamps);

    VariablesClause clause = new VariablesClause();
    clause.setValueTable(table);
    clause.setVariables(variables);
    Initialisables.initialise(clause);
    VariableValueSource variableValueSource = clause.getVariableValueSource("GENERIC_129");

    assertThat(variableValueSource).isNotNull();

    Value result = variableValueSource.getValue(valueSet);

    verify(table).getVariable("HealthQuestionnaireIdentification.SEX");
    verify(variableSource).getValue(valueSet);
    verify(variable).getUnit();
    verify(table, atLeastOnce()).isView();

    assertThat(result.getValueType()).isEqualTo(IntegerType.get());
    assertThat(result).isEqualTo(IntegerType.get().valueOf(5));
  }

  @Test
  public void testThatDerivedVariableWithSameAsAndScriptAttributesOverridesExistingVariableAttributes()
      throws Exception {
    ValueTable valueTableMock = mock(ValueTable.class);
    when(valueTableMock.getVariable("HealthQuestionnaireIdentification.SEX"))
        .thenReturn(buildHealthQuestionnaireIdentificationSex());
    when(valueTableMock.isView()).thenReturn(false);
    VariablesClause clause = new VariablesClause();
    clause.setValueTable(valueTableMock);
    clause.setVariables(variables);
    Initialisables.initialise(clause);
    VariableValueSource variableValueSource = clause.getVariableValueSource("GENERIC_129");
    Variable variable = variableValueSource.getVariable();

    verify(valueTableMock, times(2)).getVariable("HealthQuestionnaireIdentification.SEX");
    verify(valueTableMock, atLeastOnce()).isView();

    assertThat(variable.getAttribute("sameAs").getValue())
        .isEqualTo(TextType.get().valueOf("HealthQuestionnaireIdentification.SEX"));
    assertThat(variable.getAttribute("script").getValue())
        .isEqualTo(TextType.get().valueOf("$('HealthQuestionnaireIdentification.SEX')"));
    assertThat(variable.getName()).isEqualTo("GENERIC_129");
    assertThat(variable.getEntityType()).isEqualTo("Participant");
  }

  @Test
  public void testThatDerivedVariableWithSameAsAttributeOnlyDoesNotOverrideExistingVariableAttributes()
      throws Exception {
    Collection<Variable> variableSet = new HashSet<>();
    variableSet.add(buildSexWithSameAs());

    ValueTable valueTableMock = mock(ValueTable.class);
    when(valueTableMock.getVariable("HealthQuestionnaireIdentification.SEX"))
        .thenReturn(buildHealthQuestionnaireIdentificationSex());
    when(valueTableMock.isView()).thenReturn(false);
    VariablesClause clause = new VariablesClause();
    clause.setValueTable(valueTableMock);
    clause.setVariables(variableSet);
    Initialisables.initialise(clause);
    VariableValueSource variableValueSource = clause.getVariableValueSource("GENERIC_300");
    Variable variable = variableValueSource.getVariable();

    verify(valueTableMock, times(2)).getVariable("HealthQuestionnaireIdentification.SEX");
    verify(valueTableMock, atLeastOnce()).isView();

    assertThat(variable.getAttribute("sameAs").getValue())
        .isEqualTo(TextType.get().valueOf("HealthQuestionnaireIdentification.SEX"));
    assertThat(variable.getAttribute("stage").getValue()).isEqualTo(TextType.get().valueOf("HealthQuestionnaire"));
    assertThat(variable.getName()).isEqualTo("GENERIC_300");
    assertThat(variable.getEntityType()).isEqualTo("Participant");

  }

  private static Date constructDate(int year) {
    Calendar calendar = Calendar.getInstance();
    calendar.set(Calendar.HOUR, 7);
    calendar.set(Calendar.MINUTE, 0);
    calendar.set(Calendar.SECOND, 0);
    calendar.set(Calendar.MILLISECOND, 0);
    calendar.set(Calendar.AM_PM, Calendar.AM);
    calendar.set(Calendar.YEAR, year);
    return calendar.getTime();
  }
}
