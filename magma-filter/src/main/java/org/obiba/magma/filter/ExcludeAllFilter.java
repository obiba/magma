package org.obiba.magma.filter;

import org.obiba.magma.ValueSet;
import org.obiba.magma.VariableValueSource;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("excludeAll")
public class ExcludeAllFilter<T> extends AbstractFilter<T> {

  private ExcludeAllFilter() {
    // Force clients to use the builder.
  }

  @Override
  protected Boolean runFilter(T item) {
    return Boolean.TRUE;
  }

  public static class Builder {

    public static Builder newFilter() {
      return new Builder();
    }

    public ExcludeAllFilter<ValueSet> buildForValueSet() {
      ExcludeAllFilter<ValueSet> filter = new ExcludeAllFilter<ValueSet>();
      filter.setType(Type.EXCLUDE);
      return filter;
    }

    public ExcludeAllFilter<VariableValueSource> buildForVariableValueSource() {
      ExcludeAllFilter<VariableValueSource> filter = new ExcludeAllFilter<VariableValueSource>();
      filter.setType(Type.EXCLUDE);
      return filter;
    }

  }
}
