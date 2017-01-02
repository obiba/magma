/*
 * Copyright (c) 2017 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.magma.support;

import org.obiba.magma.Datasource;
import org.obiba.magma.NoSuchValueTableException;
import org.obiba.magma.ValueTable;

/**
 *
 */
public class IncrementalDatasource extends AbstractDatasourceWrapperWithCachedTables {

  private final Datasource destination;

  public IncrementalDatasource(Datasource source, Datasource destination) {
    super(source);
    this.destination = destination;
  }

  @Override
  protected ValueTable createValueTable(ValueTable table) {
    ValueTable destinationTable = null;
    try {
      destinationTable = destination.getValueTable(table.getName());
    } catch(NoSuchValueTableException ignored) {
    }
    return IncrementalValueTable.Factory.create(table, destinationTable);
  }

}
