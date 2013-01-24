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

  private final Datasource destination;

  public IncrementalDatasource(Datasource source, Datasource destination) {
    super(source);
    this.destination = destination;
  }

  @Override
  public ValueTable getValueTable(String name) throws NoSuchValueTableException {
    ValueTable destinationTable = null;
    try {
      destinationTable = destination.getValueTable(name);
    } catch(NoSuchValueTableException ignored) {
    }
    return IncrementalView.Factory.create(super.getValueTable(name), destinationTable);
  }

  @Override
  public Set<ValueTable> getValueTables() {
    Set<ValueTable> views = new HashSet<ValueTable>();
    for(ValueTable sourceTable : super.getValueTables()) {
      ValueTable destinationTable = null;
      try {
        destinationTable = destination.getValueTable(sourceTable.getName());
      } catch(NoSuchValueTableException ignored) {
      }
      views.add(IncrementalView.Factory.create(sourceTable, destinationTable));
    }
    return views;
  }

}
