package org.obiba.magma.views;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.obiba.magma.ValueTable;

public class JoinTableTest {
  //
  // Constants
  //

  private static final String PARTICIPANT_ENTITY_TYPE = "Participant";

  //
  // Instance Variables
  //

  //
  // Test Methods
  //

  @Test(expected = IllegalArgumentException.class)
  public void testTableListCannotBeNull() {
    new JoinTable(null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testTableListCannotBeEmpty() {
    List<ValueTable> tables = new ArrayList<ValueTable>();
    new JoinTable(tables);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testTableListMustContainAtLeastTwoTables() {
    List<ValueTable> tables = new ArrayList<ValueTable>();
    tables.add(createMockTable("table", PARTICIPANT_ENTITY_TYPE));

    new JoinTable(tables);
  }

  @Test
  public void testTableListWithTwoTablesAllowed() {
    List<ValueTable> tables = new ArrayList<ValueTable>();
    tables.add(createMockTable("tableOne", PARTICIPANT_ENTITY_TYPE));
    tables.add(createMockTable("tableTwo", PARTICIPANT_ENTITY_TYPE));

    replay(tables.toArray());

    new JoinTable(tables);
  }

  @Test
  public void testTableListWithMoreThanTwoTablesAllowed() {
    List<ValueTable> tables = new ArrayList<ValueTable>();
    tables.add(createMockTable("tableOne", PARTICIPANT_ENTITY_TYPE));
    tables.add(createMockTable("tableTwo", PARTICIPANT_ENTITY_TYPE));
    tables.add(createMockTable("tableThree", PARTICIPANT_ENTITY_TYPE));

    replay(tables.toArray());

    new JoinTable(tables);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testAllTablesMustHaveTheSameEntityType() {
    List<ValueTable> tables = new ArrayList<ValueTable>();
    tables.add(createMockTable("tableOne", PARTICIPANT_ENTITY_TYPE));
    tables.add(createMockTable("tableTwo", "someOtherType"));

    replay(tables.toArray());

    new JoinTable(tables);
  }

  @Test
  public void testJoinTableHasNoDatasource() {
    List<ValueTable> tables = new ArrayList<ValueTable>();
    tables.add(createMockTable("tableOne", PARTICIPANT_ENTITY_TYPE));
    tables.add(createMockTable("tableTwo", PARTICIPANT_ENTITY_TYPE));

    replay(tables.toArray());

    JoinTable joinTable = new JoinTable(tables);
    assertEquals(null, joinTable.getDatasource());
  }

  @Test
  public void testJoinTableNameIsConcatenationOfIndividualTableNames() {
    List<ValueTable> tables = new ArrayList<ValueTable>();
    tables.add(createMockTable("tableOne", PARTICIPANT_ENTITY_TYPE));
    tables.add(createMockTable("tableTwo", PARTICIPANT_ENTITY_TYPE));

    replay(tables.toArray());

    JoinTable joinTable = new JoinTable(tables);
    assertEquals("tableOne-tableTwo", joinTable.getName());
  }

  //
  // Helper Methods
  //

  private ValueTable createMockTable(String name, String entityType) {
    ValueTable mockTable = createMock(ValueTable.class);
    expect(mockTable.getName()).andReturn(name).anyTimes();
    expect(mockTable.getEntityType()).andReturn(entityType).anyTimes();

    return mockTable;
  }
}
