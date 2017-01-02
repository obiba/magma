/*
 * Copyright (c) 2017 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.magma.security.permissions;

import org.obiba.magma.Datasource;
import org.obiba.magma.ValueTable;
import org.obiba.magma.Variable;
import org.obiba.magma.security.Authorizer;

import com.google.common.base.Predicate;

public class Permissions {

  public static final String WILDCARD = "*";

  private Permissions() {}

  public static final class Actions {

    public static final String READ = "GET";

    public static final String WRITE = "POST";

    public static final String DELETE = "DELETE";

    private Actions() {}
  }

  @SuppressWarnings({ "ParameterHidesMemberVariable", "UnusedDeclaration" })
  public static class Builder<T extends Builder<?>> {

    protected String domain;

    String action;

    protected StringBuilder path = new StringBuilder();

    public Builder() {
    }

    private Builder(Builder<?> builder) {
      domain = builder.domain;
      action = builder.action;
      path = new StringBuilder(builder.path);
    }

    public T domain(String domain) {
      this.domain = domain;
      return asT();
    }

    public T anyDomain() {
      domain = WILDCARD;
      return asT();
    }

    public T action(String action) {
      this.action = action;
      return asT();
    }

    public T anyAction() {
      action = WILDCARD;
      return asT();
    }

    public T read() {
      action = Actions.READ;
      return asT();
    }

    public T delete() {
      action = Actions.DELETE;
      return asT();
    }

    public T instance(String instance) {
      path.append('/').append(instance);
      return asT();
    }

    public T instances(String... instances) {
      for(String i : instances) {
        instance(i);
      }
      return asT();
    }

    public String build() {
      return appendNonNull(domain, path.toString(), action);
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

    private String appendNonNull(String permission, String... segments) {
      String newPermission = permission;
      for(String segment : segments) {
        newPermission = appendNonNull(newPermission, segment);
      }
      return newPermission;
    }
  }

  @SuppressWarnings("StaticMethodOnlyUsedInOneClass")
  public static class DatasourcePermissionBuilder extends Builder<DatasourcePermissionBuilder> {

    private static final String DOMAIN = "magma";

    private DatasourcePermissionBuilder(DatasourcePermissionBuilder other) {
      super(other);
    }

    private DatasourcePermissionBuilder() {
    }

    public static DatasourcePermissionBuilder forDatasource(String name) {
      DatasourcePermissionBuilder db = new DatasourcePermissionBuilder();
      db.domain(DOMAIN).instance("datasource").instance(name);
      return db;
    }

    public static DatasourcePermissionBuilder forDatasource(Datasource datasource) {
      return forDatasource(datasource.getName());
    }

    public static DatasourcePermissionBuilder forDatasource() {
      DatasourcePermissionBuilder db = new DatasourcePermissionBuilder();
      db.domain(DOMAIN);
      return db;
    }

    protected DatasourcePermissionBuilder newCopy() {
      return new DatasourcePermissionBuilder(this);
    }

    public ValueTablePermissionBuilder tables() {
      // TODO: should we add "/tables" to the path?
      return new ValueTablePermissionBuilder(this)/* .instance("tables") */;
    }

    public ValueTablePermissionBuilder table(ValueTable table) {
      return new ValueTablePermissionBuilder(this).instance("table").instance(table.getName());
    }

    public ValueTablePermissionBuilder table(String name) {
      return new ValueTablePermissionBuilder(this).instance("table").instance(name);
    }

    public Predicate<Datasource> asPredicate(final Authorizer authorizer) {
      return new Predicate<Datasource>() {

        @Override
        public boolean apply(Datasource input) {
          return authorizer.isPermitted(newCopy().instance("datasource").instance(input.getName()).build());
        }
      };
    }
  }

  @SuppressWarnings("StaticMethodOnlyUsedInOneClass")
  public static class ValueTablePermissionBuilder extends Builder<ValueTablePermissionBuilder> {

    ValueTablePermissionBuilder(DatasourcePermissionBuilder builder) {
      super(builder);
    }

    private ValueTablePermissionBuilder(ValueTablePermissionBuilder builder) {
      super(builder);
    }

    protected ValueTablePermissionBuilder newCopy() {
      return new ValueTablePermissionBuilder(this);
    }

    public static ValueTablePermissionBuilder forValueTable(ValueTable table) {
      return DatasourcePermissionBuilder.forDatasource(table.getDatasource()).table(table);
    }

    public VariablePermissionBuilder variable(Variable variable) {
      return new VariablePermissionBuilder(this).instance("variable").instance(variable.getName());
    }

    public VariablePermissionBuilder variable(String variable) {
      return new VariablePermissionBuilder(this).instance("variable").instance(variable);
    }

    public Predicate<ValueTable> asPredicate(final Authorizer authorizer) {
      return new Predicate<ValueTable>() {

        @Override
        public boolean apply(ValueTable input) {
          return authorizer.isPermitted(newCopy().instance("table").instance(input.getName()).build());
        }
      };
    }

  }

  @SuppressWarnings("UnusedDeclaration")
  public static class VariablePermissionBuilder extends Builder<VariablePermissionBuilder> {

    public VariablePermissionBuilder(ValueTablePermissionBuilder builder) {
      super(builder);
    }

    private VariablePermissionBuilder(VariablePermissionBuilder builder) {
      super(builder);
    }

    protected VariablePermissionBuilder newCopy() {
      return new VariablePermissionBuilder(this);
    }

    public Predicate<Variable> asPredicate(final Authorizer authorizer) {
      return new Predicate<Variable>() {

        @Override
        public boolean apply(Variable input) {
          return authorizer.isPermitted(newCopy().instance("variable").instance(input.getName()).build());
        }
      };
    }
  }
}
