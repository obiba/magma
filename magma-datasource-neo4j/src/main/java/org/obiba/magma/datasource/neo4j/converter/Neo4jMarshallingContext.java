/*
 * Copyright (c) 2013 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.magma.datasource.neo4j.converter;

import org.obiba.magma.datasource.neo4j.domain.DatasourceNode;
import org.obiba.magma.datasource.neo4j.domain.ValueSetNode;
import org.obiba.magma.datasource.neo4j.domain.ValueTableNode;
import org.obiba.magma.datasource.neo4j.domain.VariableNode;
import org.springframework.data.neo4j.support.Neo4jTemplate;

public class Neo4jMarshallingContext {

  private Neo4jTemplate neo4jTemplate;

  private DatasourceNode datasource;

  private ValueTableNode valueTable;

  private ValueSetNode valueSet;

  private VariableNode variable;

  public static Neo4jMarshallingContext create(Neo4jTemplate neo4jTemplate, DatasourceNode datasource) {
    return create(neo4jTemplate, datasource, null);
  }

  public static Neo4jMarshallingContext create(Neo4jTemplate neo4jTemplate, DatasourceNode datasource,
      ValueTableNode valueTable) {
    Neo4jMarshallingContext context = new Neo4jMarshallingContext();
    context.neo4jTemplate = neo4jTemplate;
    context.datasource = datasource;
    context.valueTable = valueTable;

    neo4jTemplate.fetch(context.datasource);
    if(context.valueTable != null) {
      neo4jTemplate.fetch(context.valueTable);
    }
    return context;
  }

  public Neo4jTemplate getNeo4jTemplate() {
    return neo4jTemplate;
  }

  public DatasourceNode getDatasource() {
    return datasource;
  }

  public ValueTableNode getValueTable() {
    return valueTable;
  }

  public ValueSetNode getValueSet() {
    return valueSet;
  }

  public VariableNode getVariable() {
    return variable;
  }
}
