package org.obiba.magma.js.views;

import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.obiba.magma.NoSuchVariableException;
import org.obiba.magma.Value;
import org.obiba.magma.ValueSet;
import org.obiba.magma.ValueTable;
import org.obiba.magma.ValueType;
import org.obiba.magma.Variable;
import org.obiba.magma.VariableEntity;
import org.obiba.magma.VariableValueSource;
import org.obiba.magma.js.AbstractJsTest;
import org.obiba.magma.support.Initialisables;
import org.obiba.magma.type.DateTimeType;
import org.obiba.magma.type.IntegerType;
import org.obiba.magma.type.TextType;

import com.google.common.collect.Iterables;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

@SuppressWarnings({ "OverlyLongMethod", "PMD.NcssMethodCount" })
public class VariablesClauseTest extends AbstractJsTest {

  private Set<Variable> variables;

  private final Date nineteenFiftyFive = constructDate(1955);

  private Value adminParticipantBirthDateValue;

  private Value healthQuestionnaireIdentificationSexValue;

  @Before
  public void setUp() throws Exception {
    Variable yearVariable = buildYear();
    Variable sex = buildSexWithSameAsAndScript();

    variables = new HashSet<>();
    variables.add(yearVariable);
    variables.add(sex);

    adminParticipantBirthDateValue = DateTimeType.get().valueOf(nineteenFiftyFive);
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
    new VariablesClause().setValueTable(null);
  }

  @Test(expected = IllegalStateException.class)
  public void test_initialize_ThrowsIfValueTableIsNull() {
    new VariablesClause().initialise();
  }

  @Test
  public void test_getVariablesValueSources_ReturnsEmptyIterable() {
    VariablesClause clause = new VariablesClause();
    clause.setValueTable(createMock(ValueTable.class));
    clause.initialise();
    Iterable<VariableValueSource> sources = clause.getVariableValueSources();
    assertThat(sources, notNullValue());
    assertThat(Iterables.size(sources), is(0));
  }

  @Test(expected = IllegalStateException.class)
  public void test_getVariablesValueSources_ThrowsIfNotInitialized() {
    new VariablesClause().getVariableValueSources();
  }

  @Test(expected = NoSuchVariableException.class)
  public void test_getVariablesValueSource_ThrowsNoSuchVariableException() {
    VariablesClause clause = new VariablesClause();
    clause.setValueTable(createMock(ValueTable.class));
    clause.initialise();
    clause.getVariableValueSource("test");
  }

  @Test(expected = IllegalStateException.class)
  public void test_getVariablesValueSource_ThrowsIfNotInitialized() {
    new VariablesClause().getVariableValueSource("test");
  }

  @Test
  public void testScriptValue() throws Exception {
    ValueTable table = createMock(ValueTable.class);
    ValueSet valueSet = createMock(ValueSet.class);
    VariableValueSource variableValueSource = createMock(VariableValueSource.class);
    Variable variable = createMock(Variable.class);

    expect(valueSet.getValueTable()).andReturn(table).anyTimes();
    expect(valueSet.getVariableEntity()).andReturn(createMock(VariableEntity.class));
    expect(table.getVariable("HealthQuestionnaireIdentification.SEX"))
        .andReturn(buildHealthQuestionnaireIdentificationSex()).anyTimes();

    expect(table.getVariableValueSource("Admin.Participant.birthDate")).andReturn(variableValueSource).once();
    expect(variableValueSource.getValue(valueSet)).andReturn(adminParticipantBirthDateValue).once();
    expect(variableValueSource.getVariable()).andReturn(variable).once();
    expect(table.isView()).andReturn(false).atLeastOnce();
    expect(variable.getUnit()).andReturn(null).once();

    replay(valueSet, table, variableValueSource, variable);
    VariablesClause clause = new VariablesClause();
    clause.setVariables(variables);
    clause.setValueTable(table);
    Initialisables.initialise(clause);

    VariableValueSource variableValueSource_generic128 = clause.getVariableValueSource("GENERIC_128");

    assertThat(variableValueSource_generic128, is(notNullValue()));

    Value result = variableValueSource_generic128.getValue(valueSet);
    verify(valueSet, table, variableValueSource, variable);

    assertThat(result.getValueType(), is((ValueType) IntegerType.get()));
    assertThat(result, is(IntegerType.get().valueOf(1955)));
  }

  @Test
  public void testScriptVariable() throws Exception {
    ValueTable valueTableMock = createMock(ValueTable.class);
    expect(valueTableMock.getVariable("HealthQuestionnaireIdentification.SEX"))
        .andReturn(buildHealthQuestionnaireIdentificationSex()).anyTimes();
    expect(valueTableMock.isView()).andReturn(false).atLeastOnce();
    replay(valueTableMock);
    VariablesClause clause = new VariablesClause();
    clause.setValueTable(valueTableMock);
    clause.setVariables(variables);
    Initialisables.initialise(clause);
    VariableValueSource variableValueSource = clause.getVariableValueSource("GENERIC_128");
    Variable variable = variableValueSource.getVariable();
    verify(valueTableMock);
    assertThat(variable.getAttribute("label").getLocale(), is(Locale.CANADA));
    assertThat(variable.getAttribute("label").getValue(), is(TextType.get().valueOf("Birth Year")));
    assertThat(variable.getAttribute("URI").getValue(),
        is(TextType.get().valueOf("http://www.datashaper.org/owl/2009/10/generic.owl#GENERIC_128")));
    assertThat(variable.getAttribute("script").getValue(),
        is(TextType.get().valueOf("$('Admin.Participant.birthDate').year()")));
    assertThat(variable.getName(), is("GENERIC_128"));
    assertThat(variable.getEntityType(), is("Participant"));

  }

