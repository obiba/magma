package org.obiba.magma.xstream.converter;

import java.util.ArrayList;
import java.util.List;

import org.obiba.magma.ValueTable;
import org.obiba.magma.support.ValueTableReference;
import org.obiba.magma.views.JoinTable;
import org.obiba.magma.views.ListClause;
import org.obiba.magma.views.SelectClause;
import org.obiba.magma.views.View;
import org.obiba.magma.views.WhereClause;

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

public class ViewConverter implements Converter {
  //
  // Converter Methods
  //

  @SuppressWarnings("unchecked")
  public boolean canConvert(Class type) {
    return View.class.isAssignableFrom(type);
  }

  public void marshal(Object source, HierarchicalStreamWriter writer, MarshallingContext context) {
    View view = (View) source;

    writer.startNode("name");
    writer.setValue(view.getName());
    writer.endNode();

    writer.startNode("from");
    writer.addAttribute("class", view.getWrappedValueTable().getClass().getName());
    marshallFromTable(view.getWrappedValueTable(), writer);
    writer.endNode();

    writer.startNode("select");
    writer.addAttribute("class", view.getSelectClause().getClass().getName());
    context.convertAnother(view.getSelectClause());
    writer.endNode();

    writer.startNode("where");
    writer.addAttribute("class", view.getWhereClause().getClass().getName());
    context.convertAnother(view.getWhereClause());
    writer.endNode();

    writer.startNode("variables");
    writer.addAttribute("class", view.getListClause().getClass().getName());
    context.convertAnother(view.getListClause());
    writer.endNode();
  }

  public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
    reader.moveDown();
    String name = reader.getValue();
    reader.moveUp();

    ValueTable from = null;
    reader.moveDown();
    String fromClass = reader.getAttribute("class");
    if(fromClass.equals(JoinTable.class.getName())) {
      reader.moveDown();
      List<ValueTable> tables = new ArrayList<ValueTable>();
      while(reader.hasMoreChildren()) {
        reader.moveDown();
        tables.add(unmarshallValueTableReference(reader));
        reader.moveUp();
      }
      reader.moveUp();
      from = new JoinTable(tables, false);
    } else {
      reader.moveDown();
      from = unmarshallValueTableReference(reader);
      reader.moveUp();
    }
    reader.moveUp();

    View.Builder viewBuilder = new View.Builder(name, from);

    reader.moveDown();
    viewBuilder.select((SelectClause) context.convertAnother(context.currentObject(), getClass(reader.getAttribute("class"))));
    reader.moveUp();

    reader.moveDown();
    viewBuilder.where((WhereClause) context.convertAnother(context.currentObject(), getClass(reader.getAttribute("class"))));
    reader.moveUp();

    reader.moveDown();
    viewBuilder.list((ListClause) context.convertAnother(context.currentObject(), getClass(reader.getAttribute("class"))));
    reader.moveUp();

    return viewBuilder.build();
  }

  //
  // Methods
  //

  private void marshallFromTable(ValueTable fromTable, HierarchicalStreamWriter writer) {
    if(fromTable instanceof JoinTable) {
      JoinTable joinTable = (JoinTable) fromTable;

      writer.startNode("list");
      for(ValueTable vt : joinTable.getTables()) {
        marshallValueTableReference(writer, vt);
      }
      writer.endNode();
    } else {
      marshallValueTableReference(writer, fromTable);
    }
  }

  private void marshallValueTableReference(HierarchicalStreamWriter writer, ValueTable vt) {
    writer.startNode("reference");
    writer.setValue(vt.getDatasource().getName() + "." + vt.getName());
    writer.endNode();
  }

  private ValueTableReference unmarshallValueTableReference(HierarchicalStreamReader reader) {
    ValueTableReference valueTableReference = new ValueTableReference();
    valueTableReference.setReference(reader.getValue());
    return valueTableReference;
  }

  private Class<?> getClass(String className) throws RuntimeException {
    Class<?> theClass = null;

    try {
      theClass = Class.forName(className);
    } catch(ClassNotFoundException ex) {
      throw new RuntimeException(ex);
    }

    return theClass;
  }
}
