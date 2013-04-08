package org.obiba.magma.xstream.converter;

import java.util.Arrays;

import org.obiba.magma.Value;
import org.obiba.magma.ValueSequence;
import org.obiba.magma.ValueType;

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.DataHolder;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

public class ValueSequenceConverter implements Converter {

  public ValueSequenceConverter() {
  }

  @Override
  public boolean canConvert(Class type) {
    return ValueSequence.class.equals(type);
  }

  @Override
  public void marshal(Object source, HierarchicalStreamWriter writer, MarshallingContext context) {
    ValueSequence sequence = (ValueSequence) source;

    ContextHelper.startSequence(context);

    try {
      writer.addAttribute("valueType", sequence.getValueType().getName());
      if(!sequence.isNull()) {
        writer.addAttribute("size", Integer.toString(sequence.getSize()));
        int order = 0;
        for(Value value : sequence.getValue()) {
          ContextHelper.setCurrentOrder(context, order++);
          writer.startNode("value");
          context.convertAnother(value);
          writer.endNode();
        }
      }
    } finally {
      ContextHelper.endSequence(context);
    }
  }

  @Override
  public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
    String valueType = reader.getAttribute("valueType");
    String size = reader.getAttribute("size");
    if(!reader.hasMoreChildren()) {
      return ValueType.Factory.forName(valueType).nullSequence();
    }

    ContextHelper.startSequence(context);
    Value[] values = new Value[Integer.valueOf(size)];
    while(reader.hasMoreChildren()) {
      reader.moveDown();
      Value value = (Value) context.convertAnother(context.currentObject(), Value.class);
      int order = ContextHelper.getCurrentOrder(context);
      values[order] = value;
      reader.moveUp();
    }
    ContextHelper.endSequence(context);
    return ValueType.Factory.forName(valueType).sequenceOf(Arrays.asList(values));
  }

  static class ContextHelper {

    private ContextHelper() {}

    static public void startSequence(DataHolder holder) {
      holder.put(ValueSequence.class, new Object());
    }

    static public void setCurrentOrder(DataHolder holder, int order) {
      Object key = holder.get(ValueSequence.class);
      holder.put(key, order);
    }

    static public int getCurrentOrder(DataHolder holder) {
      Object key = holder.get(ValueSequence.class);
      return (Integer) holder.get(key);
    }

    static public void endSequence(DataHolder holder) {
      holder.put(ValueSequence.class, null);
    }

    public static boolean isSequence(DataHolder holder) {
      return holder.get(ValueSequence.class) != null;
    }
  }
}
