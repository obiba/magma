package org.obiba.magma.io;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;

import org.obiba.magma.Initialisable;
import org.obiba.magma.Value;
import org.obiba.magma.ValueTableWriter;
import org.obiba.magma.ValueType;
import org.obiba.magma.Variable;
import org.obiba.magma.VariableEntity;
import org.obiba.magma.xstream.XStreamValueSet;
import org.obiba.magma.xstream.XStreamValueSetValue;
import org.obiba.magma.xstream.converter.AttributeConverter;
import org.obiba.magma.xstream.converter.CategoryConverter;
import org.obiba.magma.xstream.converter.ValueConverter;
import org.obiba.magma.xstream.converter.ValueSequenceConverter;
import org.obiba.magma.xstream.converter.VariableConverter;
import org.obiba.magma.xstream.mapper.MagmaMapper;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.mapper.MapperWrapper;

import de.schlichtherle.io.File;
import de.schlichtherle.io.FileOutputStream;

public class FsValueTableWriter implements ValueTableWriter, Initialisable {

  private File archive;

  private String valueTable;

  private XStream xstream;

  public FsValueTableWriter(File archive, String valueTable) {
    this.archive = archive;
    this.valueTable = valueTable;
  }

  @Override
  public ValueSetWriter writeValueSet(VariableEntity entity) {
    String name = valueTable + '/' + entity.getIdentifier() + ".xml";
    File f = new File(archive, name);
    try {
      return new XStreamValueSetWriter(new FileOutputStream(f), new XStreamValueSet(valueTable, entity));
    } catch(IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public VariableWriter writeVariables(String entityType) {
    String name = valueTable + '/' + "variables.xml";
    File f = new File(archive, name);
    try {
      return new XStreamVariableWriter(new FileOutputStream(f));
    } catch(IOException e) {
      throw new RuntimeException(e);
    }
  }

  public void initialise() {
    xstream = new XStream() {
      @Override
      protected MapperWrapper wrapMapper(MapperWrapper next) {
        return new MagmaMapper(next);
      }
    };
    xstream.registerConverter(new VariableConverter(xstream.getMapper()));
    xstream.registerConverter(new CategoryConverter(xstream.getMapper()));
    xstream.registerConverter(new AttributeConverter());
    xstream.registerConverter(new ValueConverter());
    xstream.registerConverter(new ValueSequenceConverter());
    xstream.useAttributeFor(ValueType.class);
    xstream.setMode(XStream.NO_REFERENCES);

    xstream.processAnnotations(XStreamValueSet.class);
    xstream.processAnnotations(XStreamValueSetValue.class);

  }

  @Override
  public void close() throws IOException {
    File.umount(archive);
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
