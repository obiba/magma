/*
 * Copyright (c) 2012 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.magma.datasource.spss;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.SortedSet;

import javax.validation.constraints.NotNull;

import org.obiba.magma.AbstractVariableValueSource;
import org.obiba.magma.Value;
import org.obiba.magma.ValueSet;
import org.obiba.magma.ValueType;
import org.obiba.magma.Variable;
import org.obiba.magma.VariableEntity;
import org.obiba.magma.VariableValueSource;
import org.obiba.magma.VectorSource;
import org.obiba.magma.datasource.spss.support.SpssVariableValueFactory;
import org.opendatafoundation.data.spss.SPSSVariable;

public class SpssVariableValueSource extends AbstractVariableValueSource implements VariableValueSource, VectorSource {

  private final Variable variable;

  private final SPSSVariable spssVariable;

  private final Map<String, Integer> identifierToVariableIndex;

  public SpssVariableValueSource(Variable variable, SPSSVariable spssVariable,
      Map<String, Integer> map) {
    this.variable = variable;
    this.spssVariable = spssVariable;
    identifierToVariableIndex = map;
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
    SpssValueSet spssValueSet = (SpssValueSet) valueSet;
    return spssValueSet.getValue(variable);
  }

  @Override
  public boolean supportVectorSource() {
    return true;
  }

  @NotNull
  @Override
  public VectorSource asVectorSource() {
    return this;
  }

  @Override
  public Iterable<Value> getValues(final SortedSet<VariableEntity> entities) {
    return new Iterable<Value>() {
      @Override
      public Iterator<Value> iterator() {
        return new ValuesIterator(entities);
      }
    };
  }

  //
  // Inner classes
  //

  private class ValuesIterator implements Iterator<Value> {

    private final Iterator<VariableEntity> entitiesIterator;

    private ValuesIterator(Collection<VariableEntity> entities) {
      entitiesIterator = entities.iterator();
    }

    @Override
    public boolean hasNext() {
      return entitiesIterator.hasNext();
    }

    @Override
    public Value next() {
      if(!hasNext()) {
        throw new NoSuchElementException();
      }

      VariableEntity variableEntity = entitiesIterator.next();
      int variableIndex = identifierToVariableIndex.get(variableEntity.getIdentifier());
      return new SpssVariableValueFactory(variableIndex, spssVariable, variable.getValueType())
          .create();
    }

    @Override
    public void remove() {
      throw new UnsupportedOperationException();
    }
  }

}
