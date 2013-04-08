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
    OutputStream wrapped = os;
    for(OutputStreamWrapper factory : factories) {
      wrapped = factory.wrap(wrapped, file);
    }
    return wrapped;
  }
}
