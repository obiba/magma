package org.obiba.magma.datasource.fs.output;

import java.io.OutputStream;

import javax.crypto.CipherOutputStream;

import org.obiba.magma.datasource.crypt.DatasourceCipherFactory;
import org.obiba.magma.datasource.fs.OutputStreamWrapper;

import de.schlichtherle.io.File;

public class CipherOutputStreamWrapper implements OutputStreamWrapper {

  private final DatasourceCipherFactory cipherProvider;

  public CipherOutputStreamWrapper(DatasourceCipherFactory cipherProvider) {
    this.cipherProvider = cipherProvider;
  }

  @Override
  public OutputStream wrap(OutputStream os, File file) {
    return new CipherOutputStream(os, cipherProvider.createEncryptingCipher());
  }

}
