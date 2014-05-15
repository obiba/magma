package org.obiba.magma.support;

import org.junit.Test;
import org.obiba.magma.Disposable;
import org.obiba.magma.Initialisable;
import org.obiba.magma.MagmaRuntimeException;

import com.google.common.collect.ImmutableList;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

public class InitialisablesTest {

  @Test
  public void test_initialise_Initialisable() {
    Initialisable initialisable = createMock(Initialisable.class);
    initialisable.initialise();
    expectLastCall().once();
    replay(initialisable);
    Initialisables.initialise(initialisable);
    verify(initialisable);
  }

  @Test(expected = MagmaRuntimeException.class)
  public void test_initialise_throwsThrown() {
    Initialisable initialisable = createMock(Initialisable.class);
    initialisable.initialise();
    expectLastCall().andThrow(new MagmaRuntimeException()).once();
    replay(initialisable);
    Initialisables.initialise(initialisable);
    verify(initialisable);
  }

  @Test(expected = MagmaRuntimeException.class)
  public void test_initialise_throwsMagmaRuntimeException() {
    Initialisable initialisable = createMock(Initialisable.class);
    initialisable.initialise();
    expectLastCall().andThrow(new RuntimeException()).once();
    replay(initialisable);
    Initialisables.initialise(initialisable);
    verify(initialisable);
  }

  @Test
  public void test_initialise_InitialisableAsObject() {
    Initialisable initialisable = createMock(Initialisable.class);
    initialisable.initialise();
    expectLastCall().once();
    replay(initialisable);
    Initialisables.initialise((Object) initialisable);
    verify(initialisable);
  }

  @Test
  public void test_initialise_Object() {
    Disposable notInitialisable = createMock(Disposable.class);
    replay(notInitialisable);
    Initialisables.initialise(notInitialisable);
    verify(notInitialisable);
  }

  @Test
  public void test_initialise_Initialisables() {
    vararg(createMock(Initialisable.class), createMock(Initialisable.class), createMock(Initialisable.class));
  }

  @Test
  public void test_initialise_Objects() {
    Initialisables.initialise(new Object(), new Object());
  }

  @Test
  public void test_initialise_Iterable() {
    iterable(ImmutableList
        .of(createMock(Initialisable.class), createMock(Initialisable.class), createMock(Initialisable.class)));
  }

  private void vararg(Initialisable... mocks) {
    for(Initialisable initialisable : mocks) {
      initialisable.initialise();
      expectLastCall().once();
    }
    replay((Object[]) mocks);
    Initialisables.initialise(mocks);
    verify((Object[]) mocks);
  }

  private void iterable(Iterable<Initialisable> mocks) {
    for(Initialisable initialisable : mocks) {
      initialisable.initialise();
      expectLastCall().once();
      replay(initialisable);
    }
    Initialisables.initialise(mocks);
    for(Initialisable initialisable : mocks) {
      verify(initialisable);
    }
  }
}
