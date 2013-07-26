/*
 * Copyright (c) 2013 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.magma.datasource.mongodb.converter;

import org.obiba.magma.Attribute;
import org.obiba.magma.AttributeAware;
import org.obiba.magma.Category;
import org.obiba.magma.ValueType;
import org.obiba.magma.Variable;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.DBObject;

public class VariableConverter {

  public static String normalizeFieldName(String name) {
    return name.replaceAll("[.$]","_");
  }

  public static Variable unmarshall(DBObject object) {
    ValueType valueType = ValueType.Factory.forName(object.get("valueType").toString());
    String entityType = object.get("entityType").toString();
    return Variable.Builder.newVariable(object.get("name").toString(), valueType, entityType).build();
  }

  public static DBObject marshall(Variable variable) {
    BasicDBObjectBuilder builder = BasicDBObjectBuilder.start("_id", variable.getName()) //
        .add("name", variable.getName()) //
        .add("valueType", variable.getValueType().getName()) //
        .add("entityType", variable.getEntityType()) //
        .add("mimeType", variable.getMimeType()) //
        .add("repeatable", variable.isRepeatable()) //
        .add("occurrenceGroup", variable.getOccurrenceGroup()) //
        .add("referencedEntityType", variable.getReferencedEntityType()) //
        .add("unit", variable.getUnit());

    if(variable.hasCategories()) {
      BasicDBList list = new BasicDBList();
      for(Category category : variable.getCategories()) {
        list.add(marshall(category));
      }
      builder.add("categories", list);
    }

    if(variable.hasAttributes()) {
      BasicDBList list = new BasicDBList();
      for(Attribute attribute : variable.getAttributes()) {
        list.add(marshall(attribute));
      }
      builder.add("attributes", list);
    }

    return builder.get();
  }

  private static DBObject marshall(Category category) {
    BasicDBObjectBuilder builder = BasicDBObjectBuilder.start() //
        .add("name", category.getName()).add("missing", category.isMissing());
    if(category.hasAttributes()) {
      BasicDBList list = new BasicDBList();
      for(Attribute attribute : category.getAttributes()) {
        list.add(marshall(attribute));
      }
      builder.add("attributes", list);
    }
    return builder.get();
  }

  private static DBObject marshall(Attribute attribute) {
    return BasicDBObjectBuilder.start()//
        .add("namespace", attribute.getNamespace()) //
        .add("name", attribute.getName())//
        .add("locale", attribute.getLocale() == null ? null : attribute.getLocale().toString()) //
        .add("value", attribute.getValue().toString()).get();
  }

}
