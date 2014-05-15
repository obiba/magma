package org.obiba.magma.support;

import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.obiba.magma.ValueSet;
import org.obiba.magma.ValueTable;

import com.google.common.collect.Sets;

import static org.fest.assertions.api.Assertions.assertThat;

public class BatchDatasourceTest {

  private Set<String> originalEntities;

  @Before
  public void setup() {
    originalEntities = Sets.newLinkedHashSet();
    for(int i = 1; i <= 100; i++) {
      originalEntities.add("" + i);
    }
  }

  @Test
  public void testBatchValueTable() {
    StaticDatasource originalDs = new StaticDatasource("original");
    originalDs.addValueTable(new StaticValueTable(originalDs, "table", originalEntities));

    BatchDatasource batchDs = new BatchDatasource(originalDs, 50);
    Initialisables.initialise(originalDs, batchDs);

    assertThat(batchDs.getValueTables()).hasSize(1);
    ValueTable batchedTable = batchDs.getValueTables().iterator().next();
    assertThat(batchedTable.getName()).isEqualTo("table");
    assertThat(batchedTable.getEntityType()).isEqualTo("Participant");
    Iterable<ValueSet> batchedValueSets = batchedTable.getValueSets();
    assertThat(batchedValueSets).hasSize(50);
    assertThat(batchedTable.hasValueSet(new VariableEntityBean("Participant", "1"))).isTrue();
    assertThat(batchedTable.hasValueSet(new VariableEntityBean("Participant", "50"))).isTrue();
    assertThat(batchedTable.hasValueSet(new VariableEntityBean("Participant", "51"))).isFalse();
    assertThat(batchedTable.hasValueSet(new VariableEntityBean("Participant", "100"))).isFalse();
  }

  @Test
  public void testHasEntities() {
    StaticDatasource originalDs = new StaticDatasource("original");
    originalDs.addValueTable(new StaticValueTable(originalDs, "table", originalEntities));
    BatchDatasource batchDs = new BatchDatasource(originalDs, 50);
    Initialisables.initialise(originalDs, batchDs);
    assertThat(originalDs.hasEntities(new EntitiesPredicate.NonViewEntitiesPredicate())).isTrue();
  }

  @Test
  public void testHasNoEntities() {
    StaticDatasource originalDs = new StaticDatasource("original");
    BatchDatasource batchDs = new BatchDatasource(originalDs, 50);
    Initialisables.initialise(originalDs, batchDs);
    assertThat(originalDs.hasEntities(new EntitiesPredicate.NonViewEntitiesPredicate())).isFalse();
  }

}
