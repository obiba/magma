/*
 * Copyright (c) 2013 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.magma.datasource.neo4j;

import org.obiba.magma.Datasource;
import org.obiba.magma.NoSuchValueSetException;
import org.obiba.magma.Timestamps;
import org.obiba.magma.ValueSet;
import org.obiba.magma.VariableEntity;
import org.obiba.magma.support.AbstractValueTable;

public class Neo4jValueTable extends AbstractValueTable {

  public Neo4jValueTable(Datasource datasource, String name) {
    super(datasource, name);
  }

  @Override
  public ValueSet getValueSet(VariableEntity entity) throws NoSuchValueSetException {
    return null;  //To change body of implemented methods use File | Settings | File Templates.
  }

  @Override
  public Timestamps getTimestamps() {
    return null;  //To change body of implemented methods use File | Settings | File Templates.
  }
}
