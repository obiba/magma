package org.obiba.magma.datasource.fs;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Reader;
import java.io.Writer;

import org.obiba.magma.Disposable;
import org.obiba.magma.Initialisable;
import org.obiba.magma.MagmaRuntimeException;
import org.obiba.magma.NoSuchValueSetException;
import org.obiba.magma.Timestamps;
import org.obiba.magma.Value;
import org.obiba.magma.ValueSet;
import org.obiba.magma.ValueTable;
import org.obiba.magma.ValueType;
import org.obiba.magma.Variable;
import org.obiba.magma.VariableEntity;
import org.obiba.magma.VariableValueSource;
import org.obiba.magma.VectorSource;
import org.obiba.magma.datasource.fs.FsDatasource.InputCallback;
import org.obiba.magma.datasource.fs.FsDatasource.OutputCallback;
import org.obiba.magma.support.AbstractValueTable;
import org.obiba.magma.support.ValueSetBean;
import org.obiba.magma.xstream.XStreamValueSet;

import com.thoughtworks.xstream.XStream;

import de.schlichtherle.io.File;

class FsValueTable extends AbstractValueTable implements Initialisable, Disposable {

  /**
   * The directory of this {@code ValueTable}
   */
  private File valueTableEntry;

  /**
   * XStream instance used to de/serialize instances.
   */
  private XStream xstream;

  /**
   * Our VariableEntityProvider instance
   */
  private FsVariableEntityProvider variableEntityProvider;

  FsValueTable(FsDatasource datasource, String name) {
    super(datasource, name);
    valueTableEntry = datasource.getEntry(name);
    xstream = datasource.getXStreamInstance();
    setVariableEntityProvider(variableEntityProvider = new FsVariableEntityProvider(this));
  }

  FsValueTable(FsDatasource datasource, String name, String entityType) {
    super(datasource, name);
    valueTableEntry = datasource.getEntry(name);
    xstream = datasource.getXStreamInstance();
    setVariableEntityProvider(variableEntityProvider = new FsVariableEntityProvider(this, entityType));
  }

  @Override
  public void initialise() {
    super.initialise();
    try {
      variableEntityProvider.initialise();
      readVariables();
    } catch(RuntimeException e) {
      throw e;
    } catch(Exception e) {
      throw new MagmaRuntimeException(e);
    }
  }

  @Override
  public void dispose() {
    variableEntityProvider.dispose();
  }

  @Override
  public FsDatasource getDatasource() {
    return (FsDatasource) super.getDatasource();
  }

  @Override
  public ValueSet getValueSet(VariableEntity entity) throws NoSuchValueSetException {
    return new LazyValueSet(this, entity);
  }

  @Override
  public FsVariableEntityProvider getVariableEntityProvider() {
    return variableEntityProvider;
  }

  File getEntry(String name) {
    return new File(valueTableEntry, name);
  }

  <T> T readEntry(String name, InputCallback<T> callback) {
    return getDatasource().readEntry(getEntry(name), callback);
  }

  <T> T writeEntry(String name, OutputCallback<T> callback) {
    return getDatasource().writeEntry(getEntry(name), callback);
  }

  Writer createWriter(String name) {
    return getDatasource().createWriter(getEntry(name));
  }

  private void readVariables() throws IOException {
    readEntry("variables.xml", new InputCallback<Void>() {
      @SuppressWarnings("InfiniteLoopStatement")
      @Override
      public Void readEntry(Reader reader) throws IOException {
        ObjectInputStream ois = xstream.createObjectInputStream(reader);
        try {
          while(true) {
            Variable variable = (Variable) ois.readObject();
            addVariableValueSource(new FsVariableValueSource(variable));
          }
        } catch(EOFException e) {
          // We reached the end of the ois.
        } catch(ClassNotFoundException e) {
          throw new MagmaRuntimeException(e);
        }
        return null;
      }
    });

  }

  private XStreamValueSet readValueSet(VariableEntity entity) {
    String entryName = variableEntityProvider.getEntityFile(entity);
    if(entryName == null) {
      throw new NoSuchValueSetException(this, entity);
    }
    return readEntry(entryName, new InputCallback<XStreamValueSet>() {
      @Override
      public XStreamValueSet readEntry(Reader reader) throws IOException {
        return (XStreamValueSet) xstream.fromXML(reader);
      }
    });
  }

  private static class FsVariableValueSource implements VariableValueSource {

    private Variable variable;

    FsVariableValueSource(Variable variable) {
      this.variable = variable;
    }

    @Override
    public Variable getVariable() {
      return variable;
    }

    @Override
    public ValueType getValueType() {
      return variable.getValueType();
    }

    @Override
    public Value getValue(ValueSet valueSet) {
      return ((LazyValueSet) valueSet).getValueSet().getValue(variable);
    }

    @Override
    public VectorSource asVectorSource() {
      return null;
    }

  }

  private class LazyValueSet extends ValueSetBean {

    @SuppressWarnings("TransientFieldInNonSerializableClass")
    private transient volatile XStreamValueSet valueSet;

    LazyValueSet(ValueTable table, VariableEntity entity) {
      super(table, entity);
    }

    XStreamValueSet getValueSet() {
      if(valueSet == null) {
        valueSet = readValueSet(getVariableEntity());
      }
      return valueSet;
    }

  }

  @Override
  public Timestamps getTimestamps() {
    return new FsTimestamps(valueTableEntry);
  }

}
