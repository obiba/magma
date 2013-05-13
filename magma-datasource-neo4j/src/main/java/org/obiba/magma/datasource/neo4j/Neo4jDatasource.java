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

import java.util.Set;

import javax.annotation.Nonnull;

import org.obiba.magma.ValueTable;
import org.obiba.magma.support.AbstractDatasource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Neo4jDatasource extends AbstractDatasource {

  private static final Logger log = LoggerFactory.getLogger(Neo4jDatasource.class);

  public static final String TYPE = "neo4j";

  public Neo4jDatasource(@Nonnull String name) {
    super(name, TYPE);
  }

  @Override
  protected Set<String> getValueTableNames() {
    return null;  //To change body of implemented methods use File | Settings | File Templates.
  }

  @Override
  protected ValueTable initialiseValueTable(String tableName) {
    return null;  //To change body of implemented methods use File | Settings | File Templates.
  }
}