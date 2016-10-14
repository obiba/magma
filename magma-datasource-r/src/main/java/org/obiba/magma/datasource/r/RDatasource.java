/*
 * Copyright (c) 2016 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.magma.datasource.r;


import com.google.common.collect.Sets;
import org.obiba.magma.Datasource;
import org.obiba.magma.ValueTable;
import org.obiba.magma.support.AbstractDatasource;

import javax.validation.constraints.NotNull;
import java.util.Set;

/**
 * A datasource built over a R data frame.
 */
public class RDatasource extends AbstractDatasource implements Datasource {

  private static final String TYPE = "r";

  private RValueTable valueTable;

  public RDatasource(@NotNull String name) {
    super(name, TYPE);
  }

  void setValueTable(RValueTable valueTable) {
    this.valueTable = valueTable;
  }

  @Override
  protected Set<String> getValueTableNames() {
    return Sets.newHashSet(valueTable.getName());
  }

  @Override
  protected ValueTable initialiseValueTable(String tableName) {
    return valueTable;
  }
}

