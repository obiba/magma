package org.obiba.magma.io.input;

import java.io.InputStream;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;

import org.obiba.magma.io.InputStreamWrapper;

import de.schlichtherle.io.File;

public class CipherInputStreamWrapper implements InputStreamWrapper {

  private Cipher cipher;

  public CipherInputStreamWrapper(Cipher cipher) {
    this.cipher = cipher;
  }

  @Override
  public InputStream wrap(InputStream is, File file) {
    return new CipherInputStream(is, cipher);
  }

}
