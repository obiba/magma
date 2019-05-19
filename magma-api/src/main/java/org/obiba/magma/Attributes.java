/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.magma;

import java.util.List;
import java.util.Locale;

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;

public final class Attributes {

  private Attributes() {}

  /**
   * Makes a defensive copy of an attribute to make sure the result is immutable.
   *
   * @param attribute the attribute to copy
   * @return a new immutable attribute, or the same instance if it is determined to be immutable
   */
  public static Attribute copyOf(Attribute attribute) {
    if(attribute instanceof AttributeBean) {
      // AttributeBean is immutable.
      return attribute;
    }
    Attribute.Builder builder = Attribute.Builder.newAttribute(attribute.getName()).withValue(attribute.getValue());
    if(attribute.hasNamespace()) builder.withNamespace(attribute.getNamespace());
    if(attribute.isLocalised()) builder.withLocale(attribute.getLocale());
    return builder.build();
  }

  /**
   * Encodes the attribute namespace, name and locale for use in a tabular file header.
   *
   * @param attribute
   * @return
   */
  public static String encodeForHeader(Attribute attribute) {
    StringBuilder builder = new StringBuilder();
    if(attribute.hasNamespace()) {
      builder.append(attribute.getNamespace()).append("::");
    }
    builder.append(attribute.getName());
    if(attribute.isLocalised()) {
      builder.append(":").append(attribute.getLocale());
    }
    return builder.toString();
  }

  final static Splitter namespaceSplitter = Splitter.on("::").trimResults().omitEmptyStrings().limit(2);

  final static Splitter localeSplitter = Splitter.on(":").trimResults().omitEmptyStrings().limit(2);

  public static Attribute.Builder decodeFromHeader(String header) {
    Attribute.Builder builder = Attribute.Builder.newAttribute();
    List<String> parts = ImmutableList.copyOf(namespaceSplitter.split(header));
    String remaining = header;
    if(parts.size() > 1) {
      builder.withNamespace(parts.get(0));
      remaining = parts.get(1);
    }
    parts = ImmutableList.copyOf(localeSplitter.split(remaining));
    if(parts.size() > 1) {
      builder.withName(parts.get(0));
      builder.withLocale(new Locale(parts.get(1)));
    } else {
      builder.withName(parts.get(0));
    }
    return builder;
  }
}
