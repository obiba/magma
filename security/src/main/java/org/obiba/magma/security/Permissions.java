package org.obiba.magma.security;

public class Permissions {

  public static final String WILDCARD = "*";

  public static final class Domains {
    public static final String DATASOURCE = "magma";

    public static final String TABLE = "table";
  }

  public static final class Actions {

    public static final String READ = "read";

    public static final String WRITE = "write";
  }

  public final static class Builder {

    private String domain;

    private String action;

    private String instance;

    public static Builder create() {
      return new Builder();
    }

    public Builder domain(String domain) {
      this.domain = domain;
      return this;
    }

    public Builder anyDomain() {
      this.domain = WILDCARD;
      return this;
    }

    public Builder action(String action) {
      this.action = action;
      return this;
    }

    public Builder anyAction() {
      this.action = WILDCARD;
      return this;
    }

    public Builder read() {
      this.action = Actions.READ;
      return this;
    }

    public Builder instance(String instance) {
      this.instance = instance;
      return this;
    }

    public Builder anyInstance() {
      this.instance = WILDCARD;
      return this;
    }

    public String build() {
      return appendNonNull(appendNonNull(domain, action), instance);
    }

    /**
     * Appends <code>":" + segment</code> to <code>permission</code> when <code>segment</code> is non-null.
     */
    private String appendNonNull(String permission, String segment) {
      return permission + (segment != null ? ":" + segment : "");
    }
  }

}
