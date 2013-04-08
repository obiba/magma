package org.obiba.magma.support;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

public class MagmaEngineTableResolverTest {

  @Test
  public void testValueOfQualifiedTableName() throws Exception {
    MagmaEngineTableResolver resolver = MagmaEngineTableResolver.valueOf("ironman.Participant");
    assertThat(resolver.getDatasourceName(), is("ironman"));
    assertThat(resolver.getTableName(), is("Participant"));
  }

  @Test
  public void testValueOfTableName() throws Exception {
    MagmaEngineTableResolver resolver = MagmaEngineTableResolver.valueOf("Participant");
    assertThat(resolver.getDatasourceName(), is(nullValue()));
    assertThat(resolver.getTableName(), is("Participant"));
  }

}
