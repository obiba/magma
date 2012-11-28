/**
 *
 */
package org.obiba.magma.concurrent;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;

public class LockManager {

  private final Map<String, ReentrantLock> locks = new HashMap<String, ReentrantLock>();

  public synchronized void lock(Collection<String> lockNames) throws InterruptedException {
    @SuppressWarnings("TypeMayBeWeakened")
    Set<String> lockedNames = new HashSet<String>();
    while(lockedNames.size() < lockNames.size()) {
      for(String lockName : lockNames) {
        ReentrantLock lock = getOrCreateLock(lockName);
        if(lock.tryLock()) {
          lockedNames.add(lockName);
        } else {
          if(lockedNames.size() != 0) {
            unlock(lockedNames, false);
            lockedNames.clear();
          }
          wait();
          break;
        }
      }
    }
  }

  public synchronized void unlock(Iterable<String> lockNames, boolean notify) {
    for(String lockName : lockNames) {
      ReentrantLock lock = locks.get(lockName);
      if(lock != null) {
        lock.unlock();
      }
    }

    if(notify) {
      notify();
    }
  }

  private synchronized ReentrantLock getOrCreateLock(String lockName) {
    ReentrantLock lock = locks.get(lockName);
    if(lock == null) {
      lock = new ReentrantLock();
      locks.put(lockName, lock);
    }
    return lock;
  }
}
