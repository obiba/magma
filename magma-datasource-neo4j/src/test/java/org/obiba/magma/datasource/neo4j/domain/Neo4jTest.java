/*
 * Copyright (c) 2013 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.magma.datasource.neo4j.domain;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.obiba.magma.datasource.neo4j.repository.DatasourceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.neo4j.support.Neo4jTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/application-context-test-neo4j.xml")
@Transactional
public class Neo4jTest {

  private static final Logger log = LoggerFactory.getLogger(Neo4jTest.class);

  @Autowired
  private Neo4jTemplate template;

  @Autowired
  private DatasourceRepository datasourceRepository;

  @Test
  public void persistedDatasourceShouldBeRetrievableFromGraphDb() {
    DatasourceNode datasource = template.save(new DatasourceNode("ds1"));
    DatasourceNode retrievedDatasource = template.findOne(datasource.getGraphId(), DatasourceNode.class);
    assertThat(datasource, is(retrievedDatasource));
  }

  @Test
  public void canFindDatasourceByName() {
    DatasourceNode datasource = template.save(new DatasourceNode("ds1"));
    DatasourceNode retrievedDatasource = datasourceRepository.findByName("ds1");
    assertThat(datasource, is(retrievedDatasource));
  }

  @Test
  public void canAddTablesToDatasource() {
    DatasourceNode datasource = template.save(new DatasourceNode("ds1"));

    ValueTableNode transientTable = new ValueTableNode();
    transientTable.setName("table1");
    transientTable.setEntityType("Participant");
    transientTable.setDatasource(datasource);
    ValueTableNode table = template.save(transientTable);

    ValueTableNode retrievedTable = template.findOne(table.getGraphId(), ValueTableNode.class);
    assertThat(table, is(retrievedTable));
    assertThat(datasource, is(retrievedTable.getDatasource()));

    DatasourceNode retrievedDatasource = template.findOne(datasource.getGraphId(), DatasourceNode.class);
    assertThat(retrievedDatasource.getValueTables().contains(retrievedTable), is(true));
  }

}
