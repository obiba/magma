package org.obiba.magma.views;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.obiba.magma.Datasource;
import org.obiba.magma.ValueSet;
import org.obiba.magma.ValueTable;
import org.obiba.magma.VariableEntity;
import org.obiba.magma.audit.VariableEntityAuditEvent;
import org.obiba.magma.audit.VariableEntityAuditLog;
import org.obiba.magma.audit.VariableEntityAuditLogManager;
import org.obiba.magma.test.AbstractMagmaTest;
import org.obiba.magma.type.TextType;

public class IncrementalWhereClauseTest extends AbstractMagmaTest {
  //
  // Instance Variables
  //

  private VariableEntityAuditLogManager auditLogManagerMock;

  private VariableEntityAuditLog auditLogMock;

  private Datasource sourceMock;

  private ValueTable valueTableMock;

  private ValueSet valueSetMock;

  private VariableEntity entityMock;

  //
  // Fixture Methods (setUp / tearDown)
  //

  @Before
  public void setUp() {
    // Create mocks.
    auditLogManagerMock = createMock(VariableEntityAuditLogManager.class);
    auditLogMock = createMock(VariableEntityAuditLog.class);
    sourceMock = createMock(Datasource.class);
    valueTableMock = createMock(ValueTable.class);
    valueSetMock = createMock(ValueSet.class);
    entityMock = createMock(VariableEntity.class);

    // Record common expectations.
    expect(sourceMock.getName()).andReturn("source").anyTimes();
    expect(auditLogManagerMock.getAuditLog(entityMock)).andReturn(auditLogMock).anyTimes();
    expect(valueTableMock.getDatasource()).andReturn(sourceMock).anyTimes();
    expect(valueTableMock.getName()).andReturn("table").anyTimes();
    expect(valueSetMock.getVariableEntity()).andReturn(entityMock).anyTimes();
    expect(valueSetMock.getValueTable()).andReturn(valueTableMock).anyTimes();
  }

  //
  // Test Methods
  //

  @Test
  public void testCaseOneValueSetNeverCopiedFromSourceToDestination() {
    expect(auditLogMock.getAuditEvents(valueTableMock)).andReturn(getCaseOneCopyEventsFromSource()).anyTimes();
    expect(auditLogMock.getAuditEvents("COPY")).andReturn(getCaseOneAllCopyEvents()).anyTimes();
    replay(auditLogManagerMock, auditLogMock, sourceMock, valueTableMock, valueSetMock, entityMock);

    IncrementalWhereClause whereClause = new IncrementalWhereClause("destination.table");
    whereClause.setAuditLogManager(auditLogManagerMock);

    assertEquals(true, whereClause.where(valueSetMock));
  }

  @Test
  public void testCaseTwoValueSetCopiedFromSourceToDestinationButNeverCopiedToSource() {
    expect(auditLogMock.getAuditEvents(valueTableMock)).andReturn(getCaseTwoCopyEventsFromSource()).anyTimes();
    expect(auditLogMock.getAuditEvents("COPY")).andReturn(getCaseTwoAllCopyEvents()).anyTimes();
    replay(auditLogManagerMock, auditLogMock, sourceMock, valueTableMock, valueSetMock, entityMock);

    IncrementalWhereClause whereClause = new IncrementalWhereClause("destination.table");
    whereClause.setAuditLogManager(auditLogManagerMock);

    assertEquals(true, whereClause.where(valueSetMock));
  }

  @Test
  public void testCaseThreeValueSetCopiedFromSourceToDestinationAndCopiedToSourceAfterLastCopyToDestination() {
    expect(auditLogMock.getAuditEvents(valueTableMock)).andReturn(getCaseThreeCopyEventsFromSource()).anyTimes();
    expect(auditLogMock.getAuditEvents("COPY")).andReturn(getCaseThreeAllCopyEvents()).anyTimes();
    replay(auditLogManagerMock, auditLogMock, sourceMock, valueTableMock, valueSetMock, entityMock);

    IncrementalWhereClause whereClause = new IncrementalWhereClause("destination.table");
    whereClause.setAuditLogManager(auditLogManagerMock);

    assertEquals(true, whereClause.where(valueSetMock));
  }

  @Test
  public void testCaseFourValueSetCopiedFromSourceToDestinationAndCopiedToSourceBeforeLastCopyToDestination() {
    expect(auditLogMock.getAuditEvents(valueTableMock)).andReturn(getCaseFourCopyEventsFromSource()).anyTimes();
    expect(auditLogMock.getAuditEvents("COPY")).andReturn(getCaseFourAllCopyEvents()).anyTimes();
    replay(auditLogManagerMock, auditLogMock, sourceMock, valueTableMock, valueSetMock, entityMock);

    IncrementalWhereClause whereClause = new IncrementalWhereClause("destination.table");
    whereClause.setAuditLogManager(auditLogManagerMock);

    assertEquals(false, whereClause.where(valueSetMock));
  }

  //
  // Helper Methods
  //

