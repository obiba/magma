/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.magma.datasource.mongodb.converter;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.DBObject;
import org.bson.Document;
import org.obiba.magma.Attribute;
import org.obiba.magma.Category;
import org.obiba.magma.ValueType;
import org.obiba.magma.Variable;
import org.obiba.magma.datasource.mongodb.MongoDBVariable;

import javax.annotation.Nullable;
import java.util.Collection;

public class VariableConverter {

  private VariableConverter() {}

  public static MongoDBVariable unmarshall(Document object) {
    ValueType valueType = ValueType.Factory.forName(getFieldAsString(object, "valueType"));
    Variable.Builder builder = Variable.Builder.newVariable(getFieldAsString(object, "name"), valueType,
        getFieldAsString(object, "entityType")) //
        .repeatable(getFieldAsBoolean(object, "repeatable")) //
        .mimeType(getFieldAsString(object, "mimeType")) //
        .referencedEntityType(getFieldAsString(object, "referencedEntityType")) //
        .occurrenceGroup(getFieldAsString(object, "occurrenceGroup")) //
        .unit(getFieldAsString(object, "unit")).index(getFieldAsInteger(object, "index"));

    if(object.containsKey("categories")) {
      builder.addCategories(unmarshallCategories((Iterable<?>) object.get("categories")));
    }

    if(object.containsKey("attributes")) {
      builder.addAttributes(unmarshallAttributes((Iterable<?>) object.get("attributes")));
    }

    return new MongoDBVariable(builder.build(), object.get("_id").toString());
  }

  private static Iterable<Category> unmarshallCategories(Iterable<?> cats) {
    ImmutableList.Builder<Category> list = ImmutableList.builder();
    for(Object o : cats) {
      Document cat = (Document) o;
      Category.Builder catBuilder = Category.Builder.newCategory(cat.get("name").toString())
          .missing(Boolean.parseBoolean(cat.get("missing").toString()));
      if(cat.containsKey("attributes")) {
        catBuilder.addAttributes(unmarshallAttributes((Iterable<?>) cat.get("attributes")));
      }
      list.add(catBuilder.build());
    }
    return list.build();
  }

  private static Iterable<Attribute> unmarshallAttributes(Iterable<?> attributes) {
    ImmutableList.Builder<Attribute> list = ImmutableList.builder();
    for(Object o : attributes) {
      Document attr = (Document) o;
      String value = getFieldAsString(attr, "value");
      if(!Strings.isNullOrEmpty(value)) {
        Attribute.Builder attrBuilder = Attribute.Builder.newAttribute(attr.get("name").toString()) //
            .withNamespace(getFieldAsString(attr, "namespace")).withValue(value);

        String locale = getFieldAsString(attr, "locale");
        if(!Strings.isNullOrEmpty(locale)) attrBuilder.withLocale(locale);

        list.add(attrBuilder.build());
      }
    }
    return list.build();
  }

  @Nullable
  private static String getFieldAsString(Document object, String key) {
    if(!object.containsKey(key)) return null;
    Object value = object.get(key);
    return value == null ? null : value.toString();
  }

  private static boolean getFieldAsBoolean(Document object, String key) {
    if(!object.containsKey(key)) return false;
    Object value = object.get(key);
    return value == null ? false : Boolean.valueOf(value.toString());
  }

  private static Integer getFieldAsInteger(Document object, String key) {
    if(!object.containsKey(key)) return null;
    Object value = object.get(key);
    try {
      return value == null ? null : Integer.valueOf(value.toString());
    } catch(NumberFormatException e) {
      return null;
    }
  }

  public static Document marshall(Variable variable) {
    Document builder = new Document()
        .append("name", variable.getName()) //
        .append("valueType", variable.getValueType().getName()) //
        .append("entityType", variable.getEntityType()) //
        .append("mimeType", variable.getMimeType()) //
        .append("repeatable", variable.isRepeatable()) //
        .append("occurrenceGroup", variable.getOccurrenceGroup()) //
        .append("referencedEntityType", variable.getReferencedEntityType()) //
        .append("unit", variable.getUnit())
        .append("index", variable.getIndex());

    if(variable.hasCategories()) {
      Collection<Object> list = new BasicDBList();
      for(Category category : variable.getCategories()) {
        list.add(marshall(category));
      }
      builder.append("categories", list);
    }

    if(variable.hasAttributes()) {
      Collection<Object> list = new BasicDBList();
      for(Attribute attribute : variable.getAttributes()) {
        list.add(marshall(attribute));
      }
      builder.append("attributes", list);
    }

    return builder;
  }

  private static DBObject marshall(Category category) {
    BasicDBObjectBuilder builder = BasicDBObjectBuilder.start() //
        .add("name", category.getName()).add("missing", category.isMissing());
    if(category.hasAttributes()) {
      Collection<Object> list = new BasicDBList();
      for(Attribute attribute : category.getAttributes()) {
        list.add(marshall(attribute));
      }
      builder.add("attributes", list);
    }
    return builder.get();
  }

  private static DBObject marshall(Attribute attribute) {
    return BasicDBObjectBuilder.start() //
        .add("namespace", attribute.hasNamespace() ? attribute.getNamespace() : null) //
        .add("name", attribute.getName()) //
        .add("locale", attribute.isLocalised() ? attribute.getLocale().toString() : null) //
        .add("value", attribute.getValue().toString()).get();
  }

}
