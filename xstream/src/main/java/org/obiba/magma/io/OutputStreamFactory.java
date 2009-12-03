package org.obiba.magma.io;

import java.io.File;
import java.io.OutputStream;

public interface OutputStreamFactory {

  public OutputStream createOutputStream(File file);

}
