package org.obiba.magma.datasource.fs;

import java.io.OutputStream;

import de.schlichtherle.io.File;

public interface OutputStreamWrapper {

  public OutputStream wrap(OutputStream os, File file);

}
