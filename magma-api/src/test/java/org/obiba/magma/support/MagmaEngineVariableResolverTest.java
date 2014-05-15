package org.obiba.magma.support;

import org.junit.Test;

import static org.fest.assertions.api.Assertions.assertThat;

public class MagmaEngineVariableResolverTest {

  @Test
  public void testValueOfQualifiedTableName() throws Exception {
    MagmaEngineVariableResolver resolver = MagmaEngineVariableResolver.valueOf("ironman.Participant:");
    assertThat(resolver.getDatasourceName()).isEqualTo("ironman");
    assertThat(resolver.getTableName()).isEqualTo("Participant");
    assertThat(resolver.getVariableName()).isNull();
  }

  @Test
  public void testValueOfVariableName() throws Exception {
    MagmaEngineVariableResolver resolver = MagmaEngineVariableResolver.valueOf("SMOKER_STATUS");
    assertThat(resolver.getDatasourceName()).isNull();
    assertThat(resolver.getTableName()).isNull();
    assertThat(resolver.getVariableName()).isEqualTo("SMOKER_STATUS");
  }

  @Test
  public void testValueOfTableAndVariableName() throws Exception {
    MagmaEngineVariableResolver resolver = MagmaEngineVariableResolver.valueOf("Participant:SMOKER_STATUS");
    assertThat(resolver.getDatasourceName()).isNull();
    assertThat(resolver.getTableName()).isEqualTo("Participant");
    assertThat(resolver.getVariableName()).isEqualTo("SMOKER_STATUS");
  }

  @Test
  public void testValueOfDatasourceAndTableAndVariableName() throws Exception {
    MagmaEngineVariableResolver resolver = MagmaEngineVariableResolver.valueOf("ironman.Participant:SMOKER_STATUS");
    assertThat(resolver.getDatasourceName()).isEqualTo("ironman");
    assertThat(resolver.getTableName()).isEqualTo("Participant");
    assertThat(resolver.getVariableName()).isEqualTo("SMOKER_STATUS");
  }
}
