package org.obiba.magma.security.shiro;

import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.subject.Subject;

/**
 * {@code AuthorizationToken} for performing an action with elevated privileges.
 * <p>
 * TODO: add the action being performed.
 */
public class SudoAuthToken implements AuthenticationToken {

  private static final long serialVersionUID = 4956112777374283844L;

  private final Subject sudoer;

  public SudoAuthToken(Subject sudoer) {
    this.sudoer = sudoer;
  }

  @Override
  public Object getPrincipal() {
    return sudoer.getPrincipal();
  }

  @Override
  public Object getCredentials() {
    return null;
  }
}
