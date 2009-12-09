package org.obiba.magma.datasource.fs.input;

import java.io.InputStream;

import javax.crypto.CipherInputStream;

import org.obiba.magma.datasource.crypt.DatasourceCipherFactory;
import org.obiba.magma.datasource.fs.InputStreamWrapper;

import de.schlichtherle.io.File;

public class CipherInputStreamWrapper implements InputStreamWrapper {

  private DatasourceCipherFactory cipherProvider;

  public CipherInputStreamWrapper(DatasourceCipherFactory cipherProvider) {
    this.cipherProvider = cipherProvider;
  }

  @Override
  public InputStream wrap(InputStream is, File file) {
    return new CipherInputStream(is, cipherProvider.createDecryptingCipher());
  }

}
