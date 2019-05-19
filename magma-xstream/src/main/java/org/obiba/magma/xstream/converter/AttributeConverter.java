/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.magma.xstream.converter;

import java.util.Locale;

import org.obiba.magma.Attribute;
import org.obiba.magma.ValueType;

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

/**
 * Converts an {@code Attribute} instance.
 * <p/>
 * Resulting XML:
 * <p/>
 * <pre>
 * &lt;attribute name="attributeName" valueType="integer" locale="en"&gt;12345&lt;/attribute>
 * </pre>
 */
public class AttributeConverter implements Converter {

  public AttributeConverter() {
  }

  @Override
  public boolean canConvert(@SuppressWarnings("rawtypes") Class type) {
    return Attribute.class.isAssignableFrom(type);
  }

  @Override
  public void marshal(Object source, HierarchicalStreamWriter writer, MarshallingContext context) {
    Attribute attribute = (Attribute) source;
    writer.addAttribute("name", attribute.getName());

    if(attribute.hasNamespace()) {
      writer.addAttribute("namespace", attribute.getNamespace());
    }

    writer.addAttribute("valueType", attribute.getValueType().getName());
    if(attribute.isLocalised()) {
      writer.addAttribute("locale", attribute.getLocale().getLanguage());
    }
    writer.setValue(attribute.getValue().toString());
  }

  @Override
  public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {

    String name = reader.getAttribute("name");
    String namespace = reader.getAttribute("namespace");

    ValueType valueType = ValueType.Factory.forName(reader.getAttribute("valueType"));
    String localeName = reader.getAttribute("locale");

    Locale locale = localeName == null ? null : new Locale(localeName);

    String valueString = reader.getValue();

    return Attribute.Builder.newAttribute(name).withNamespace(namespace).withValue(valueType.valueOf(valueString))
        .withLocale(locale).build();
  }
}
