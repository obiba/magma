package org.obiba.magma.js;

import org.easymock.EasyMock;
import org.junit.Assert;
import org.junit.Test;
import org.obiba.magma.Value;
import org.obiba.magma.ValueSet;
import org.obiba.magma.ValueTable;
import org.obiba.magma.Variable;
import org.obiba.magma.VariableEntity;
import org.obiba.magma.VariableValueSource;
import org.obiba.magma.support.ValueSetBean;
import org.obiba.magma.support.VariableEntityBean;
import org.obiba.magma.type.TextType;

public class JavascriptVariableValueSourceTest extends AbstractJsTest {

  @Test
  public void testVariableLookup() {
    // Build the javascript variable that returns AnotherVariable's value
    Variable.Builder builder = Variable.Builder.newVariable("my-table", "JavascriptVariable", TextType.get(), "Participant");
    Variable variable = builder.extend(JavascriptVariableBuilder.class).setScript("$('AnotherVariable')").build();
    JavascriptVariableValueSource source = new JavascriptVariableValueSource(variable);

    source.initialise();

    // Create the VariableValueSource for AnotherVariable
    Variable anotherVariable = Variable.Builder.newVariable("my-table", "AnotherVariable", TextType.get(), "Participant").build();

    VariableValueSource mockSource = EasyMock.createMock(VariableValueSource.class);
    EasyMock.expect(mockSource.getVariable()).andReturn(anotherVariable).anyTimes();
    EasyMock.expect(mockSource.getValue((ValueSet) EasyMock.anyObject())).andReturn(TextType.get().valueOf("The Value")).anyTimes();

    ValueTable mockTable = EasyMock.createMock(ValueTable.class);
    ValueSet valueSet = new ValueSetBean(mockTable, new VariableEntityBean("Participant", "1234"));
    EasyMock.expect(mockTable.getName()).andReturn("my-table").anyTimes();
    EasyMock.expect(mockTable.getVariableValueSource("AnotherVariable")).andReturn(mockSource).anyTimes();
    EasyMock.expect(mockTable.getValueSet((VariableEntity) EasyMock.anyObject())).andReturn(valueSet).anyTimes();

    EasyMock.replay(mockSource, mockTable);
    Value value = source.getValue(valueSet);

    Assert.assertNotNull(value);
    Assert.assertFalse(value.isNull());
    Assert.assertEquals("The Value", value.toString());
  }

}
