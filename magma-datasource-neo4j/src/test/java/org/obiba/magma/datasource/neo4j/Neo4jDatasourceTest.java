/*
 * Copyright (c) 2013 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.magma.datasource.neo4j;

import java.util.Date;

import javax.annotation.Resource;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.obiba.magma.MagmaEngine;
import org.obiba.magma.Value;
import org.obiba.magma.ValueSet;
import org.obiba.magma.ValueTable;
import org.obiba.magma.ValueTableWriter;
import org.obiba.magma.Variable;
import org.obiba.magma.VariableEntity;
import org.obiba.magma.datasource.neo4j.domain.AttributeNode;
import org.obiba.magma.datasource.neo4j.domain.DatasourceNode;
import org.obiba.magma.datasource.neo4j.domain.ValueTableNode;
import org.obiba.magma.support.VariableEntityBean;
import org.obiba.magma.type.IntegerType;
import org.obiba.magma.type.TextType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.data.neo4j.support.Neo4jTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.Iterables;

import junit.framework.Assert;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

@SuppressWarnings("OverlyLongMethod")
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/application-context-test-neo4j.xml")
@Transactional
public class Neo4jDatasourceTest {

  private static final Logger log = LoggerFactory.getLogger(Neo4jDatasourceTest.class);

  public static final String DS_NAME = "testDs";

  public static final String TABLE_NAME = "testTable";

  public static final String PARTICIPANT = "Participant";

  @Resource
  private ApplicationContext applicationContext;

  @SuppressWarnings("SpringJavaAutowiringInspection")
  @Resource
  private Neo4jTemplate neo4jTemplate;

  @Before
  public void startYourEngine() {
    MagmaEngine.get();
  }

  @After
  public void stopYourEngine() {
    MagmaEngine.get().shutdown();
  }

  private Neo4jDatasource createDatasource() {
    return (Neo4jDatasource) new Neo4jDatasourceFactory(DS_NAME, applicationContext).create();
  }

  @Test
  public void canPersistDatasource() throws Exception {
    Neo4jDatasource datasource = createDatasource();
    datasource.setAttributeValue("type", TextType.Factory.newValue("neo4j"));
    MagmaEngine.get().addDatasource(datasource);

    DatasourceNode datasourceNode = datasource.getNode();
    assertThat(datasourceNode.getName(), is(DS_NAME));
    assertThat(datasourceNode.getCreatedDate(), notNullValue());
    assertThat(datasourceNode.getLastModifiedDate(), notNullValue());

    AttributeNode attributeNode = datasourceNode.getAttribute("type", null);
    assertThat(attributeNode, notNullValue());

    neo4jTemplate.fetch(attributeNode.getValue());
    assertThat(attributeNode.getValue().getValue(), is("neo4j"));

    assertThat(datasource.getAttributeStringValue("type"), is("neo4j"));
  }

  @SuppressWarnings("ConstantConditions")
  @Test
  public void canPersistTables() throws Exception {

    Date now = new Date();

    Neo4jDatasource datasource = createDatasource();

    MagmaEngine.get().addDatasource(datasource);

    ValueTableWriter tableWriter = datasource.createWriter(TABLE_NAME, PARTICIPANT);
    tableWriter.close();

    assertThat(datasource.hasValueTable(TABLE_NAME), is(true));
    assertThat(datasource.getValueTableNames().contains(TABLE_NAME), is(true));

    Neo4jValueTable valueTable = (Neo4jValueTable) datasource.getValueTable(TABLE_NAME);
    assertThat(valueTable.getName(), is(TABLE_NAME));

    assertThat(valueTable.getTimestamps(), notNullValue());

    Date created = (Date) valueTable.getTimestamps().getCreated().getValue();
    assertThat(created, notNullValue());
    assertThat(created + " should be after " + now, created.after(now), is(true));

    Date lastUpdate = (Date) valueTable.getTimestamps().getLastUpdate().getValue();
    assertThat(lastUpdate, notNullValue());
    assertThat(lastUpdate + " should be after " + now, lastUpdate.after(now), is(true));

    ValueTableNode tableNode = valueTable.getNode();
    assertThat(tableNode.getName(), is(TABLE_NAME));
    assertThat(tableNode.getCreatedDate(), notNullValue());
    assertThat(tableNode.getLastModifiedDate(), notNullValue());

    assertThat(valueTable, notNullValue());
    assertThat(valueTable.getName(), is(TABLE_NAME));
    assertThat(valueTable.getTimestamps(), notNullValue());

    // Make sure the table is not visible outside this transaction.
    new TestThread() {
      @Override
      public void test() {
        // Assert that the datasource does not have the value table
        try {
          assertThat(MagmaEngine.get().getDatasource(DS_NAME).hasValueTable(TABLE_NAME), is(false));
          fail("Should throw NoSuchDatasourceException");
        } catch(Exception ignored) {
        }
      }
    };

  }

  @SuppressWarnings("ConstantConditions")
  @Test
  public void canPersistVariables() throws Exception {

    Neo4jDatasource datasource = createDatasource();
    MagmaEngine.get().addDatasource(datasource);
    ValueTableWriter tableWriter = datasource.createWriter(TABLE_NAME, PARTICIPANT);
    ValueTable valueTable = datasource.getValueTable(TABLE_NAME);

    Date timestampAtCreation = (Date) valueTable.getTimestamps().getLastUpdate().getValue();

    ValueTableWriter.VariableWriter variableWriter = tableWriter.writeVariables();

    Variable variable1 = Variable.Builder.newVariable("Var1", TextType.get(), PARTICIPANT).build();
    variableWriter.writeVariable(variable1);
    assertThat(Iterables.size(valueTable.getVariables()), is(1));
    assertThat(Iterables.contains(valueTable.getVariables(), variable1), is(true));
    Variable retrievedVariable1 = valueTable.getVariable("Var1");
    assertThat(retrievedVariable1, notNullValue());
    assertThat(retrievedVariable1.getName(), is("Var1"));

    Date timestampAfterVariableAdded = (Date) valueTable.getTimestamps().getLastUpdate().getValue();
    assertThat(timestampAtCreation + "should be before " + timestampAfterVariableAdded,
        timestampAtCreation.before(timestampAfterVariableAdded), is(true));

    Variable variable2 = Variable.Builder.newVariable("Var2", IntegerType.get(), PARTICIPANT).build();
    variableWriter.writeVariable(variable2);
    assertThat(Iterables.contains(valueTable.getVariables(), variable2), is(true));

    Variable retrievedVariable2 = valueTable.getVariable("Var2");
    assertThat(retrievedVariable2, notNullValue());
    assertThat(retrievedVariable2.getName(), is("Var2"));

    assertThat(Iterables.size(valueTable.getVariables()), is(2));

    variableWriter.close();
    tableWriter.close();
  }

  @Test
  public void canPersistValues() throws Exception {

    Neo4jDatasource datasource = createDatasource();
    MagmaEngine.get().addDatasource(datasource);
    ValueTableWriter tableWriter = datasource.createWriter(TABLE_NAME, PARTICIPANT);
    ValueTableWriter.VariableWriter variableWriter = tableWriter.writeVariables();
    Variable variable = Variable.Builder.newVariable("Var1", TextType.get(), PARTICIPANT).build();
    variableWriter.writeVariable(variable);

    VariableEntity entity = new VariableEntityBean(PARTICIPANT, "participant_1");
    ValueTableWriter.ValueSetWriter valueSetWriter = tableWriter.writeValueSet(entity);

    valueSetWriter.writeValue(variable, TextType.Factory.newValue("value1"));

    ValueTable valueTable = datasource.getValueTable(TABLE_NAME);

    assertThat(Iterables.size(valueTable.getVariableEntities()), is(1));
    assertThat(Iterables.size(valueTable.getValueSets()), is(1));

    ValueSet valueSet = valueTable.getValueSet(entity);
    assertThat(valueSet, notNullValue());
    assertThat(valueSet.getValueTable(), is(valueTable));
    assertThat(valueSet.getVariableEntity(), is(entity));

    Value value = valueTable.getValue(variable, valueSet);
    assertThat(value, notNullValue());
    assertThat(value.isNull(), is(false));
    assertThat(value.isSequence(), is(false));
    assertThat((TextType) value.getValueType(), is(TextType.get()));
    assertThat((String) value.getValue(), is("value1"));
  }

  private abstract static class TestThread extends Thread {

    private final Logger log = LoggerFactory.getLogger(getClass());

    private Throwable threadException;

    @Override
    public void run() {
      try {
        test();
      } catch(Throwable t) {
        log.debug("Throwable", t);
        threadException = t;
      }
    }

    public void assertNoException() throws InterruptedException {
      start();
      join();
      Assert.assertNull(threadException);
    }

    abstract protected void test() throws Throwable;

  }

}
