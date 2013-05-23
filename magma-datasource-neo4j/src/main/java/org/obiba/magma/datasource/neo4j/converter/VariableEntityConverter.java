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

import javax.annotation.Nonnull;

import org.obiba.magma.VariableEntity;
import org.obiba.magma.datasource.neo4j.domain.VariableEntityNode;

public class VariableEntityConverter implements Neo4jConverter<VariableEntityNode, VariableEntity> {

  public static VariableEntityConverter getInstance() {
    return new VariableEntityConverter();
  }

  private VariableEntityConverter() {
  }

  @Override
  public VariableEntityNode marshal(@Nonnull VariableEntity entity, @Nonnull Neo4jMarshallingContext context) {
    VariableEntityNode entityNode = context.getVariableEntityRepository()
        .findByIdentifierAndType(entity.getIdentifier(), entity.getType());
    if(entityNode == null) {
      entityNode = context.getNeo4jTemplate().save(new VariableEntityNode(entity.getIdentifier(), entity.getType()));
    }
    context.getNeo4jTemplate().fetch(entityNode);
    return entityNode;
  }

  @Override
  public VariableEntity unmarshal(@Nonnull VariableEntityNode entityNode, @Nonnull Neo4jMarshallingContext context) {
    return null;
  }
}
