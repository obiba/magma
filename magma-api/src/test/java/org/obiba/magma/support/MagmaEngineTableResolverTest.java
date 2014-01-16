package org.obiba.magma.support;

import org.junit.Test;

import static org.fest.assertions.api.Assertions.assertThat;

public class MagmaEngineTableResolverTest {

  @Test
  public void testValueOfQualifiedTableName() throws Exception {
    MagmaEngineTableResolver resolver = MagmaEngineTableResolver.valueOf("ironman.Participant");
    assertThat(resolver.getDatasourceName()).isEqualTo("ironman");
    assertThat(resolver.getTableName()).isEqualTo("Participant");
  }

  @Test
  public void testValueOfTableName() throws Exception {
    MagmaEngineTableResolver resolver = MagmaEngineTableResolver.valueOf("Participant");
    assertThat(resolver.getDatasourceName()).isNull();
    assertThat(resolver.getTableName()).isEqualTo("Participant");
  }

}