  private List<VariableEntityAuditEvent> getCaseOneCopyEventsFromSource() {
    List<VariableEntityAuditEvent> events = new ArrayList<VariableEntityAuditEvent>();

    Calendar calendar = Calendar.getInstance();
    calendar.set(2010, 0, 01, 0, 0, 0);
    calendar.set(Calendar.MILLISECOND, 0);

    events.add(createMockCopyEvent("COPY", calendar.getTime(), "source.table", "destination.differentTable"));
    events.add(createMockCopyEvent("COPY", getDayBefore(calendar), "source.table", "otherDestination.otherTable"));

    return events;
  }

  private List<VariableEntityAuditEvent> getCaseOneAllCopyEvents() {
    List<VariableEntityAuditEvent> events = new ArrayList<VariableEntityAuditEvent>();

    Calendar calendar = Calendar.getInstance();
    calendar.set(2010, 0, 01, 0, 0, 0);
    calendar.set(Calendar.MILLISECOND, 0);

    events.add(createMockCopyEvent("COPY", calendar.getTime(), "source.table", "destination.differentTable"));
    events.add(createMockCopyEvent("COPY", getDayBefore(calendar), "source.table", "otherDestination.otherTable"));
    events.add(createMockCopyEvent("COPY", getDayBefore(calendar), "foo.bar", "source.table"));

    return events;
  }

  private List<VariableEntityAuditEvent> getCaseTwoCopyEventsFromSource() {
    List<VariableEntityAuditEvent> events = new ArrayList<VariableEntityAuditEvent>();

    Calendar calendar = Calendar.getInstance();
    calendar.set(2010, 01, 01);

    events.add(createMockCopyEvent("COPY", calendar.getTime(), "source.table", "destination.table"));

    return events;
  }

  private List<VariableEntityAuditEvent> getCaseTwoAllCopyEvents() {
    List<VariableEntityAuditEvent> events = new ArrayList<VariableEntityAuditEvent>();

    Calendar calendar = Calendar.getInstance();
    calendar.set(2010, 01, 02);

    events.add(createMockCopyEvent("COPY", calendar.getTime(), "source.table", "destination.table"));
    events.add(createMockCopyEvent("COPY", getDayBefore(calendar), "foo.bar", "destination.table"));

    return events;
  }

  private List<VariableEntityAuditEvent> getCaseThreeCopyEventsFromSource() {
    List<VariableEntityAuditEvent> events = new ArrayList<VariableEntityAuditEvent>();

    Calendar calendar = Calendar.getInstance();
    calendar.set(2010, 01, 01);

    events.add(createMockCopyEvent("COPY", calendar.getTime(), "source.table", "destination.table"));

    return events;
  }

  private List<VariableEntityAuditEvent> getCaseThreeAllCopyEvents() {
    List<VariableEntityAuditEvent> events = new ArrayList<VariableEntityAuditEvent>();

    Calendar calendar = Calendar.getInstance();
    calendar.set(2010, 01, 02);

    events.add(createMockCopyEvent("COPY", calendar.getTime(), "source.table", "destination.table"));
    events.add(createMockCopyEvent("COPY", getDayAfter(calendar), "foo.bar", "source.table"));

    return events;
  }

  private List<VariableEntityAuditEvent> getCaseFourCopyEventsFromSource() {
    List<VariableEntityAuditEvent> events = new ArrayList<VariableEntityAuditEvent>();

    Calendar calendar = Calendar.getInstance();
    calendar.set(2010, 01, 01);

    events.add(createMockCopyEvent("COPY", calendar.getTime(), "source.table", "destination.table"));

    return events;
  }

  private List<VariableEntityAuditEvent> getCaseFourAllCopyEvents() {
    List<VariableEntityAuditEvent> events = new ArrayList<VariableEntityAuditEvent>();

    Calendar calendar = Calendar.getInstance();
    calendar.set(2010, 01, 02);

    events.add(createMockCopyEvent("COPY", calendar.getTime(), "source.table", "destination.table"));
    events.add(createMockCopyEvent("COPY", getDayBefore(calendar), "foo.bar", "source.table"));

    return events;
  }

  private VariableEntityAuditEvent createMockCopyEvent(String type, Date date, String source, String destination) {
    String[] sourceTokens = source.split("\\.");

    VariableEntityAuditEvent eventMock = createMock(VariableEntityAuditEvent.class);
    expect(eventMock.getType()).andReturn(type).anyTimes();
    expect(eventMock.getDatetime()).andReturn(date).anyTimes();
    expect(eventMock.getDatasource()).andReturn(sourceTokens[0]).anyTimes();
    expect(eventMock.getValueTable()).andReturn(sourceTokens[1]).anyTimes();
    expect(eventMock.getDetailValue("destinationName")).andReturn(TextType.get().valueOf(destination)).anyTimes();
    replay(eventMock);

    return eventMock;
  }

  private Date getDayBefore(Calendar calender) {
    calender.add(Calendar.DATE, -1);
    return calender.getTime();
  }

  private Date getDayAfter(Calendar calender) {
    calender.add(Calendar.DATE, 1);
    return calender.getTime();
  }
}
