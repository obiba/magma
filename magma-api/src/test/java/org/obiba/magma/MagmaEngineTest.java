/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.magma;

import org.easymock.EasyMock;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.fest.assertions.api.Assertions.assertThat;

public class MagmaEngineTest {

  private MagmaEngine magmaEngine;

  @Before
  public void setUp() throws Exception {
    new MagmaEngine();
    magmaEngine = MagmaEngine.get();
  }

  @After
  public void stopMagmaEngine() {
    MagmaEngine.get().shutdown();
  }

  @Test
  public void testAddingTwoUniqueDatasources() throws Exception {
    Datasource datasourceOne = createMock(Datasource.class);
    Datasource datasourceTwo = createMock(Datasource.class);
    datasourceOne.initialise();
    expect(datasourceOne.getName()).andReturn("one").anyTimes();
    datasourceTwo.initialise();
    expect(datasourceTwo.getName()).andReturn("two").anyTimes();
    datasourceOne.dispose();
    datasourceTwo.dispose();
    replay(datasourceOne, datasourceTwo);
    magmaEngine.addDatasource(datasourceOne);
    magmaEngine.addDatasource(datasourceTwo);
    assertThat(magmaEngine.getDatasources()).hasSize(2);
    magmaEngine.shutdown();
    verify(datasourceOne, datasourceTwo);
  }

  /**
   * Subsequent requests to add a Datasource are ignored. The Datasource is already present in the MagmaEngine.
   */
  @Test
  public void testAddingNonUniqueDatasources() throws Exception {
    Datasource datasourceOne = createMock(Datasource.class);
    expect(datasourceOne.getName()).andReturn("one").anyTimes();
    datasourceOne.initialise();
    expectLastCall().times(1);
    datasourceOne.dispose();
    replay(datasourceOne);
    magmaEngine.addDatasource(datasourceOne);
    magmaEngine.addDatasource(datasourceOne);
    assertThat(magmaEngine.getDatasources()).hasSize(1);
    magmaEngine.shutdown();
    verify(datasourceOne);
  }

  @Test(expected = DuplicateDatasourceNameException.class)
  public void testAddingUniqueDatasourcesWithNonUniqueNames() throws Exception {
    Datasource datasourceOne = createMock(Datasource.class);
    Datasource datasourceTwo = createMock(Datasource.class);
    datasourceOne.initialise();
    expect(datasourceOne.getName()).andReturn("fireball").anyTimes();
    expect(datasourceTwo.getName()).andReturn("fireball").anyTimes();
    datasourceOne.dispose();
    replay(datasourceOne, datasourceTwo);
    magmaEngine.addDatasource(datasourceOne);
    try {
      magmaEngine.addDatasource(datasourceTwo);
    } finally {
      assertThat(magmaEngine.getDatasources()).hasSize(1);
      magmaEngine.shutdown();
      verify(datasourceOne, datasourceTwo);
    }
  }

  @Test
  public void testHasTransientDatasourceIsTrue() throws Exception {
    DatasourceFactory factory = createMock(DatasourceFactory.class);
    expect(factory.getName()).andReturn("uid").atLeastOnce();
    factory.setName((String) EasyMock.anyObject());

    replay(factory);
    String uid = magmaEngine.addTransientDatasource(factory);
    assertThat(magmaEngine.hasTransientDatasource(uid)).isTrue();
    assertThat(magmaEngine.hasTransientDatasource("foo")).isFalse();
    magmaEngine.removeTransientDatasource(uid);
    assertThat(magmaEngine.hasTransientDatasource(uid)).isFalse();
    magmaEngine.shutdown();
    verify(factory);
  }

  @Test
  public void testGetTransientDatasourceInstanceIsInitialised() throws Exception {
    DatasourceFactory factory = createMock(DatasourceFactory.class);
    Datasource datasource = createMock(Datasource.class);
    expect(factory.getName()).andReturn("uid").atLeastOnce();
    expect(factory.create()).andReturn(datasource).once();
    expect(datasource.getName()).andReturn("uid").once();
    factory.setName((String) EasyMock.anyObject());
    datasource.initialise();

    replay(factory, datasource);
    String uid = magmaEngine.addTransientDatasource(factory);
    Datasource created = magmaEngine.getTransientDatasourceInstance(uid);
    assertThat(created).isNotNull();
    assertThat(created.getName()).isEqualTo(uid);
    magmaEngine.shutdown();
    verify(factory, datasource);
  }

  @Test(expected = NoSuchDatasourceException.class)
  public void testGetNonExistingTransientDatasourceInstance() throws Exception {
    try {
      magmaEngine.getTransientDatasourceInstance("foo");
      magmaEngine.shutdown();
      Assert.fail();
    } finally {
      magmaEngine.shutdown();
    }
  }

  @Test(expected = NoSuchDatasourceException.class)
  public void testGetNonExistingDatasource() throws Exception {
    try {
      magmaEngine.getDatasource("pwel");
      magmaEngine.shutdown();
      Assert.fail();
    } finally {
      magmaEngine.shutdown();
    }
  }

  @Test
  public void testRemoveNonExistingTransientDatasourceIsSilent() throws Exception {
    magmaEngine.removeTransientDatasource("foo");
    magmaEngine.shutdown();
  }

  @Test(expected = IllegalArgumentException.class)
  public void testGetTransientDatasourceWithNullName() throws Exception {
    try {
      magmaEngine.getTransientDatasourceInstance(null);
      Assert.fail();
    } finally {
      magmaEngine.shutdown();
    }
  }

  @Test(expected = IllegalArgumentException.class)
  public void testGetDatasourceWithNullName() throws Exception {
    try {
      magmaEngine.getDatasource(null);
      magmaEngine.shutdown();
      Assert.fail();
    } finally {
      magmaEngine.shutdown();
    }
  }
}
