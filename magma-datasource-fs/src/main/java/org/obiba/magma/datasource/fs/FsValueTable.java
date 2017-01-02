/*
 * Copyright (c) 2017 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.magma.datasource.fs;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Reader;
import java.io.Writer;

import javax.validation.constraints.NotNull;

import org.obiba.magma.AbstractVariableValueSource;
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
  private final File valueTableEntry;

  /**
   * XStream instance used to de/serialize instances.
   */
  private final XStream xstream;

  /**
   * Our VariableEntityProvider instance
   */
  private final FsVariableEntityProvider variableEntityProvider;

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

  @NotNull
  @Override
  public FsDatasource getDatasource() {
    return (FsDatasource) super.getDatasource();
  }

  @Override
  public ValueSet getValueSet(VariableEntity entity) throws NoSuchValueSetException {
    return new LazyValueSet(this, entity);
  }

  @NotNull
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

  private void readVariables() {
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

  private static class FsVariableValueSource extends AbstractVariableValueSource implements VariableValueSource {

    private final Variable variable;

    private FsVariableValueSource(Variable variable) {
      this.variable = variable;
    }

    @NotNull
    @Override
    public Variable getVariable() {
      return variable;
    }

    @NotNull
    @Override
    public ValueType getValueType() {
      return variable.getValueType();
    }

    @NotNull
    @Override
    public Value getValue(ValueSet valueSet) {
      return ((LazyValueSet) valueSet).getValueSet().getValue(variable);
    }

    @Override
    public boolean supportVectorSource() {
      return false;
    }

    @NotNull
    @Override
    public VectorSource asVectorSource() {
      throw new MagmaRuntimeException("FS Datasource does not support vector source");
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

    private XStreamValueSet readValueSet(VariableEntity entity) {
      String entryName = variableEntityProvider.getEntityFile(entity);
      if(entryName == null) {
        throw new NoSuchValueSetException(FsValueTable.this, entity);
      }
      return readEntry(entryName, new InputCallback<XStreamValueSet>() {
        @Override
        public XStreamValueSet readEntry(Reader reader) throws IOException {
          return (XStreamValueSet) xstream.fromXML(reader);
        }
      });
    }

  }

  @NotNull
  @Override
  public Timestamps getTimestamps() {
    return new FsTimestamps(valueTableEntry);
  }

}
