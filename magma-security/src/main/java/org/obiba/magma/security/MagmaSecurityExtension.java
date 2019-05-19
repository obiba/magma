/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.magma.security;

import org.obiba.magma.DatasourceRegistry;
import org.obiba.magma.Decorator;
import org.obiba.magma.MagmaEngine;
import org.obiba.magma.MagmaEngineExtension;
import org.obiba.magma.security.shiro.ShiroAuthorizer;
import org.obiba.magma.support.Initialisables;

public class MagmaSecurityExtension implements MagmaEngineExtension {

//  private static final Logger log = LoggerFactory.getLogger(MagmaSecurityExtension.class);

  private static final long serialVersionUID = 8901321475619160822L;

  private final Authorizer authorizer = new ShiroAuthorizer();

  @Override
  public String getName() {
    return "security";
  }

  public Authorizer getAuthorizer() {
    return authorizer;
  }

  @Override
  public void initialise() {
    Initialisables.initialise(authorizer);
    MagmaEngine.get().decorate(new Decorator<DatasourceRegistry>() {

      @Override
      public DatasourceRegistry decorate(DatasourceRegistry object) {
        return new SecuredDatasourceRegistry(authorizer, object);
      }

      @Override
      public void release(DatasourceRegistry object) {

      }
    });

  }

}
