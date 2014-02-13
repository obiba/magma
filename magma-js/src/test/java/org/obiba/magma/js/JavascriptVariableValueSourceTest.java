package org.obiba.magma.js;

import java.util.Date;

import org.junit.Test;
import org.obiba.magma.Datasource;
import org.obiba.magma.MagmaDate;
import org.obiba.magma.Timestamps;
import org.obiba.magma.Value;
import org.obiba.magma.ValueSet;
import org.obiba.magma.ValueTable;
import org.obiba.magma.Variable;
import org.obiba.magma.VariableEntity;
import org.obiba.magma.VariableValueSource;
import org.obiba.magma.support.ValueSetBean;
import org.obiba.magma.support.VariableEntityBean;
import org.obiba.magma.type.DateType;
import org.obiba.magma.type.TextType;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SuppressWarnings({ "PMD.NcssMethodCount", "OverlyLongMethod" })
public class JavascriptVariableValueSourceTest extends AbstractJsTest {

  @Test
  public void testVariableLookup() {
    // Build the javascript variable that returns AnotherVariable's value
    Variable variable = Variable.Builder.newVariable("JavascriptVariable", TextType.get(), "Participant")
        .extend(JavascriptVariableBuilder.class).setScript("$('AnotherVariable')").build();

    // Create the VariableValueSource for AnotherVariable
    Variable anotherVariable = Variable.Builder.newVariable("AnotherVariable", TextType.get(), "Participant").build();

    VariableValueSource mockSource = mock(VariableValueSource.class);
    when(mockSource.getVariable()).thenReturn(anotherVariable);
    when(mockSource.getValue(any(ValueSet.class))).thenReturn(TextType.get().valueOf("The Value"));

    ValueTable mockTable = mock(ValueTable.class);
    ValueSet valueSet = new ValueSetBean(mockTable, new VariableEntityBean("Participant", "1234"));
    when(mockTable.getName()).thenReturn("my-table");
    when(mockTable.getDatasource()).thenReturn(null);
    when(mockTable.getVariableValueSource("AnotherVariable")).thenReturn(mockSource);
    when(mockTable.getValueSet(any(VariableEntity.class))).thenReturn(valueSet);

    Timestamps tableTimestamps = mock(Timestamps.class);
    when(tableTimestamps.getLastUpdate()).thenReturn(DateType.get().valueOf(new MagmaDate(new Date())));
    when(mockTable.getTimestamps()).thenReturn(tableTimestamps);

    JavascriptVariableValueSource source = new JavascriptVariableValueSource(variable, mockTable);
    source.initialise();
    Value value = source.getValue(valueSet);

    assertThat(value).isNotNull();
    assertThat(value.isNull()).isFalse();
    assertThat(value.toString()).isEqualTo("The Value");
  }

  @Test
  public void test_relativeReference() {

    // Build the javascript variable that returns AnotherVariable's value
    Variable variable = Variable.Builder.newVariable("JavascriptVariable", TextType.get(), "Participant")
        .extend(JavascriptVariableBuilder.class).setScript("$('anotherTable:AnotherVariable')").build();

    // Create the VariableValueSource for AnotherVariable
    Variable anotherVariable = Variable.Builder.newVariable("AnotherVariable", TextType.get(), "Participant").build();

    VariableValueSource mockSource = mock(VariableValueSource.class);
    when(mockSource.getVariable()).thenReturn(anotherVariable);
    when(mockSource.getValue(any(ValueSet.class))).thenReturn(TextType.get().valueOf("The Value"));

    Datasource mockDatasource = mock(Datasource.class);
    ValueTable mockTable = mock(ValueTable.class);
    ValueSet valueSet = new ValueSetBean(mockTable, new VariableEntityBean("Participant", "1234"));
    when(mockTable.getName()).thenReturn("my-table");
    when(mockTable.getDatasource()).thenReturn(mockDatasource);

    Timestamps tableTimestamps = mock(Timestamps.class);
    when(tableTimestamps.getLastUpdate()).thenReturn(DateType.get().valueOf(new MagmaDate(new Date())));
    when(mockTable.getTimestamps()).thenReturn(tableTimestamps);

    ValueTable mockTable2 = mock(ValueTable.class);
    when(mockDatasource.getValueTable("anotherTable")).thenReturn(mockTable2);
    when(mockTable2.getVariableValueSource("AnotherVariable")).thenReturn(mockSource);
    when(mockTable2.getValueSet(any(VariableEntity.class))).thenReturn(valueSet);

    JavascriptVariableValueSource source = new JavascriptVariableValueSource(variable, mockTable);
    source.initialise();
    Value value = source.getValue(valueSet);
    assertThat(value).isNotNull();
    assertThat(value.isNull()).isFalse();
    assertThat(value.toString()).isEqualTo("The Value");
  }

}
