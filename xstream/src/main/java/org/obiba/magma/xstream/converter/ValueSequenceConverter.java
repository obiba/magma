package org.obiba.magma.xstream.converter;

import org.obiba.magma.Value;
import org.obiba.magma.ValueSequence;

import com.thoughtworks.xstream.converters.Converter;
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
    if(sequence.isNull()) {
      writer.setValue("null");
    } else {
      for(Value value : sequence.getValue()) {
        writer.startNode("value");
        context.convertAnother(value);
        writer.endNode();
      }
    }
  }

  @Override
  public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
    throw new UnsupportedOperationException();
  }

}
