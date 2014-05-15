/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.magma.crypt.support;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;

public interface CachingCallbackHandler extends CallbackHandler {

  void cacheCallbackResult(Callback callback);
}
