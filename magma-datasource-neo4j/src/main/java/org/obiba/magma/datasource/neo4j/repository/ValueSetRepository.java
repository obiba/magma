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

import org.obiba.magma.datasource.neo4j.domain.ValueSetNode;
import org.obiba.magma.datasource.neo4j.domain.ValueTableNode;
import org.obiba.magma.datasource.neo4j.domain.VariableEntityNode;
import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.GraphRepository;
import org.springframework.data.neo4j.repository.RelationshipOperationsRepository;
import org.springframework.data.repository.query.Param;

public interface ValueSetRepository
    extends GraphRepository<ValueSetNode>, RelationshipOperationsRepository<ValueSetNode> {

  @Query("start table=node({table}), entity=node({entity}) " + //
      "match (table)-[:HAS_VALUE_SETS]->(valueSet)<-[:ENTITIES_HAS_VALUE_SETS]-(entity)" + //
      "return valueSet")
  ValueSetNode find(@Param("table") ValueTableNode table, @Param("entity") VariableEntityNode entity);

}
