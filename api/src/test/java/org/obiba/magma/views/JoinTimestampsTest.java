package org.obiba.magma.views;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.Date;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.obiba.magma.MagmaEngine;
import org.obiba.magma.Timestamps;
import org.obiba.magma.Value;
import org.obiba.magma.ValueTable;
import org.obiba.magma.type.DateTimeType;

public class JoinTimestampsTest {

  private Value earlyValue;

  private Value lateValue;

  @Before
  public void before() {
    new MagmaEngine();
    earlyValue = DateTimeType.get().valueOf(new Date(1000L));
    lateValue = DateTimeType.get().valueOf(new Date(4000000000L));
  }

  @After
  public void after() {
    MagmaEngine.get().shutdown();
  }

  @Test
  public void testGetCreated_DateIsEarliestDate() throws Exception {
    ValueTable valueTableMockOne = createMock(ValueTable.class);
    ValueTable valueTableMockTwo = createMock(ValueTable.class);
    Timestamps timestampsOne = createMock(Timestamps.class);
    Timestamps timestampsTwo = createMock(Timestamps.class);
    expect(valueTableMockOne.getTimestamps(null)).andReturn(timestampsOne).times(2);
    expect(timestampsOne.getCreated()).andReturn(lateValue).once();
    expect(valueTableMockTwo.getTimestamps(null)).andReturn(timestampsTwo).times(2);
    expect(timestampsTwo.getCreated()).andReturn(earlyValue).once();
    replay(valueTableMockOne, valueTableMockTwo, timestampsOne, timestampsTwo);
    Timestamps joinTimestamps = new JoinTimestamps(null, Arrays.asList(new ValueTable[] { valueTableMockOne, valueTableMockTwo }));
    assertThat(joinTimestamps.getCreated(), is(earlyValue));
    verify(valueTableMockOne, valueTableMockTwo, timestampsOne, timestampsTwo);
  }

  @Test
  public void testGetCreated_ThatSingleNullDateIsIgnored() throws Exception {
    ValueTable valueTableMockOne = createMock(ValueTable.class);
    ValueTable valueTableMockTwo = createMock(ValueTable.class);
    Timestamps timestampsOne = createMock(Timestamps.class);
    Timestamps timestampsTwo = createMock(Timestamps.class);
    expect(valueTableMockOne.getTimestamps(null)).andReturn(timestampsOne).times(2);
    expect(timestampsOne.getCreated()).andReturn(lateValue).once();
    expect(valueTableMockTwo.getTimestamps(null)).andReturn(timestampsTwo).times(2);
    expect(timestampsTwo.getCreated()).andReturn(null).once();
    replay(valueTableMockOne, valueTableMockTwo, timestampsOne, timestampsTwo);
    Timestamps joinTimestamps = new JoinTimestamps(null, Arrays.asList(new ValueTable[] { valueTableMockOne, valueTableMockTwo }));
    assertThat(joinTimestamps.getCreated(), is(lateValue));
    verify(valueTableMockOne, valueTableMockTwo, timestampsOne, timestampsTwo);
  }

  @Test
  public void testGetCreated_AllNullCreationDatesReturnNull() throws Exception {
    ValueTable valueTableMockOne = createMock(ValueTable.class);
    ValueTable valueTableMockTwo = createMock(ValueTable.class);
    Timestamps timestampsOne = createMock(Timestamps.class);
    Timestamps timestampsTwo = createMock(Timestamps.class);
    expect(valueTableMockOne.getTimestamps(null)).andReturn(timestampsOne).times(2);
    expect(timestampsOne.getCreated()).andReturn(null).once();
    expect(valueTableMockTwo.getTimestamps(null)).andReturn(timestampsTwo).times(2);
    expect(timestampsTwo.getCreated()).andReturn(null).once();
    replay(valueTableMockOne, valueTableMockTwo, timestampsOne, timestampsTwo);
    Timestamps joinTimestamps = new JoinTimestamps(null, Arrays.asList(new ValueTable[] { valueTableMockOne, valueTableMockTwo }));
    assertThat(joinTimestamps.getCreated(), nullValue());
    verify(valueTableMockOne, valueTableMockTwo, timestampsOne, timestampsTwo);
  }

  @Test
  public void testGetLastUpdate_DateIsLatestDate() throws Exception {
    ValueTable valueTableMockOne = createMock(ValueTable.class);
    ValueTable valueTableMockTwo = createMock(ValueTable.class);
    Timestamps timestampsOne = createMock(Timestamps.class);
    Timestamps timestampsTwo = createMock(Timestamps.class);
    expect(valueTableMockOne.getTimestamps(null)).andReturn(timestampsOne).times(2);
    expect(timestampsOne.getLastUpdate()).andReturn(lateValue).once();
    expect(valueTableMockTwo.getTimestamps(null)).andReturn(timestampsTwo).times(2);
    expect(timestampsTwo.getLastUpdate()).andReturn(earlyValue).once();
    replay(valueTableMockOne, valueTableMockTwo, timestampsOne, timestampsTwo);
    Timestamps joinTimestamps = new JoinTimestamps(null, Arrays.asList(new ValueTable[] { valueTableMockOne, valueTableMockTwo }));
    assertThat(joinTimestamps.getLastUpdate(), is(lateValue));
    verify(valueTableMockOne, valueTableMockTwo, timestampsOne, timestampsTwo);
  }
}
