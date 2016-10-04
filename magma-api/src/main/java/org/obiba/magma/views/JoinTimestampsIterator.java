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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import org.obiba.magma.Timestamps;
import org.obiba.magma.VariableEntity;
import org.obiba.magma.support.NullTimestamps;
import org.obiba.magma.support.UnionTimestamps;

import javax.validation.constraints.NotNull;
import java.util.Iterator;
import java.util.List;
import java.util.SortedSet;
import java.util.stream.Collectors;

class JoinTimestampsIterator implements Iterator<Timestamps> {

  private final JoinTable joinTable;

  @NotNull
  private final SortedSet<VariableEntity> entities;

  private final Iterator<VariableEntity> entitiesIterator;

  private List<Iterator<Timestamps>> timestampsIterators = Lists.newArrayList();

  JoinTimestampsIterator(JoinTable joinTable, SortedSet<VariableEntity> entities) {
    this.joinTable = joinTable;
    this.entities = entities;
    entitiesIterator = entities.iterator();
  }

  @Override
  public boolean hasNext() {
    return entitiesIterator.hasNext();
  }

  @Override
  public Timestamps next() {
    // get the value iterator for each table
    if (timestampsIterators.isEmpty()) {
      timestampsIterators.addAll(joinTable.getTables().stream().map(table -> table.getValueSetTimestamps(entities).iterator()).collect(Collectors.toList()));
    }

    // increment each timestamps iterator and make a union of them
    entitiesIterator.next();
    ImmutableList.Builder<Timestamps> timestamps = ImmutableList.builder();
    for (Iterator<Timestamps> iterator : timestampsIterators) {
      Timestamps ts = iterator.next();
      timestamps.add(ts == null ? NullTimestamps.get() : ts);
    }
    return new UnionTimestamps(timestamps.build());
  }

  @Override
  public void remove() {
    throw new UnsupportedOperationException();
  }
}
