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

import org.obiba.magma.VariableValueSource;
import org.obiba.magma.VariableValueSourceFactory;

import com.google.common.collect.ImmutableSet;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;

public class MongoDBVariableValueSourceFactory implements VariableValueSourceFactory {

  private final MongoDBValueTable table;

  public MongoDBVariableValueSourceFactory(MongoDBValueTable table) {
    this.table = table;
  }

  @Override
  public Set<VariableValueSource> createSources() {
    ImmutableSet.Builder<VariableValueSource> builder = ImmutableSet.builder();
    try(DBCursor variableCursor = table.getVariablesCollection().find(new BasicDBObject())) {
      while(variableCursor.hasNext()) {
        builder.add(new MongoDBVariableValueSource(table, variableCursor.next().get("name").toString()));
      }
    }
    return builder.build();
  }

}
