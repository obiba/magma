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

import org.obiba.magma.datasource.neo4j.domain.ValueTableNode;
import org.obiba.magma.datasource.neo4j.domain.VariableNode;
import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.GraphRepository;
import org.springframework.data.neo4j.repository.RelationshipOperationsRepository;
import org.springframework.data.repository.query.Param;

public interface VariableRepository
    extends GraphRepository<ValueTableNode>, RelationshipOperationsRepository<ValueTableNode> {

  @Query("start table=node({table}) " + //
      "match (table)-[:HAS_VARIABLES]->(variable) " + //
      "where variable.name={variableName} " + //
      "return variable")
  VariableNode findByTableAndName(@Param("table") ValueTableNode table, @Param("variableName") String name);

}
