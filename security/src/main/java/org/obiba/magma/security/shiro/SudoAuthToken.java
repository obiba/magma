package org.obiba.magma.security.shiro;

import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.Subject;

/**
 * {@code AuthorizationToken} for performing an action with elevated privileges.
 * <p/>
 * TODO: add the action being performed.
 */
public class SudoAuthToken implements AuthenticationToken {

  private static final long serialVersionUID = 4956112777374283844L;

  private final PrincipalCollection sudoer;

  public SudoAuthToken(Subject sudoer) {
    this.sudoer = sudoer.getPrincipals();
  }

  @Override
  public Object getPrincipal() {
    return sudoer.getPrimaryPrincipal();
  }

  @Override
  public Object getCredentials() {
    return null;
  }

  public PrincipalCollection getSudoer() {
    return sudoer;
  }
}
