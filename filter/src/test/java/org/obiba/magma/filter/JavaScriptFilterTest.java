package org.obiba.magma.filter;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

import org.easymock.EasyMock;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.obiba.magma.MagmaEngine;
import org.obiba.magma.ValueSet;
import org.obiba.magma.ValueTable;
import org.obiba.magma.Variable;
import org.obiba.magma.VariableEntity;
import org.obiba.magma.VariableValueSource;
import org.obiba.magma.js.MagmaJsExtension;
import org.obiba.magma.support.ValueSetBean;
import org.obiba.magma.support.VariableEntityBean;
import org.obiba.magma.type.BooleanType;
import org.obiba.magma.type.TextType;

public class JavaScriptFilterTest {

  private ValueSet valueSetMock;

  @Before
  public void setUp() throws Exception {
    new MagmaEngine().extend(new MagmaJsExtension());
    valueSetMock = EasyMock.createMock(ValueSet.class);
  }

  @After
  public void cleanUp() throws Exception {
    MagmaEngine.get().shutdown();
  }

  @Test(expected = IllegalArgumentException.class)
  public void testJavaScriptNotAllowed() throws Exception {
    new JavaScriptFilter(null);
  }

  @Test
  public void testAttributeAndValueAllowed() throws Exception {
    new JavaScriptFilter("1;");
  }

  @Test
  public void testSimpleScriptReturnsTrue() throws Exception {
    JavaScriptFilter filter = JavaScriptFilter.Builder.newFilter().javascript("4 > 3;").include().build();
    assertThat(filter.runFilter(valueSetMock), is(true));
  }

  @Test
  public void testSimpleScriptReturnsFalse() throws Exception {
    JavaScriptFilter filter = JavaScriptFilter.Builder.newFilter().javascript("2 > 3;").include().build();
    assertThat(filter.runFilter(valueSetMock), is(false));
  }

  @Test
  public void testNullReturnValueSameAsFalse() throws Exception {
    JavaScriptFilter filter = JavaScriptFilter.Builder.newFilter().javascript("null;").exclude().build();
    assertThat(filter.runFilter(valueSetMock), nullValue());
  }

  @Test
  public void testScriptAnyReturnsTrue() throws Exception {
    JavaScriptFilter filter = JavaScriptFilter.Builder.newFilter().javascript("$('Admin.Interview.exported').any('TRUE')").include().build();

    Variable variable = Variable.Builder.newVariable("Admin.Interview.exported", BooleanType.get(), "Participant").build();

    VariableValueSource mockSource = EasyMock.createMock(VariableValueSource.class);
    EasyMock.expect(mockSource.getVariable()).andReturn(variable).anyTimes();
    EasyMock.expect(mockSource.getValue((ValueSet) EasyMock.anyObject())).andReturn(BooleanType.get().valueOf("TRUE")).anyTimes();

    ValueTable tableMock = EasyMock.createMock(ValueTable.class);
    ValueSet valueSet = new ValueSetBean(tableMock, new VariableEntityBean("Participant", "1234"));
    EasyMock.expect(tableMock.getName()).andReturn("collectionName").anyTimes();
    EasyMock.expect(tableMock.getVariableValueSource("Admin.Interview.exported")).andReturn(mockSource).anyTimes();
    EasyMock.expect(tableMock.getValueSet((VariableEntity) EasyMock.anyObject())).andReturn(valueSet).anyTimes();
    EasyMock.replay(mockSource, tableMock);

    assertThat(filter.runFilter(valueSet), is(true));
  }

  @Test
  public void testScriptAnyReturnsFalse() throws Exception {
    JavaScriptFilter filter = JavaScriptFilter.Builder.newFilter().javascript("$('Admin.Interview.exported').any('FALSE')").include().build();

    Variable variable = Variable.Builder.newVariable("Admin.Interview.exported", BooleanType.get(), "Participant").build();

    VariableValueSource mockSource = EasyMock.createMock(VariableValueSource.class);
    EasyMock.expect(mockSource.getVariable()).andReturn(variable).anyTimes();
    EasyMock.expect(mockSource.getValue((ValueSet) EasyMock.anyObject())).andReturn(BooleanType.get().valueOf("TRUE")).anyTimes();

    ValueTable tableMock = EasyMock.createMock(ValueTable.class);
    ValueSet valueSet = new ValueSetBean(tableMock, new VariableEntityBean("Participant", "1234"));
    EasyMock.expect(tableMock.getName()).andReturn("collectionName").anyTimes();
    EasyMock.expect(tableMock.getVariableValueSource("Admin.Interview.exported")).andReturn(mockSource).anyTimes();
    EasyMock.expect(tableMock.getValueSet((VariableEntity) EasyMock.anyObject())).andReturn(valueSet).anyTimes();
    EasyMock.replay(mockSource, tableMock);

    assertThat(filter.runFilter(valueSet), is(false));
  }

