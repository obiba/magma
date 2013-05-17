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
import org.obiba.magma.type.TextType;
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

  public static final String DS_NAME = "ds1";

  public static final String TABLE_NAME = "table1";

  public static final String PARTICIPANT = "Participant";

  public static final String VAR_NAME = "var1";

  @Autowired
  private Neo4jTemplate template;

  @Autowired
  private DatasourceRepository datasourceRepository;

  @Test
  public void persistedDatasourceShouldBeRetrievableFromGraphDb() {
    DatasourceNode datasource = template.save(new DatasourceNode(DS_NAME));
    DatasourceNode retrievedDatasource = template.findOne(datasource.getGraphId(), DatasourceNode.class);
    assertThat(datasource, is(retrievedDatasource));
  }

  @Test
  public void canFindDatasourceByName() {
    DatasourceNode datasource = template.save(new DatasourceNode(DS_NAME));
    DatasourceNode retrievedDatasource = datasourceRepository.findByName(DS_NAME);
    assertThat(datasource, is(retrievedDatasource));
  }

  @Test
  public void canAddTablesToDatasource() {
    DatasourceNode datasource = template.save(new DatasourceNode(DS_NAME));

    ValueTableNode transientTable = new ValueTableNode();
    transientTable.setName(TABLE_NAME);
    transientTable.setEntityType(PARTICIPANT);
    transientTable.setDatasource(datasource);
    ValueTableNode table = template.save(transientTable);

    ValueTableNode retrievedTable = template.findOne(table.getGraphId(), ValueTableNode.class);
    assertThat(table, is(retrievedTable));
    assertThat(datasource, is(retrievedTable.getDatasource()));

    DatasourceNode retrievedDatasource = template.findOne(datasource.getGraphId(), DatasourceNode.class);
    assertThat(retrievedDatasource.getValueTables().contains(retrievedTable), is(true));
  }

  @Test
  public void canAddVariablesToTable() {
    DatasourceNode datasource = template.save(new DatasourceNode(DS_NAME));
    ValueTableNode table = createTable(datasource);
    VariableNode variable = createVariable(table);
    VariableNode retrievedVariable = template.findOne(variable.getGraphId(), VariableNode.class);
    assertThat(variable, is(retrievedVariable));
    assertThat(table, is(retrievedVariable.getValueTable()));
  }

  @Test
  public void canAddValue() {
    DatasourceNode datasource = template.save(new DatasourceNode(DS_NAME));
    ValueTableNode table = createTable(datasource);
    VariableNode variable = createVariable(table);
    VariableEntityNode entity = template.save(new VariableEntityNode("1", PARTICIPANT));
    ValueNode value = template.save(new ValueNode(TextType.Factory.newValue("value1")));
    ValueSetNode valueSet = createValueSet(table, entity);
    ValueSetValueNode valueSetValue = createValueSetValue(variable, value, valueSet);

  }

  private ValueTableNode createTable(DatasourceNode datasource) {
    return template.save(new ValueTableNode(TABLE_NAME, PARTICIPANT, datasource));
  }

  private VariableNode createVariable(ValueTableNode table) {
    VariableNode transientVariable = new VariableNode();
    transientVariable.setName(VAR_NAME);
    transientVariable.setEntityType(PARTICIPANT);
    transientVariable.setValueTable(table);
    return template.save(transientVariable);
  }

  private ValueSetNode createValueSet(ValueTableNode table, VariableEntityNode entity) {
    ValueSetNode transientValueSet = new ValueSetNode();
    transientValueSet.setValueTable(table);
    transientValueSet.setVariableEntity(entity);
    return template.save(transientValueSet);
  }

  private ValueSetValueNode createValueSetValue(VariableNode variable, ValueNode value, ValueSetNode valueSet) {
    ValueSetValueNode transientValueSetValue = new ValueSetValueNode();
    transientValueSetValue.setValue(value);
    transientValueSetValue.setValueSet(valueSet);
    transientValueSetValue.setVariable(variable);
    return template.save(transientValueSetValue);
  }

}
