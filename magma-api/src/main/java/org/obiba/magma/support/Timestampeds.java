package org.obiba.magma.support;

import java.util.Comparator;

import org.obiba.magma.Timestamped;
import org.obiba.magma.Value;

public final class Timestampeds {

  public static final Comparator<Timestamped> lastUpdateComparator = new Comparator<Timestamped>() {

    @Override
    public int compare(Timestamped o1, Timestamped o2) {
      Value left = o1.getTimestamps().getLastUpdate();

      if(left.isNull()) return -1;

      Value right = o2.getTimestamps().getLastUpdate();

      if(right.isNull()) return 1;

      return left.compareTo(right);
    }

  };

  public static final Comparator<Timestamped> createdComparator = new Comparator<Timestamped>() {

    @Override
    public int compare(Timestamped o1, Timestamped o2) {
      Value left = o1.getTimestamps().getCreated();

      if(left.isNull()) return -1;

      Value right = o2.getTimestamps().getCreated();

      if(right.isNull()) return 1;

      return left.compareTo(right);
    }

  };

  private Timestampeds() {}
}
