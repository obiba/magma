package org.obiba.magma;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Test;

public class MagmaEngineTest {

  private MagmaEngine magmaEngine;

  @Before
  public void setUp() throws Exception {
    new MagmaEngine();
    magmaEngine = MagmaEngine.get();
  }

  @Test
  public void testAddingTwoUniqueDatasources() throws Exception {
    Datasource datasourceOne = createMock(Datasource.class);
    Datasource datasourceTwo = createMock(Datasource.class);
    datasourceOne.initialise();
    expect(datasourceOne.getName()).andReturn("one");
    datasourceTwo.initialise();
    expect(datasourceTwo.getName()).andReturn("two");
    datasourceOne.dispose();
    datasourceTwo.dispose();
    replay(datasourceOne, datasourceTwo);
    magmaEngine.addDatasource(datasourceOne);
    magmaEngine.addDatasource(datasourceTwo);
    assertThat(magmaEngine.getDatasources().size(), is(2));
    magmaEngine.shutdown();
    verify(datasourceOne, datasourceTwo);
  }

  /**
   * Subsequent requests to add a Datasource are ignored. The Datasource is already present in the MagmaEngine.
   */
  @Test
  public void testAddingNonUniqueDatasources() throws Exception {
    Datasource datasourceOne = createMock(Datasource.class);
    datasourceOne.initialise();
    expectLastCall().times(2);
    datasourceOne.dispose();
    replay(datasourceOne);
    magmaEngine.addDatasource(datasourceOne);
    magmaEngine.addDatasource(datasourceOne);
    assertThat(magmaEngine.getDatasources().size(), is(1));
    magmaEngine.shutdown();
    verify(datasourceOne);
  }

  @Test(expected = DuplicateDatasourceNameException.class)
  public void testAddingUniqueDatasourcesWithNonUniqueNames() throws Exception {
    Datasource datasourceOne = createMock(Datasource.class);
    Datasource datasourceTwo = createMock(Datasource.class);
    datasourceOne.initialise();
    expect(datasourceOne.getName()).andReturn("fireball").times(2);
    datasourceTwo.initialise();
    expect(datasourceTwo.getName()).andReturn("fireball");
    datasourceOne.dispose();
    replay(datasourceOne, datasourceTwo);
    magmaEngine.addDatasource(datasourceOne);
    try {
      magmaEngine.addDatasource(datasourceTwo);
    } finally {
      assertThat(magmaEngine.getDatasources().size(), is(1));
      magmaEngine.shutdown();
      verify(datasourceOne, datasourceTwo);
    }
  }
}
