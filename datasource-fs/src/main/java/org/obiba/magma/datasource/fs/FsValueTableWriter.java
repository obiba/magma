package org.obiba.magma.datasource.fs;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Writer;

import org.obiba.magma.Value;
import org.obiba.magma.ValueTableWriter;
import org.obiba.magma.Variable;
import org.obiba.magma.VariableEntity;
import org.obiba.magma.xstream.XStreamValueSet;

import com.thoughtworks.xstream.XStream;

class FsValueTableWriter implements ValueTableWriter {

  private FsValueTable valueTable;

  private XStream xstream;

  public FsValueTableWriter(FsValueTable valueTable, XStream xstream) {
    this.valueTable = valueTable;
    this.xstream = xstream;
  }

  @Override
  public ValueSetWriter writeValueSet(VariableEntity entity) {
    String entry = valueTable.getVariableEntityProvider().addEntity(entity);
    try {
      return new XStreamValueSetWriter(valueTable.createWriter(entry), new XStreamValueSet(valueTable.getName(), entity));
    } catch(IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public VariableWriter writeVariables() {
    try {
      return new XStreamVariableWriter(valueTable.createWriter("variables.xml"));
    } catch(IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void close() throws IOException {
  }

  private class XStreamVariableWriter implements VariableWriter {

    ObjectOutputStream oos;

    XStreamVariableWriter(Writer os) throws IOException {
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

    Writer os;

    XStreamValueSet valueSet;

    XStreamValueSetWriter(Writer os, XStreamValueSet valueSet) throws IOException {
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
      valueSet.setValue(variable, value);
    }
  }
}
