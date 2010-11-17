package org.obiba.magma.security.permissions;

import java.util.List;

import org.obiba.magma.Datasource;
import org.obiba.magma.ValueTable;
import org.obiba.magma.Variable;
import org.obiba.magma.security.Authorizer;

import com.google.common.base.Predicate;
import com.google.common.collect.Lists;

public class Permissions {

  public static final String WILDCARD = "*";

  public static final class Actions {

    public static final String READ = "read";

    public static final String WRITE = "write";
  }

  public static class Builder<T extends Builder<?>> {

    protected String domain;

    String action;

    protected List<String> instances = Lists.newArrayList();

    public Builder() {
    }

    private Builder(Builder builder) {
      this.domain = builder.domain;
      this.action = builder.action;
      this.instances.addAll(builder.instances);
    }

    public T domain(String domain) {
      this.domain = domain;
      return asT();
    }

    public T anyDomain() {
      this.domain = WILDCARD;
      return asT();
    }

    public T action(String action) {
      this.action = action;
      return asT();
    }

    public T anyAction() {
      this.action = WILDCARD;
      return asT();
    }

    public T read() {
      this.action = Actions.READ;
      return asT();
    }

    public T instance(String instance) {
      this.instances.add(instance);
      return asT();
    }

    public T instances(String... instances) {
      for(String i : instances) {
        this.instance(i);
      }
      return asT();
    }

    public T anyInstance() {
      this.instances.add(WILDCARD);
      return asT();
    }

    protected Builder newCopy() {
      return new Builder(this);
    }

    public String build() {
      return appendNonNull(appendNonNull(domain, action), instances);
    }

    @SuppressWarnings("unchecked")
    private T asT() {
      return (T) this;
    }

    /**
     * Appends <code>":" + segment</code> to <code>permission</code> when <code>segment</code> is non-null.
     */
    private String appendNonNull(String permission, String segment) {
      return permission + (segment != null ? ":" + segment : "");
    }

    private String appendNonNull(String permission, List<String> segments) {
      for(String segment : segments) {
        return appendNonNull(permission, segment);
      }
      return permission;
    }
  }

  public static class DatasourceBuilder extends Builder<DatasourceBuilder> {

    private static final String DOMAIN = "datasource";

    public static DatasourceBuilder forDatasource(String name) {
      DatasourceBuilder db = new DatasourceBuilder();
      db.domain(DOMAIN).instance(name);
      return db;
    }

    public static DatasourceBuilder forDatasource(Datasource datasource) {
      return forDatasource(datasource.getName());
    }

    public static DatasourceBuilder forDatasource() {
      DatasourceBuilder db = new DatasourceBuilder();
      db.domain(DOMAIN);
      return db;
    }

    public ValueTableBuilder tables() {
      return new ValueTableBuilder(this);
    }

    public ValueTableBuilder table(ValueTable table) {
      return new ValueTableBuilder(this).instance(table.getName());
    }

    public ValueTableBuilder table(String name) {
      return new ValueTableBuilder(this).instance(name);
    }

    public Predicate<Datasource> asPredicate(final Authorizer authorizer) {
      return new Predicate<Datasource>() {

        @Override
        public boolean apply(Datasource input) {
          return authorizer.isPermitted(DatasourceBuilder.this.newCopy().instance(input.getName()).build());
        }
      };
    }
  }

  public static class ValueTableBuilder extends Builder<ValueTableBuilder> {

    public ValueTableBuilder(DatasourceBuilder builder) {
      super(builder);
    }

    public static ValueTableBuilder forValueTable(ValueTable table) {
      return DatasourceBuilder.forDatasource(table.getDatasource()).table(table);
    }

    public VariableBuilder variable(Variable variable) {
      return new VariableBuilder(this).instance(variable.getName());
    }

    public VariableBuilder variable(String variable) {
      return new VariableBuilder(this).instance(variable);
    }

    public Predicate<ValueTable> asPredicate(final Authorizer authorizer) {
      return new Predicate<ValueTable>() {

        @Override
        public boolean apply(ValueTable input) {
          return authorizer.isPermitted(ValueTableBuilder.this.newCopy().instance(input.getName()).build());
        }
      };
    }

  }

  public static class VariableBuilder extends Builder<VariableBuilder> {

    public VariableBuilder(ValueTableBuilder builder) {
      super(builder);
    }

    public Predicate<Variable> asPredicate(final Authorizer authorizer) {
      return new Predicate<Variable>() {

        @Override
        public boolean apply(Variable input) {
          return authorizer.isPermitted(VariableBuilder.this.newCopy().instance(input.getName()).build());
        }
      };
    }
  }
}
