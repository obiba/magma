/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/

package org.obiba.magma.io.output;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.security.DigestOutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.obiba.magma.MagmaRuntimeException;
import org.obiba.magma.io.OutputStreamWrapper;

import de.schlichtherle.io.File;

/**
 * 
 */
public class DigestOutputStreamWrapper implements OutputStreamWrapper {

  private String digestType = "SHA-512";

  private String entrySuffix = ".sha512";

  public void setDigestType(String digestType) {
    this.digestType = digestType;
  }

  public void setEntrySuffix(String entrySuffix) {
    this.entrySuffix = entrySuffix;
  }

  @Override
  public OutputStream wrap(OutputStream os, File entry) {
    File digestFile = new File(entry.getParent() + '/' + entry.getName() + entrySuffix);
    return new WrappedDigestOutputStream(os, digestFile);
  }

  protected MessageDigest newDigest() {
    try {
      return MessageDigest.getInstance(digestType);
    } catch(NoSuchAlgorithmException e) {
      throw new MagmaRuntimeException(e);
    }
  }

  private class WrappedDigestOutputStream extends DigestOutputStream {

    private File digestEntry;

    public WrappedDigestOutputStream(OutputStream stream, File digestEntry) {
      super(stream, newDigest());
      this.digestEntry = digestEntry;
    }

    public void close() throws IOException {
      super.close();

      ByteArrayInputStream bais = new ByteArrayInputStream(getMessageDigest().digest());
      try {
        digestEntry.catFrom(bais);
      } finally {
        bais.close();
      }
    }
  }
}
