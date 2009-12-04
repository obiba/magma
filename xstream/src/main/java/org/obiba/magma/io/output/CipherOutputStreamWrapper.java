package org.obiba.magma.io.output;

import java.io.OutputStream;

import javax.crypto.Cipher;
import javax.crypto.CipherOutputStream;

import org.obiba.magma.io.OutputStreamWrapper;

import de.schlichtherle.io.File;

public class CipherOutputStreamWrapper implements OutputStreamWrapper {

  private Cipher cipher;

  public CipherOutputStreamWrapper(Cipher cipher) {
    this.cipher = cipher;
  }

  @Override
  public OutputStream wrap(OutputStream os, File file) {
    return new CipherOutputStream(os, cipher);
  }

}