  @Test
  public void testScriptAnyMultipleReturnsTrue() throws Exception {
    JavaScriptFilter filter = JavaScriptFilter.Builder.newFilter().javascript("$('Participant.Interview.status').any('CANCELED','CLOSED')").include().build();

    Variable variable = Variable.Builder.newVariable("Admin.Interview.exported", BooleanType.get(), "Participant").build();

    VariableValueSource mockSource = EasyMock.createMock(VariableValueSource.class);
    EasyMock.expect(mockSource.getVariable()).andReturn(variable).anyTimes();
    EasyMock.expect(mockSource.getValue((ValueSet) EasyMock.anyObject())).andReturn(TextType.get().valueOf("CLOSED")).anyTimes();

    ValueTable tableMock = EasyMock.createMock(ValueTable.class);
    ValueSet valueSet = new ValueSetBean(tableMock, new VariableEntityBean("Participant", "1234"));
    EasyMock.expect(tableMock.getName()).andReturn("collectionName").anyTimes();
    EasyMock.expect(tableMock.getVariableValueSource("Participant.Interview.status")).andReturn(mockSource).anyTimes();
    EasyMock.expect(tableMock.getValueSet((VariableEntity) EasyMock.anyObject())).andReturn(valueSet).anyTimes();
    EasyMock.replay(mockSource, tableMock);

    assertThat(filter.runFilter(valueSet), is(true));
  }

  @Test
  public void testScriptAnyMultipleReturnsFalse() throws Exception {
    JavaScriptFilter filter = JavaScriptFilter.Builder.newFilter().javascript("$('Participant.Interview.status').any('CANCELED','CLOSED')").include().build();

    Variable variable = Variable.Builder.newVariable("Admin.Interview.exported", BooleanType.get(), "Participant").build();

    VariableValueSource mockSource = EasyMock.createMock(VariableValueSource.class);
    EasyMock.expect(mockSource.getVariable()).andReturn(variable).anyTimes();
    EasyMock.expect(mockSource.getValue((ValueSet) EasyMock.anyObject())).andReturn(TextType.get().valueOf("IN_PROGRESS")).anyTimes();

    ValueTable tableMock = EasyMock.createMock(ValueTable.class);
    ValueSet valueSet = new ValueSetBean(tableMock, new VariableEntityBean("Participant", "1234"));
    EasyMock.expect(tableMock.getName()).andReturn("collectionName").anyTimes();
    EasyMock.expect(tableMock.getVariableValueSource("Participant.Interview.status")).andReturn(mockSource).anyTimes();
    EasyMock.expect(tableMock.getValueSet((VariableEntity) EasyMock.anyObject())).andReturn(valueSet).anyTimes();
    EasyMock.replay(mockSource, tableMock);

    assertThat(filter.runFilter(valueSet), is(false));
  }

  @Test
  public void testScriptNotEqualReturnsTrue() throws Exception {
    JavaScriptFilter filter = JavaScriptFilter.Builder.newFilter().javascript("$('Participant.Interview.status').not('CANCELED')").include().build();

    Variable variable = Variable.Builder.newVariable("Admin.Interview.exported", BooleanType.get(), "Participant").build();

    VariableValueSource mockSource = EasyMock.createMock(VariableValueSource.class);
    EasyMock.expect(mockSource.getVariable()).andReturn(variable).anyTimes();
    EasyMock.expect(mockSource.getValue((ValueSet) EasyMock.anyObject())).andReturn(TextType.get().valueOf("IN_PROGRESS")).anyTimes();

    ValueTable tableMock = EasyMock.createMock(ValueTable.class);
    ValueSet valueSet = new ValueSetBean(tableMock, new VariableEntityBean("Participant", "1234"));
    EasyMock.expect(tableMock.getName()).andReturn("collectionName").anyTimes();
    EasyMock.expect(tableMock.getVariableValueSource("Participant.Interview.status")).andReturn(mockSource).anyTimes();
    EasyMock.expect(tableMock.getValueSet((VariableEntity) EasyMock.anyObject())).andReturn(valueSet).anyTimes();
    EasyMock.replay(mockSource, tableMock);

    assertThat(filter.runFilter(valueSet), is(true));
  }

  @Test
  public void testScriptNotEqualReturnsFalse() throws Exception {
    JavaScriptFilter filter = JavaScriptFilter.Builder.newFilter().javascript("$('Participant.Interview.status').not('IN_PROGRESS')").include().build();

    Variable variable = Variable.Builder.newVariable("Admin.Interview.exported", BooleanType.get(), "Participant").build();

    VariableValueSource mockSource = EasyMock.createMock(VariableValueSource.class);
    EasyMock.expect(mockSource.getVariable()).andReturn(variable).anyTimes();
    EasyMock.expect(mockSource.getValue((ValueSet) EasyMock.anyObject())).andReturn(TextType.get().valueOf("IN_PROGRESS")).anyTimes();

    ValueTable tableMock = EasyMock.createMock(ValueTable.class);
    ValueSet valueSet = new ValueSetBean(tableMock, new VariableEntityBean("Participant", "1234"));
    EasyMock.expect(tableMock.getName()).andReturn("collectionName").anyTimes();
    EasyMock.expect(tableMock.getVariableValueSource("Participant.Interview.status")).andReturn(mockSource).anyTimes();
    EasyMock.expect(tableMock.getValueSet((VariableEntity) EasyMock.anyObject())).andReturn(valueSet).anyTimes();
    EasyMock.replay(mockSource, tableMock);

    assertThat(filter.runFilter(valueSet), is(false));
  }

}
