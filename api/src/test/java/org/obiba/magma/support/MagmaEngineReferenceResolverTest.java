package org.obiba.magma.support;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Test;

public class MagmaEngineReferenceResolverTest {

  @Before
  public void setUp() throws Exception {
  }

  @Test
  public void testValueOfQualifiedTableName() throws Exception {
    MagmaEngineReferenceResolver resolver = MagmaEngineReferenceResolver.valueOf("ironman.Participant:");
    assertThat(resolver.getDatasourceName(), is("ironman"));
    assertThat(resolver.getTableName(), is("Participant"));
    assertThat(resolver.getVariableName(), is(nullValue()));
  }

  @Test
  public void testValueOfVariableName() throws Exception {
    MagmaEngineReferenceResolver resolver = MagmaEngineReferenceResolver.valueOf("SMOKER_STATUS");
    assertThat(resolver.getDatasourceName(), is(nullValue()));
    assertThat(resolver.getTableName(), is(nullValue()));
    assertThat(resolver.getVariableName(), is("SMOKER_STATUS"));
  }

  @Test
  public void testValueOfTableAndVariableName() throws Exception {
    MagmaEngineReferenceResolver resolver = MagmaEngineReferenceResolver.valueOf("Participant:SMOKER_STATUS");
    assertThat(resolver.getDatasourceName(), is(nullValue()));
    assertThat(resolver.getTableName(), is("Participant"));
    assertThat(resolver.getVariableName(), is("SMOKER_STATUS"));
  }

  @Test
  public void testValueOfDatasourceAndTableAndVariableName() throws Exception {
    MagmaEngineReferenceResolver resolver = MagmaEngineReferenceResolver.valueOf("ironman.Participant:SMOKER_STATUS");
    assertThat(resolver.getDatasourceName(), is("ironman"));
    assertThat(resolver.getTableName(), is("Participant"));
    assertThat(resolver.getVariableName(), is("SMOKER_STATUS"));
  }
}