  @Test
  public void testSameAsWithExplicitScriptValue() throws Exception {
    ValueTable valueTableMock = createMock(ValueTable.class);
    ValueSet valueSetMock = createMock(ValueSet.class);
    VariableValueSource variableValueSourceMock = createMock(VariableValueSource.class);
    Variable mockVariable = createMock(Variable.class);

    expect(valueSetMock.getValueTable()).andReturn(valueTableMock).anyTimes();
    expect(valueSetMock.getVariableEntity()).andReturn(createMock(VariableEntity.class)).anyTimes();
    expect(valueTableMock.getVariable("HealthQuestionnaireIdentification.SEX"))
        .andReturn(buildHealthQuestionnaireIdentificationSex()).once();
    expect(valueTableMock.getVariableValueSource("HealthQuestionnaireIdentification.SEX"))
        .andReturn(variableValueSourceMock).once();
    expect(valueTableMock.isView()).andReturn(false).atLeastOnce();
    expect(variableValueSourceMock.getValue(valueSetMock)).andReturn(healthQuestionnaireIdentificationSexValue).once();
    expect(variableValueSourceMock.getVariable()).andReturn(mockVariable).once();
    expect(mockVariable.getUnit()).andReturn(null).once();

    replay(valueSetMock, valueTableMock, variableValueSourceMock, mockVariable);

    VariablesClause clause = new VariablesClause();
    clause.setValueTable(valueTableMock);
    clause.setVariables(variables);
    Initialisables.initialise(clause);
    VariableValueSource variableValueSource = clause.getVariableValueSource("GENERIC_129");

    assertThat(variableValueSource, is(notNullValue()));

    Value result = variableValueSource.getValue(valueSetMock);
    verify(valueSetMock, valueTableMock, variableValueSourceMock, mockVariable);

    assertThat(result.getValueType(), is((ValueType) IntegerType.get()));
    assertThat(result, is(IntegerType.get().valueOf(5)));
  }

  @Test
  public void testThatDerivedVariableWithSameAsAndScriptAttributesOverridesExistingVariableAttributes()
      throws Exception {
    ValueTable valueTableMock = createMock(ValueTable.class);
    expect(valueTableMock.getVariable("HealthQuestionnaireIdentification.SEX"))
        .andReturn(buildHealthQuestionnaireIdentificationSex()).times(2);
    expect(valueTableMock.isView()).andReturn(false).atLeastOnce();
    replay(valueTableMock);
    VariablesClause clause = new VariablesClause();
    clause.setValueTable(valueTableMock);
    clause.setVariables(variables);
    Initialisables.initialise(clause);
    VariableValueSource variableValueSource = clause.getVariableValueSource("GENERIC_129");
    Variable variable = variableValueSource.getVariable();

    verify(valueTableMock);
    assertThat(variable.getAttribute("sameAs").getValue(),
        is(TextType.get().valueOf("HealthQuestionnaireIdentification.SEX")));
    assertThat(variable.getAttribute("script").getValue(),
        is(TextType.get().valueOf("$('HealthQuestionnaireIdentification.SEX')")));
    assertThat(variable.getName(), is("GENERIC_129"));
    assertThat(variable.getEntityType(), is("Participant"));
  }

  @Test
  public void testThatDerivedVariableWithSameAsAttributeOnlyDoesNotOverrideExistingVariableAttributes()
      throws Exception {
    Collection<Variable> variableSet = new HashSet<>();
    variableSet.add(buildSexWithSameAs());

    ValueTable valueTableMock = createMock(ValueTable.class);
    expect(valueTableMock.getVariable("HealthQuestionnaireIdentification.SEX"))
        .andReturn(buildHealthQuestionnaireIdentificationSex()).times(2);
    expect(valueTableMock.isView()).andReturn(false).atLeastOnce();
    replay(valueTableMock);
    VariablesClause clause = new VariablesClause();
    clause.setValueTable(valueTableMock);
    clause.setVariables(variableSet);
    Initialisables.initialise(clause);
    VariableValueSource variableValueSource = clause.getVariableValueSource("GENERIC_300");
    Variable variable = variableValueSource.getVariable();
    verify(valueTableMock);
    assertThat(variable.getAttribute("sameAs").getValue(),
        is(TextType.get().valueOf("HealthQuestionnaireIdentification.SEX")));
    assertThat(variable.getAttribute("stage").getValue(), is(TextType.get().valueOf("HealthQuestionnaire")));
    assertThat(variable.getName(), is("GENERIC_300"));
    assertThat(variable.getEntityType(), is("Participant"));

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
