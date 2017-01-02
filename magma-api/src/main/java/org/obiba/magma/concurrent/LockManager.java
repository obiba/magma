/*
 * Copyright (c) 2017 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

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

  private final Map<String, ReentrantLock> locks = new HashMap<>();

  public synchronized void lock(Collection<String> lockNames) throws InterruptedException {
    @SuppressWarnings("TypeMayBeWeakened")
    Set<String> lockedNames = new HashSet<>();
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
