package org.obiba.magma.xstream.converter;

import java.util.ArrayList;
import java.util.List;

import org.obiba.magma.ValueTable;
import org.obiba.magma.support.ValueTableReference;
import org.obiba.magma.views.JoinTable;

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

public class JoinTableConverter implements Converter {

  public static final JoinTableConverter INSTANCE = new JoinTableConverter();

  @Override
  public boolean canConvert(@SuppressWarnings("rawtypes") Class type) {
    return JoinTable.class.isAssignableFrom(type);
  }

  @Override
  public void marshal(Object source, HierarchicalStreamWriter writer, MarshallingContext context) {
    JoinTable joinTable = (JoinTable) source;

    writer.startNode("list");
    for(ValueTable vt : joinTable.getTables()) {
      if(vt instanceof ValueTableReference) {
        writer.startNode("table");
        writer.addAttribute("class", ValueTableReference.class.getName());
        context.convertAnother(vt);
        writer.endNode();
      } else {
        throw new RuntimeException("Unexpected table type in JoinTable tables list: " + vt.getClass().getSimpleName());
      }
    }
    writer.endNode();
  }

  @Override
  public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
    reader.moveDown();
    List<ValueTable> tables = new ArrayList<ValueTable>();
    while(reader.hasMoreChildren()) {
      reader.moveDown();
      tables.add((ValueTable) context.convertAnother(context.currentObject(), ValueTableReference.class));
      reader.moveUp();
    }
    reader.moveUp();
    return new JoinTable(tables, false);
  }
}