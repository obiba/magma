/*
 * Copyright (c) 2014 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.magma.datasource.mongodb.converter;

import javax.annotation.Nullable;

import org.bson.BSONObject;
import org.obiba.magma.Attribute;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.DBObject;

public class AttributeAwareConverter {

  protected static Iterable<Attribute> unmarshallAttributes(Iterable<?> attributes) {
    ImmutableList.Builder<Attribute> list = ImmutableList.builder();
    for(Object o : attributes) {
      BSONObject attr = (BSONObject) o;
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

  protected static DBObject marshall(Attribute attribute) {
    return BasicDBObjectBuilder.start() //
        .add("namespace", attribute.hasNamespace() ? attribute.getNamespace() : null) //
        .add("name", attribute.getName()) //
        .add("locale", attribute.isLocalised() ? attribute.getLocale().toString() : null) //
        .add("value", attribute.getValue().toString()).get();
  }


  @Nullable
  protected static String getFieldAsString(BSONObject object, String key) {
    if(!object.containsField(key)) return null;
    Object value = object.get(key);
    return value == null ? null : value.toString();
  }

  protected static boolean getFieldAsBoolean(BSONObject object, String key) {
    if(!object.containsField(key)) return false;
    Object value = object.get(key);
    return value == null ? false : Boolean.valueOf(value.toString());
  }

  protected static Integer getFieldAsInteger(BSONObject object, String key) {
    if(!object.containsField(key)) return null;
    Object value = object.get(key);
    try {
      return value == null ? null : Integer.valueOf(value.toString());
    } catch(NumberFormatException e) {
      return null;
    }
  }

}
