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

import java.util.LinkedHashSet;
import java.util.Set;

import org.obiba.magma.ValueTable;
import org.obiba.magma.datasource.neo4j.domain.DatasourceNode;
import org.obiba.magma.datasource.neo4j.domain.ValueTableNode;
import org.obiba.magma.datasource.neo4j.repository.DatasourceRepository;
import org.obiba.magma.support.AbstractDatasource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.neo4j.support.Neo4jTemplate;

public class Neo4jDatasource extends AbstractDatasource {

  private static final Logger log = LoggerFactory.getLogger(Neo4jDatasource.class);

  public static final String TYPE = "neo4j";

  @Autowired
  private DatasourceRepository datasourceRepository;

  @Autowired
  private Neo4jTemplate neo4jTemplate;

  public Neo4jDatasource(String name) {
    super(name, TYPE);
  }

  @Override
  protected Set<String> getValueTableNames() {
    Set<String> names = new LinkedHashSet<String>();
    DatasourceNode datasourceNode = datasourceRepository.findByName(getName());
    for(ValueTableNode tableNode : datasourceNode.getValueTables()) {
      names.add(tableNode.getName());
    }
    return names;
  }

  @Override
  protected ValueTable initialiseValueTable(String tableName) {
    return new Neo4jValueTable(this, tableName);
  }

  public Neo4jTemplate getNeo4jTemplate() {
    return neo4jTemplate;
  }

  public DatasourceRepository getDatasourceRepository() {
    return datasourceRepository;
  }
}