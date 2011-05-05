package org.obiba.magma.support;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.Date;

import org.junit.Before;
import org.junit.Test;
import org.obiba.magma.Timestamped;
import org.obiba.magma.Timestamps;
import org.obiba.magma.Value;
import org.obiba.magma.support.UnionTimestamps;
import org.obiba.magma.test.AbstractMagmaTest;
import org.obiba.magma.type.DateTimeType;

import com.google.common.collect.ImmutableList;

public class UnionTimestampsTest extends AbstractMagmaTest {

  private Value earlyValue;

  private Value lateValue;

  @Before
  public void before() {
    earlyValue = DateTimeType.get().valueOf(new Date(1000L));
    lateValue = DateTimeType.get().valueOf(new Date(4000000000L));
  }

  @Test
  public void testGetCreated_DateIsEarliestDate() throws Exception {
    assertCreatedTimestamps(lateValue, earlyValue, earlyValue);
  }

  @Test
  public void testGetCreated_ThatSingleNullDateIsIgnored() throws Exception {
    assertCreatedTimestamps(lateValue, DateTimeType.get().nullValue(), lateValue);
    assertCreatedTimestamps(DateTimeType.get().nullValue(), lateValue, lateValue);
  }

  @Test
  public void testGetCreated_AllNullCreationDatesReturnNull() throws Exception {
    assertCreatedTimestamps(DateTimeType.get().nullValue(), DateTimeType.get().nullValue(), DateTimeType.get().nullValue());
  }

  @Test
  public void testGetLastUpdate_DateIsLatestDate() throws Exception {
    assertUpdatedTimestamps(lateValue, earlyValue, lateValue);
  }

  @Test
  public void testGetLastUpdate_ThatSingleNullDateIsIgnored() throws Exception {
    assertUpdatedTimestamps(lateValue, DateTimeType.get().nullValue(), lateValue);
    assertUpdatedTimestamps(DateTimeType.get().nullValue(), lateValue, lateValue);
  }

  @Test
  public void testGetLastUpdate_AllNullCreationDatesReturnNull() throws Exception {
    assertUpdatedTimestamps(DateTimeType.get().nullValue(), DateTimeType.get().nullValue(), DateTimeType.get().nullValue());
  }

  private void assertCreatedTimestamps(Value firstTimestamp, Value secondTimestamp, Value expectedTimestamp) {
    assertTimestamps(true, firstTimestamp, secondTimestamp, expectedTimestamp);
  }

  private void assertUpdatedTimestamps(Value firstTimestamp, Value secondTimestamp, Value expectedTimestamp) {
    assertTimestamps(false, firstTimestamp, secondTimestamp, expectedTimestamp);
  }

  private void assertTimestamps(boolean useCreatedTimestamps, Value firstTimestamp, Value secondTimestamp, Value expectedTimestamp) {

    Timestamped firstTimestamped = createMock(Timestamped.class);
    Timestamped secondTimestamped = createMock(Timestamped.class);

    Timestamps timestampsOne = createMock(Timestamps.class);
    Timestamps timestampsTwo = createMock(Timestamps.class);

    expect(firstTimestamped.getTimestamps()).andReturn(timestampsOne).once();
    if(useCreatedTimestamps) {
      expect(timestampsOne.getCreated()).andReturn(firstTimestamp).once();
    } else {
      expect(timestampsOne.getLastUpdate()).andReturn(firstTimestamp).once();
    }

    expect(secondTimestamped.getTimestamps()).andReturn(timestampsTwo).once();

    if(useCreatedTimestamps) {
      expect(timestampsTwo.getCreated()).andReturn(secondTimestamp).once();
    } else {
      expect(timestampsTwo.getLastUpdate()).andReturn(secondTimestamp).once();
    }
    replay(firstTimestamped, secondTimestamped, timestampsOne, timestampsTwo);

    Timestamps joinTimestamps = new UnionTimestamps(ImmutableList.of(firstTimestamped, secondTimestamped));

    if(useCreatedTimestamps) {
      assertThat(joinTimestamps.getCreated(), is(expectedTimestamp));
    } else {
      assertThat(joinTimestamps.getLastUpdate(), is(expectedTimestamp));
    }
    verify(firstTimestamped, secondTimestamped, timestampsOne, timestampsTwo);
  }
}
