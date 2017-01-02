/*
 * Copyright (c) 2017 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.magma.xstream.converter;

import org.obiba.magma.Value;
import org.obiba.magma.ValueType;

import com.google.common.base.Strings;
import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

public class ValueConverter implements Converter {

  public ValueConverter() {
  }

  @Override
  @SuppressWarnings("rawtypes")
  public boolean canConvert(Class type) {
    return Value.class.equals(type);
  }

  @Override
  public void marshal(Object source, HierarchicalStreamWriter writer, MarshallingContext context) {
    Value value = (Value) source;
    writer.addAttribute("valueType", value.getValueType().getName());
    if(ValueSequenceConverter.ContextHelper.isSequence(context)) {
      int order = ValueSequenceConverter.ContextHelper.getCurrentOrder(context);
      writer.addAttribute("order", Integer.toString(order));
    }
    if(!value.isNull()) {
      writer.setValue(value.toString());
    }
  }

  @Override
  public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
    String valueType = reader.getAttribute("valueType");

    if(ValueSequenceConverter.ContextHelper.isSequence(context)) {
      String order = reader.getAttribute("order");
      ValueSequenceConverter.ContextHelper.setCurrentOrder(context, Integer.valueOf(order));
    }

    String value = reader.getValue();
    return ValueType.Factory.forName(valueType).valueOf(Strings.emptyToNull(value));
  }

}
