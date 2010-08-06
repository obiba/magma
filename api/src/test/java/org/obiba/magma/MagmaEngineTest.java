package org.obiba.magma;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import junit.framework.Assert;

import org.junit.Test;

public class MagmaEngineTest {

  private MagmaEngine magmaEngine;

  public void setUp() throws Exception {
    new MagmaEngine();
    magmaEngine = MagmaEngine.get();
  }

  public void setUpForTansient() throws Exception {
    new MagmaEngine() {
      @Override
      String randomTransientDatasourceName() {
        return "pwel";
      }
    };
    magmaEngine = MagmaEngine.get();
  }

  @Test
  public void testAddingTwoUniqueDatasources() throws Exception {
    setUp();
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
    setUp();
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
    setUp();
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

  @Test
  public void testHasTransientDatasourceIsTrue() throws Exception {
    setUpForTansient();
    DatasourceFactory factory = createMock(DatasourceFactory.class);
    expect(factory.getName()).andReturn("pwel").atLeastOnce();
    factory.setName("pwel");

    replay(factory);
    magmaEngine.addTransientDatasource(factory);
    assertThat(magmaEngine.hasTransientDatasource("pwel"), is(true));
    assertThat(magmaEngine.hasTransientDatasource("foo"), is(false));
    magmaEngine.removeTransientDatasource("pwel");
    assertThat(magmaEngine.hasTransientDatasource("pwel"), is(false));
    magmaEngine.shutdown();
    verify(factory);
  }

  @Test
  public void testGetTransientDatasourceInstanceIsInitialised() throws Exception {
    setUpForTansient();
    DatasourceFactory factory = createMock(DatasourceFactory.class);
    Datasource datasource = createMock(Datasource.class);
    expect(factory.getName()).andReturn("pwel").atLeastOnce();
    expect(factory.create()).andReturn(datasource).once();
    expect(datasource.getName()).andReturn("pwel").once();
    factory.setName("pwel");
    datasource.initialise();

    replay(factory, datasource);
    magmaEngine.addTransientDatasource(factory);
    Datasource created = magmaEngine.getTransientDatasourceInstance("pwel");
    Assert.assertNotNull(created);
    assertThat(created.getName(), is("pwel"));
    magmaEngine.shutdown();
    verify(factory, datasource);
  }

  @Test(expected = NoSuchDatasourceException.class)
  public void testGetNonExistingTransientDatasourceInstance() throws Exception {
    setUpForTansient();
    try {
      magmaEngine.getTransientDatasourceInstance("pwel");
      magmaEngine.shutdown();
      Assert.fail();
    } catch(Exception e) {
      magmaEngine.shutdown();
      throw e;
    }
  }

  @Test(expected = NoSuchDatasourceException.class)
  public void testGetNonExistingDatasource() throws Exception {
    setUp();
    try {
      magmaEngine.getDatasource("pwel");
      magmaEngine.shutdown();
      Assert.fail();
    } catch(Exception e) {
      magmaEngine.shutdown();
      throw e;
    }
  }

  @Test
  public void testRemoveNonExistingTransientDatasourceIsSilent() throws Exception {
    setUpForTansient();
    magmaEngine.removeTransientDatasource("pwel");
    magmaEngine.shutdown();
  }

  @Test(expected = IllegalArgumentException.class)
  public void testGetTransientDatasourceWithNullName() throws Exception {
    setUpForTansient();
    try {
      magmaEngine.getTransientDatasourceInstance(null);
      magmaEngine.shutdown();
      Assert.fail();
    } catch(Exception e) {
      magmaEngine.shutdown();
      throw e;
    }
  }

  @Test(expected = IllegalArgumentException.class)
  public void testGetDatasourceWithNullName() throws Exception {
    setUp();
    try {
      magmaEngine.getDatasource(null);
      magmaEngine.shutdown();
      Assert.fail();
    } catch(Exception e) {
      magmaEngine.shutdown();
      throw e;
    }
  }
}
