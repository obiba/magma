package org.obiba.magma.support;

import com.google.common.collect.ForwardingBlockingDeque;
import org.obiba.magma.ValueTable;
import org.obiba.magma.VariableEntity;

import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;

public class VariableEntityBlockingQueue extends ForwardingBlockingDeque<VariableEntity> {

  private final ValueTable table;

  private final int limit = 10000 * ValueTable.ENTITY_BATCH_SIZE;

  private final BlockingDeque<VariableEntity> deque;

  private int offset = 0;

  private int count;

  private final int maxCount;

  public VariableEntityBlockingQueue(ValueTable table) {
    this.table = table;
    this.maxCount = table.getVariableEntityCount();
    this.count = maxCount;
    this.deque = new LinkedBlockingDeque<>(this.limit);
  }

  @Override
  public int size() {
    return count;
  }

  @Override
  public VariableEntity poll() {
    synchronized (this) {
      if (deque.size() == 0 && offset < maxCount) {
        deque.addAll(table.getVariableEntities(offset, limit));
        offset = offset + limit;
      }
      count--;
    }
    return super.poll();
  }

  @Override
  protected BlockingDeque<VariableEntity> delegate() {
    return deque;
  }
}
