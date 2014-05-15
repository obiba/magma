package org.obiba.magma.datasource.fs;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Writer;

import javax.validation.constraints.NotNull;

import org.obiba.magma.Value;
import org.obiba.magma.ValueTableWriter;
import org.obiba.magma.Variable;
import org.obiba.magma.VariableEntity;
import org.obiba.magma.xstream.XStreamValueSet;

import com.thoughtworks.xstream.XStream;

class FsValueTableWriter implements ValueTableWriter {

  private final FsValueTable valueTable;

  private final XStream xstream;

  FsValueTableWriter(FsValueTable valueTable, XStream xstream) {
    this.valueTable = valueTable;
    this.xstream = xstream;
  }

  @NotNull
  @Override
  public ValueSetWriter writeValueSet(@NotNull VariableEntity entity) {
    String entry = valueTable.getVariableEntityProvider().addEntity(entity);
    try {
      return new XStreamValueSetWriter(valueTable.createWriter(entry),
          new XStreamValueSet(valueTable.getName(), entity));
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
  public void close() {
  }

  private class XStreamVariableWriter implements VariableWriter {

    ObjectOutputStream oos;

    XStreamVariableWriter(Writer os) throws IOException {
      oos = xstream.createObjectOutputStream(os, "variables");
    }

    @Override
    public void close() {
      try {
        oos.close();
      } catch(IOException e) {
        throw new RuntimeException(e);
      }
    }

    @edu.umd.cs.findbugs.annotations.SuppressWarnings(value = "DMI_NONSERIALIZABLE_OBJECT_WRITTEN",
        justification = "XStream implementation of ObjectOutputStream does not expect or require objects to implement the Serializable marker interface. http://xstream.codehaus.org/faq.html#Serialization")
    @Override
    public void writeVariable(@NotNull Variable variable) {
      try {
        oos.writeObject(variable);
      } catch(IOException e) {
        throw new RuntimeException(e);
      }
    }

    @Override
    public void removeVariable(@NotNull Variable variable) {
      throw new UnsupportedOperationException("Variable cannot be removed from a XML file");
    }
  }

  private class XStreamValueSetWriter implements ValueSetWriter {

    private final Writer os;

    private final XStreamValueSet valueSet;

    private XStreamValueSetWriter(Writer os, XStreamValueSet valueSet) throws IOException {
      this.os = os;
      this.valueSet = valueSet;
    }

    @Override
    public void close() {
      try {
        xstream.toXML(valueSet, os);
      } finally {
        try {
          os.close();
        } catch(IOException ignored) {
        }
      }
    }

    @Override
    public void writeValue(@NotNull Variable variable, Value value) {
      valueSet.setValue(variable, value);
    }
  }
}
