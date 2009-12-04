package org.obiba.magma.io;

import java.io.OutputStream;

import de.schlichtherle.io.File;

public interface OutputStreamWrapper {

  public OutputStream wrap(OutputStream os, File file);

}
