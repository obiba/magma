/*
 * Copyright (c) 2013 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.magma.datasource.neo4j.repository;

import org.obiba.magma.datasource.neo4j.domain.DatasourceNode;
import org.obiba.magma.datasource.neo4j.domain.ValueTableNode;
import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.GraphRepository;
import org.springframework.data.neo4j.repository.RelationshipOperationsRepository;
import org.springframework.data.repository.query.Param;

public interface ValueTableRepository
    extends GraphRepository<ValueTableNode>, RelationshipOperationsRepository<ValueTableNode> {

  @Query("start datasource=node({datasource}) " + //
      "match (datasource)-[:HAS_TABLES]->(table) " + //
      "where table.name={tableName} " + //
      "return table")
  ValueTableNode findByDatasourceAndName(@Param("datasource") DatasourceNode datasource,
      @Param("tableName") String name);

  @Query("start datasource=node:datasource(name={datasourceName}) " + //
      "match (datasource)-[:HAS_TABLES]->(table) " + //
      "where table.name={tableName} " + //
      "return table")
  ValueTableNode findByDatasourceAndName(@Param("datasourceName") String datasourceName,
      @Param("tableName") String name);

}
