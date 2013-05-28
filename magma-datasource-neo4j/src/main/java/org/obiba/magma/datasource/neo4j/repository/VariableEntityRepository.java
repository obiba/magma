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

import org.obiba.magma.datasource.neo4j.domain.VariableEntityNode;
import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.GraphRepository;
import org.springframework.data.neo4j.repository.RelationshipOperationsRepository;
import org.springframework.data.repository.query.Param;

public interface VariableEntityRepository
    extends GraphRepository<VariableEntityNode>, RelationshipOperationsRepository<VariableEntityNode> {

  //TODO it would maybe be faster to do "start entity=node:variable_entity(identifier={identifier} and entity.type={type}) return entity"
  @Query("start entity=node:variable_entity(identifier={identifier}) where entity.type={type} return entity")
  VariableEntityNode findByIdentifierAndType(@Param("identifier") String identifier, @Param("type") String type);

}
