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

import java.util.HashSet;
import java.util.Set;

import org.obiba.magma.Datasource;
import org.obiba.magma.NoSuchValueTableException;
import org.obiba.magma.ValueTable;

/**
 *
 */
public class IncrementalDatasource extends AbstractDatasourceWrapper {

  private final ValueTable destinationTable;

  public IncrementalDatasource(Datasource wrapped, ValueTable destinationTable) {
    super(wrapped);
    this.destinationTable = destinationTable;
  }

  @Override
  public ValueTable getValueTable(String name) throws NoSuchValueTableException {
    return createIncrementalView(super.getValueTable(name));
  }

  @Override
  public Set<ValueTable> getValueTables() {
    Set<ValueTable> views = new HashSet<ValueTable>();
    for(ValueTable valueTable : super.getValueTables()) {
      views.add(createIncrementalView(valueTable));
    }
    return views;
  }

  private ValueTable createIncrementalView(ValueTable sourceTable) {
    return new IncrementalView(sourceTable, destinationTable);
  }
}
