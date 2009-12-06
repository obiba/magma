package org.obiba.magma.datasource.fs.output;

import java.io.OutputStream;
import java.util.Arrays;
import java.util.List;

import org.obiba.magma.datasource.fs.OutputStreamWrapper;

import de.schlichtherle.io.File;

public class ChainedOutputStreamWrapper implements OutputStreamWrapper {

  List<OutputStreamWrapper> factories;

  public ChainedOutputStreamWrapper(List<OutputStreamWrapper> factories) {
    this.factories = factories;
  }

  public ChainedOutputStreamWrapper(OutputStreamWrapper... factories) {
    this(Arrays.asList(factories));
  }

  @Override
  public OutputStream wrap(OutputStream os, File file) {
    for(OutputStreamWrapper factory : factories) {
      os = factory.wrap(os, file);
    }
    return os;
  }
}
