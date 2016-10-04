/*
 * Copyright (c) 2016 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.magma.views;

import com.google.common.collect.Lists;
import org.obiba.magma.*;

import javax.validation.constraints.NotNull;
import java.util.Iterator;
import java.util.List;
import java.util.SortedSet;

class JoinValueIterator implements Iterator<Value> {

  @NotNull
  private final SortedSet<VariableEntity> entities;

  private final Iterator<VariableEntity> entitiesIterator;

  @NotNull
  private final List<ValueTable> owners;

  @NotNull
  private final Variable variable;

  private List<Iterator<Value>> valueIterators = Lists.newArrayList();

  JoinValueIterator(SortedSet<VariableEntity> entities, List<ValueTable> owners, Variable variable) {
    this.entities = entities;
    this.owners = owners;
    this.variable = variable;
    entitiesIterator = entities.iterator();
  }

  @Override
  public boolean hasNext() {
    return entitiesIterator.hasNext();
  }

  @Override
  public Value next() {
    // get the value iterator for each table
    if (valueIterators.isEmpty()) {
      for (ValueTable table : owners) {
        VectorSource vSource = table.getVariableValueSource(variable.getName()).asVectorSource();
        valueIterators.add(vSource.getValues(entities).iterator());
      }
    }

    // increment each value iterators and find first not null value
    entitiesIterator.next();
    Value joinedValue = null;
    for (Iterator<Value> vector : valueIterators) {
      Value value = vector.next();
      if (joinedValue == null && !value.isNull()) {
        joinedValue = value;
      }
    }
    if (joinedValue == null) {
      joinedValue = variable.isRepeatable()
          ? variable.getValueType().nullSequence()
          : variable.getValueType().nullValue();
    }

    return joinedValue;
  }

  @Override
  public void remove() {
    throw new UnsupportedOperationException();
  }
}
