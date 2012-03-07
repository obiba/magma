package org.obiba.magma.lang;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.io.Closeable;
import java.io.IOException;

import org.junit.Test;

public class CloseablesTest {

  @Test
  public void test_closeQuitely_callsClose() throws IOException {
    Closeable mock = createMock(Closeable.class);
    mock.close();
    expectLastCall().once();
    replay(mock);
    Closeables.closeQuietly(mock);
    verify(mock);
  }

  @Test
  public void test_closeQuitely_callsCloseAndEatsIOException() throws IOException {
    Closeable mock = createMock(Closeable.class);
    mock.close();
    expectLastCall().andThrow(new IOException()).once();
    replay(mock);
    try {
      Closeables.closeQuietly(mock);
    } finally {
      verify(mock);
    }
  }

  @Test
  public void test_closeQuitely_callsCloseAndDoesNotEatRuntimeException() throws IOException {
    Closeable mock = createMock(Closeable.class);
    mock.close();
    RuntimeException runtime = new RuntimeException();
    expectLastCall().andThrow(runtime).once();
    replay(mock);
    try {
      Closeables.closeQuietly(mock);
      assertThat("Expected an exception to be throw", true, is(false));
    } catch(RuntimeException e) {
      assertThat(e, is(runtime));
    } finally {
      verify(mock);
    }
  }

  @Test
  public void test_closeQuietly_acceptsNull() {
    Closeables.closeQuietly(null);
  }
}
