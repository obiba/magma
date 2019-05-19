/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.magma.security.shiro;

import java.util.concurrent.Callable;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.session.Session;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.util.ThreadContext;
import org.obiba.magma.security.Authorizer;
import org.obiba.shiro.authc.SudoAuthToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ShiroAuthorizer implements Authorizer {

  private static final long serialVersionUID = -3683635184800703377L;

  private static final Logger log = LoggerFactory.getLogger(ShiroAuthorizer.class);

  @Override
  public boolean isPermitted(String permission) {
    Subject subject = ThreadContext.getSubject();
    boolean p = subject == null ? false : subject.isPermitted(permission);
    log.debug(String.format("isPermitted(%s, %s)==%s", SecurityUtils.getSubject().getPrincipal(), permission, p));
    Session session = SecurityUtils.getSubject().getSession(false);
    if(session != null) session.touch();
    return p;
  }

  @Override
  public <V> V silentSudo(Callable<V> call) {
    try {
      return sudo(call);
    } catch(Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public <V> V sudo(Callable<V> call) throws Exception {
    return sudoSubject().execute(call);
  }

  /**
   * Tries to authenticate the current subject with a {@link SudoAuthToken}. If successful, this method returns the
   * {@code Subject} instance to use to run the privileged code.
   *
   * @return a {@code Subject} instance for performing the privileged action
   */
  protected Subject sudoSubject() throws AuthenticationException {
    return new Subject.Builder().principals(
        SecurityUtils.getSecurityManager().authenticate(new SudoAuthToken(SecurityUtils.getSubject())).getPrincipals())
        .authenticated(true).buildSubject();
  }
}
