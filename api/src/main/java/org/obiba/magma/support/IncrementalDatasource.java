/*
 * Copyright (c) 2013 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.magma.support;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.NoSuchElementException;
import java.util.Set;

import org.obiba.magma.Datasource;
import org.obiba.magma.NoSuchValueTableException;
import org.obiba.magma.ValueTable;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

/**
 *
 */
@SuppressWarnings("UnusedDeclaration")
public class IncrementalDatasource extends AbstractDatasourceWrapper {

  private final Datasource destination;

  private final Set<ValueTable> valueTablesCache = new LinkedHashSet<ValueTable>(100);

  public IncrementalDatasource(Datasource source, Datasource destination) {
    super(source);
    this.destination = destination;
  }

  @Override
  public ValueTable getValueTable(String name) throws NoSuchValueTableException {
    ValueTable valueTable = null;
    try {
      valueTable = getCachedValueTable(name);
    } catch(NoSuchValueTableException e) {
      ValueTable destinationTable = null;
      try {
        destinationTable = destination.getValueTable(name);
      } catch(NoSuchValueTableException ignored) {
      }
      valueTable = IncrementalView.Factory.create(super.getValueTable(name), destinationTable);
      valueTablesCache.add(valueTable);
    }
    return valueTable;
  }

  @Override
  public Set<ValueTable> getValueTables() {
    for(ValueTable sourceTable : super.getValueTables()) {
      try {
        getCachedValueTable(sourceTable.getName());
      } catch(NoSuchValueTableException e) {
        ValueTable destinationTable = null;
        try {
          destinationTable = destination.getValueTable(sourceTable.getName());
        } catch(NoSuchValueTableException ignored) {
        }
        valueTablesCache.add(IncrementalView.Factory.create(sourceTable, destinationTable));
      }
    }
    return Collections.unmodifiableSet(valueTablesCache);
  }

  private ValueTable getCachedValueTable(final String tableName) throws NoSuchValueTableException {
    try {
      return Iterables.find(valueTablesCache, new Predicate<ValueTable>() {
        @Override
        public boolean apply(ValueTable input) {
          return tableName.equals(input.getName());
        }
      });
    } catch(NoSuchElementException e) {
      throw new NoSuchValueTableException(getName(), tableName);
    }
  }

}
