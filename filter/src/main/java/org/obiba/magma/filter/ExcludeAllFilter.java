package org.obiba.magma.filter;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("excludeAll")
public class ExcludeAllFilter<T> extends AbstractFilter<T> {

  @Override
  boolean runFilter(T item) {
    type = EXCLUDE;
    return true;
  }
}
