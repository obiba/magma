package org.obiba.magma.xstream.converter;

import org.obiba.magma.ValueTable;
import org.obiba.magma.support.ValueTableReference;
import org.obiba.magma.views.JoinTable;
import org.obiba.magma.views.View;

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

public class ValueTableConverter implements Converter {

  @Override
  public boolean canConvert(@SuppressWarnings("rawtypes") Class type) {
    return ValueTable.class.isAssignableFrom(type) && !type.equals(View.class) && !type.equals(JoinTable.class);
  }

  @Override
  public void marshal(Object source, HierarchicalStreamWriter writer, MarshallingContext context) {
    ValueTable valueTable = (ValueTable) source;
    writer.startNode("reference");
    writer.setValue(valueTable.getDatasource().getName() + "." + valueTable.getName());
    writer.endNode();
  }

  @Override
  public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
    reader.moveDown();
    ValueTableReference valueTableReference = new ValueTableReference(reader.getValue());
    reader.moveUp();
    return valueTableReference;
  }

}
