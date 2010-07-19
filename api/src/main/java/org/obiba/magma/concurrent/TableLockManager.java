/**
 * 
 */
package org.obiba.magma.concurrent;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;

public class TableLockManager {

  private Map<String, ReentrantLock> tableLocks = new HashMap<String, ReentrantLock>();

  public synchronized void lockTables(Set<String> tableNames) {
    Set<String> lockedTables = new HashSet<String>();

    while(lockedTables.size() < tableNames.size()) {
      for(String tableName : tableNames) {
        ReentrantLock tableLock = getOrCreateLock(tableName);
        if(tableLock.tryLock()) {
          lockedTables.add(tableName);
        } else {
          unlockTables(lockedTables);
          try {
            wait(1000l);
          } catch(InterruptedException ex) {
            throw new RuntimeException("TableLockManager: " + Thread.currentThread() + " was interrupted!");
          }
          break;
        }
      }
    }
  }

  public synchronized void unlockTables(Set<String> tableNames) {
    for(String tableName : tableNames) {
      ReentrantLock tableLock = tableLocks.get(tableName);
      if(tableLock != null) {
        tableLock.unlock();
      }
    }
  }

  private synchronized ReentrantLock getOrCreateLock(String tableName) {
    ReentrantLock tableLock = tableLocks.get(tableName);
    if(tableLock == null) {
      tableLock = new ReentrantLock();
      tableLocks.put(tableName, tableLock);
    }
    return tableLock;
  }
}
