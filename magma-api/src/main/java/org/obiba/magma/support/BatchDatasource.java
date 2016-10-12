/*
 * Copyright (c) 2016 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.magma.support;

import org.obiba.magma.Datasource;
import org.obiba.magma.ValueTable;

/**
 *
 */
public class BatchDatasource extends AbstractDatasourceWrapperWithCachedTables {

  private final int limit;

  public BatchDatasource(Datasource wrapped, int limit) {
    super(wrapped);
    this.limit = limit;
  }

  @Override
  protected ValueTable createValueTable(ValueTable table) {
    return new BatchValueTable(table, limit);
  }

}
