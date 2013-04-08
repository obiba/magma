package org.obiba.magma.support;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

public class MagmaEngineVariableResolverTest {

  @Test
  public void testValueOfQualifiedTableName() throws Exception {
    MagmaEngineVariableResolver resolver = MagmaEngineVariableResolver.valueOf("ironman.Participant:");
    assertThat(resolver.getDatasourceName(), is("ironman"));
    assertThat(resolver.getTableName(), is("Participant"));
    assertThat(resolver.getVariableName(), is(nullValue()));
  }

  @Test
  public void testValueOfVariableName() throws Exception {
    MagmaEngineVariableResolver resolver = MagmaEngineVariableResolver.valueOf("SMOKER_STATUS");
    assertThat(resolver.getDatasourceName(), is(nullValue()));
    assertThat(resolver.getTableName(), is(nullValue()));
    assertThat(resolver.getVariableName(), is("SMOKER_STATUS"));
  }

  @Test
  public void testValueOfTableAndVariableName() throws Exception {
    MagmaEngineVariableResolver resolver = MagmaEngineVariableResolver.valueOf("Participant:SMOKER_STATUS");
    assertThat(resolver.getDatasourceName(), is(nullValue()));
    assertThat(resolver.getTableName(), is("Participant"));
    assertThat(resolver.getVariableName(), is("SMOKER_STATUS"));
  }

  @Test
  public void testValueOfDatasourceAndTableAndVariableName() throws Exception {
    MagmaEngineVariableResolver resolver = MagmaEngineVariableResolver.valueOf("ironman.Participant:SMOKER_STATUS");
    assertThat(resolver.getDatasourceName(), is("ironman"));
    assertThat(resolver.getTableName(), is("Participant"));
    assertThat(resolver.getVariableName(), is("SMOKER_STATUS"));
  }
}
