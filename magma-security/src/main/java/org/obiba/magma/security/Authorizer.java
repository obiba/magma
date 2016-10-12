/*
 * Copyright (c) 2016 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.magma.security;

import java.io.Serializable;
import java.util.concurrent.Callable;

public interface Authorizer extends Serializable {

  boolean isPermitted(String permission);

  <V> V silentSudo(Callable<V> sudo);

  <V> V sudo(Callable<V> sudo) throws Exception;

}
