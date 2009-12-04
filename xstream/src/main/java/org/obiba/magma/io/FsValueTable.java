package org.obiba.magma.io;

import java.io.EOFException;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Set;

import org.obiba.magma.Initialisable;
import org.obiba.magma.MagmaEngine;
import org.obiba.magma.MagmaRuntimeException;
import org.obiba.magma.NoSuchValueSetException;
import org.obiba.magma.Value;
import org.obiba.magma.ValueSet;
import org.obiba.magma.ValueType;
import org.obiba.magma.Variable;
import org.obiba.magma.VariableEntity;
import org.obiba.magma.VariableValueSource;
import org.obiba.magma.support.AbstractValueTable;
import org.obiba.magma.support.AbstractVariableEntityProvider;
import org.obiba.magma.support.ValueSetBean;
import org.obiba.magma.support.VariableEntityBean;
import org.obiba.magma.xstream.MagmaXStreamExtension;
import org.obiba.magma.xstream.XStreamValueSet;

import com.google.common.collect.Sets;
import com.thoughtworks.xstream.XStream;

import de.schlichtherle.io.File;
import de.schlichtherle.io.FileInputStream;

class FsValueTable extends AbstractValueTable implements Initialisable {

  private File tableDirectory;

  private XStream xstream;

  private Set<VariableEntity> entities = Sets.newHashSet();

  private String entityType;

  public FsValueTable(FsDatasource datasource, String name) {
    super(datasource, name);
    tableDirectory = new File(datasource.getFile(), name);
  }

  @Override
  public void initialise() {
    super.initialise();
    xstream = MagmaEngine.get().getExtension(MagmaXStreamExtension.class).getXStreamFactory().createXStream();
    try {
      readVariables();

      setVariableEntityProvider(new AbstractVariableEntityProvider(entityType) {

        @Override
        public Set<VariableEntity> getVariableEntities() {
          return entities;
        }
      });

      readEntities();
    } catch(RuntimeException e) {
      throw e;
    } catch(Exception e) {
      throw new MagmaRuntimeException(e);
    }
  }

  @Override
  public FsDatasource getDatasource() {
    return (FsDatasource) super.getDatasource();
  }

  @Override
  public ValueSet getValueSet(VariableEntity entity) throws NoSuchValueSetException {
    return new ValueSetBean(this, entity);
  }

  private void readVariables() throws FileNotFoundException, IOException {
    File variables = new File(tableDirectory, "variables.xml");
    ObjectInputStream ois = xstream.createObjectInputStream(new FileInputStream(variables));
    try {
      while(true) {
        Variable variable = (Variable) ois.readObject();
        entityType = variable.getEntityType();
        super.addVariableValueSource(new FsVariableValueSource(variable));
      }
    } catch(EOFException e) {
      // We reached the end of the ois.
    } catch(ClassNotFoundException e) {
      throw new MagmaRuntimeException(e);
    } finally {
      ois.close();
    }
  }

  private void readEntities() throws FileNotFoundException, IOException {
    String[] entries = tableDirectory.list();
    for(String entry : entries) {
      if(entry.matches("\\d+\\.xml$")) {
        entities.add(new VariableEntityBean(getEntityType(), entry.replace(".xml", "")));
      }
    }
  }

  private XStreamValueSet readValueSet(ValueSet valueSet) {
    try {
      return (XStreamValueSet) xstream.fromXML(new FileInputStream(new File(tableDirectory, valueSet.getVariableEntity().getIdentifier() + ".xml")));
    } catch(FileNotFoundException e) {
      throw new NoSuchValueSetException(valueSet.getVariableEntity());
    } catch(RuntimeException e) {
      throw new MagmaRuntimeException(valueSet.toString(), e);
    }
  }

  private class FsVariableValueSource implements VariableValueSource {

    private Variable variable;

    public FsVariableValueSource(Variable variable) {
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
      return readValueSet(valueSet).getValue(variable);
    }

  }

}
