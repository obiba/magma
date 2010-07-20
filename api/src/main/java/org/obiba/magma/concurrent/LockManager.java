/**
 * 
 */
package org.obiba.magma.concurrent;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;

public class LockManager {

  private Map<String, ReentrantLock> locks = new HashMap<String, ReentrantLock>();

  public synchronized void lock(Set<String> lockNames) throws InterruptedException {
    Set<String> lockedNames = new HashSet<String>();

    while(lockedNames.size() < lockNames.size()) {
      for(String lockName : lockNames) {
        ReentrantLock lock = getOrCreateLock(lockName);
        if(lock.tryLock()) {
          lockedNames.add(lockName);
        } else {
          unlock(lockedNames);
          wait(1000l);
          break;
        }
      }
    }
  }

  public synchronized void unlock(Set<String> lockNames) {
    for(String lockName : lockNames) {
      ReentrantLock lock = locks.get(lockName);
      if(lock != null) {
        lock.unlock();
      }
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
