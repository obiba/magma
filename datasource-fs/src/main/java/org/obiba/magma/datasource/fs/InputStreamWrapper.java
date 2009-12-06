package org.obiba.magma.datasource.fs;

import java.io.InputStream;

import de.schlichtherle.io.File;

public interface InputStreamWrapper {

  public InputStream wrap(InputStream is, File file);

}
