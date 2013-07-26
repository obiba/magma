/*
 * Copyright (c) 2013 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.magma.datasource.mongodb;

import java.util.Set;

import org.obiba.magma.VariableEntity;
import org.obiba.magma.support.VariableEntityBean;
import org.obiba.magma.support.VariableEntityProvider;

import com.google.common.collect.ImmutableSet;
import com.mongodb.BasicDBObject;
import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.DBCursor;

class MongoDBVariableEntityProvider implements VariableEntityProvider {

  private String entityType;

  private final MongoDBValueTable table;

  MongoDBVariableEntityProvider(MongoDBValueTable table, String entityType) {
    this.table = table;
    this.entityType = entityType;
  }

  @Override
  public String getEntityType() {
    if (entityType == null) {
      entityType = (String) table.asDBObject().get("entityType");
    }
    return entityType;
  }

  @Override
  public boolean isForEntityType(String entityType) {
    return this.entityType.equals(entityType);
  }

  @Override
  public Set<VariableEntity> getVariableEntities() {
    ImmutableSet.Builder<VariableEntity> builder = ImmutableSet.builder();

    DBCursor cursor = table.getValueSetCollection().find(new BasicDBObject(), BasicDBObjectBuilder.start("_id",1).get());
    try {
      while(cursor.hasNext()) {
        builder.add(new VariableEntityBean(table.getEntityType(), cursor.next().get("_id").toString()));
      }
    } finally {
      cursor.close();
    }

    return builder.build();
  }
}
