package org.obiba.magma.io;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;

import org.obiba.magma.Initialisable;
import org.obiba.magma.MagmaEngine;
import org.obiba.magma.MagmaRuntimeException;
import org.obiba.magma.Value;
import org.obiba.magma.ValueTableWriter;
import org.obiba.magma.Variable;
import org.obiba.magma.VariableEntity;
import org.obiba.magma.xstream.MagmaXStreamExtension;
import org.obiba.magma.xstream.XStreamValueSet;

import com.thoughtworks.xstream.XStream;

import de.schlichtherle.io.File;
import de.schlichtherle.io.FileOutputStream;

class FsValueTableWriter implements ValueTableWriter, Initialisable {

  private File archive;

  private String valueTable;

  private XStream xstream;

  private OutputStreamWrapper outputStreamWrapper;

  public FsValueTableWriter(File archive, String valueTable, OutputStreamWrapper outputStreamWrapper) {
    this.archive = archive;
    this.valueTable = valueTable;
    this.outputStreamWrapper = outputStreamWrapper;
  }

  @Override
  public ValueSetWriter writeValueSet(VariableEntity entity) {
    String name = valueTable + '/' + entity.getIdentifier() + ".xml";
    File file = new File(archive, name);
    try {
      return new XStreamValueSetWriter(newStream(file), new XStreamValueSet(valueTable, entity));
    } catch(IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public VariableWriter writeVariables(String entityType) {
    String name = valueTable + '/' + "variables.xml";
    File file = new File(archive, name);
    try {
      return new XStreamVariableWriter(newStream(file));
    } catch(IOException e) {
      throw new RuntimeException(e);
    }
  }

  public void initialise() {
    xstream = MagmaEngine.get().getExtension(MagmaXStreamExtension.class).getXStreamFactory().createXStream();
  }

  @Override
  public void close() throws IOException {
    File.update(archive);
  }

  protected OutputStream newStream(File file) {
    try {
      return outputStreamWrapper.wrap(new FileOutputStream(file), file);
    } catch(FileNotFoundException e) {
      throw new MagmaRuntimeException(e);
    }
  }

  private class XStreamVariableWriter implements VariableWriter {

    ObjectOutputStream oos;

    XStreamVariableWriter(OutputStream os) throws IOException {
      oos = xstream.createObjectOutputStream(os, "variables");
    }

    @Override
    public void close() throws IOException {
      oos.close();
    }

    @Override
    public void writeVariable(Variable variable) {
      try {
        oos.writeObject(variable);
      } catch(IOException e) {
        throw new RuntimeException(e);
      }
    }
  }

  private class XStreamValueSetWriter implements ValueSetWriter {

    OutputStream os;

    XStreamValueSet valueSet;

    XStreamValueSetWriter(OutputStream os, XStreamValueSet valueSet) throws IOException {
      this.os = os;
      this.valueSet = valueSet;
    }

    @Override
    public void close() throws IOException {
      xstream.toXML(valueSet, os);
      os.close();
    }

    @Override
    public void writeValue(Variable variable, Value value) {
      valueSet.addValue(variable, value);
    }
  }
}
