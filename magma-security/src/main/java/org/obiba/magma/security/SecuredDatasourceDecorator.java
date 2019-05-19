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

import org.obiba.magma.Datasource;
import org.obiba.magma.Decorator;

public class SecuredDatasourceDecorator implements Decorator<Datasource> {

  private final Authorizer authorizer;

  public SecuredDatasourceDecorator(Authorizer authorizer) {
    if(authorizer == null) throw new IllegalArgumentException("authorizer cannot be null");
    this.authorizer = authorizer;
  }

  @Override
  public Datasource decorate(Datasource datasource) {
    if(datasource == null) throw new IllegalArgumentException("datasource cannot be null");
    return new SecuredDatasource(authorizer, datasource);
  }

  @Override
  public void release(Datasource object) {

  }

  public Datasource undecorate(Datasource datasource) {
    if(datasource instanceof SecuredDatasource) {
      SecuredDatasource secured = (SecuredDatasource) datasource;
      return secured.getWrappedDatasource();
    }
    return datasource;
  }

}
