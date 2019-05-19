/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.magma.crypt.support;

import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.PublicKey;
import java.security.cert.Certificate;

import org.obiba.magma.Datasource;
import org.obiba.magma.crypt.MagmaCryptRuntimeException;
import org.obiba.magma.crypt.NoSuchKeyException;
import org.obiba.magma.crypt.PublicKeyProvider;

/**
 * Looks for a {@code java.security.cert.Certificate} in a {@code KeyStore} using the {@code Datasource}'s name as the
 * certificate alias.
 */
public class KeyStorePublicKeyProvider implements PublicKeyProvider {

  private final KeyStore keyStore;

  public KeyStorePublicKeyProvider(KeyStore keyStore) {
    this.keyStore = keyStore;
  }

  @Override
  public PublicKey getPublicKey(Datasource datasource) throws NoSuchKeyException {
    try {
      Certificate cert = keyStore.getCertificate(datasource.getName());
      if(cert != null) {
        return cert.getPublicKey();
      }
    } catch(KeyStoreException e) {
      throw new MagmaCryptRuntimeException(e);
    }
    throw new NoSuchKeyException(datasource.getName(), "No PublicKey for Datasource '" + datasource.getName() + "'");
  }

}
