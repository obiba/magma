/*******************************************************************************
 * Copyright (c) 2012 OBiBa. All rights reserved.
 *  
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *  
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.magma.support;

import java.io.InputStream;
import java.net.URL;

import org.obiba.magma.MagmaRuntimeException;
import org.obiba.magma.ValueLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.io.ByteStreams;

/**
 * Loads a value for a url.
 */
public class BinaryValueURLLoader implements ValueLoader {

  private static final Logger log = LoggerFactory.getLogger(BinaryValueURLLoader.class);

  private String url;

  private byte[] value;

  public BinaryValueURLLoader(String url) {
    this.url = url;
  }

  @Override
  public boolean isNull() {
    return url == null || url.length() == 0;
  }

  @Override
  public Object getValue() {
    if(value == null) {
      try {
        log.info("Loading binary from: {}", url);
        try {
          throw new MagmaRuntimeException("coucou");
        } catch(MagmaRuntimeException e) {
          e.printStackTrace();
        }
        URL u = new URL(url);
        InputStream in = u.openStream();
        value = ByteStreams.toByteArray(in);
        in.close();
        log.debug("Binary loaded from: {}", url);
      } catch(Exception e) {
        value = null;
        throw new MagmaRuntimeException("URL cannot be read: " + url, e);
      }
    }
    return value;
  }

}