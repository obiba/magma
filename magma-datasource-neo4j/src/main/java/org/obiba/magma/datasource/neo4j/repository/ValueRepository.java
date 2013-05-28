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

import java.util.Collection;

import org.obiba.magma.datasource.neo4j.domain.ValueNode;
import org.obiba.magma.datasource.neo4j.domain.ValueSetNode;
import org.obiba.magma.datasource.neo4j.domain.VariableEntityNode;
import org.obiba.magma.datasource.neo4j.domain.VariableNode;
import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.GraphRepository;
import org.springframework.data.neo4j.repository.RelationshipOperationsRepository;
import org.springframework.data.repository.query.Param;

public interface ValueRepository extends GraphRepository<ValueNode>, RelationshipOperationsRepository<ValueNode> {

  @Query("start variable=node({variable}), valueSet=node({valueSet}) " +
      "match (variable)-[:VARIABLE_HAS_VALUE_SET_VALUES]->(valueSetValue), " +
      "(valueSet)-[:VALUE_SET_HAS_VALUE_SET_VALUES]->(valueSetValue)-[:HAS_VALUE]->(value)" +
      "return value")
  ValueNode find(@Param("variable") VariableNode variable, @Param("valueSet") ValueSetNode valueSet);

  @Query("start variable=node({variable}), entity=node({entities}) " +
      "match (variable)-[:VARIABLE_HAS_VALUE_SET_VALUES]->(valueSetValue), " +
      "(entity)-[:ENTITIES_HAS_VALUE_SETS]->(valueSet)-[:VALUE_SET_HAS_VALUE_SET_VALUES]->(valueSetValue)-[:HAS_VALUE]->(value)" +
      "return value")
  Iterable<ValueNode> find(@Param("variable") VariableNode variable,
      @Param("entities") Collection<VariableEntityNode> entities);

}
