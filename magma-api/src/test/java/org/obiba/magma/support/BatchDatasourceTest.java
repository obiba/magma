package org.obiba.magma.support;

import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.obiba.magma.ValueSet;
import org.obiba.magma.ValueTable;

import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class BatchDatasourceTest {

  private Set<String> originalEntities;

  @Before
  public void setup() {
    originalEntities = Sets.newLinkedHashSet();
    for (int i = 1; i <= 100; i++) {
      originalEntities.add("" + i);
    }
  }

  @Test
  public void testBatchValueTable() {
    StaticDatasource originalDs = new StaticDatasource("original");
    originalDs.addValueTable(new StaticValueTable(originalDs, "table", originalEntities));

    BatchDatasource batchDs = new BatchDatasource(originalDs, 50);
    Initialisables.initialise(originalDs, batchDs);

    assertThat(batchDs.getValueTables().size(), is(1));
    ValueTable batchedTable = batchDs.getValueTables().iterator().next();
    assertThat(batchedTable.getName(), is("table"));
    assertThat(batchedTable.getEntityType(), is("Participant"));
    Iterable<ValueSet> batchedValueSets = batchedTable.getValueSets();
    assertThat(Iterables.size(batchedValueSets), is(50));
    assertThat(batchedTable.hasValueSet(new VariableEntityBean("Participant", "1")), is(true));
    assertThat(batchedTable.hasValueSet(new VariableEntityBean("Participant", "50")), is(true));
    assertThat(batchedTable.hasValueSet(new VariableEntityBean("Participant","51")), is(false));
    assertThat(batchedTable.hasValueSet(new VariableEntityBean("Participant","100")), is(false));
  }

  @Test
  public void testHasEntities() {
    StaticDatasource originalDs = new StaticDatasource("original");
    originalDs.addValueTable(new StaticValueTable(originalDs, "table", originalEntities));
    BatchDatasource batchDs = new BatchDatasource(originalDs, 50);
    Initialisables.initialise(originalDs, batchDs);
    assertThat(originalDs.hasEntities(new EntitiesPredicate.NonViewEntitiesPredicate()), is(true));
  }


  @Test
  public void testHasNoEntities() {
    StaticDatasource originalDs = new StaticDatasource("original");
    BatchDatasource batchDs = new BatchDatasource(originalDs, 50);
    Initialisables.initialise(originalDs, batchDs);
    assertThat(originalDs.hasEntities(new EntitiesPredicate.NonViewEntitiesPredicate()), is(false));
  }

}
