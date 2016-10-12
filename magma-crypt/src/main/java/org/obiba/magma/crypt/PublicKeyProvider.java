/*
 * Copyright (c) 2016 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.magma.crypt;

import java.security.PublicKey;

import org.obiba.magma.Datasource;

/**
 * A simple interface for mapping a {@code PublicKey} to a name.
 * <p/>
 * A {@code KeyStore} is a type of {@code PublicKeyProvider}, since it can contain a public key mapped to an alias.
 */
public interface PublicKeyProvider {

  /**
   * Returns the {@code PublicKey} for the specified {@code datasource}.
   *
   * @param name
   * @return
   */
  PublicKey getPublicKey(Datasource datasource) throws NoSuchKeyException;

}
