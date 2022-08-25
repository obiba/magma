/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.magma.datasource.mongodb;

import com.google.common.collect.ImmutableSet;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.Sorts;
import org.bson.Document;
import org.obiba.magma.VariableValueSource;
import org.obiba.magma.VariableValueSourceFactory;

import java.util.Set;

public class MongoDBVariableValueSourceFactory implements VariableValueSourceFactory {

  private final MongoDBValueTable table;

  public MongoDBVariableValueSourceFactory(MongoDBValueTable table) {
    this.table = table;
  }

  @Override
  public Set<VariableValueSource> createSources() {
    ImmutableSet.Builder<VariableValueSource> builder = ImmutableSet.builder();
    try(MongoCursor<Document> variableCursor = table.getVariablesCollection()
        .find()
        .sort(Sorts.ascending("_id")).cursor()) {
      while(variableCursor.hasNext()) {
        builder.add(new MongoDBVariableValueSource(table, variableCursor.next().get("name").toString()));
      }
    }
    return builder.build();
  }

}
