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
import org.obiba.magma.views.support.AllClause;
import org.obiba.magma.views.support.NoneClause;

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
    String name = null;
    ValueTable from = null;
    SelectClause select = new AllClause();
    WhereClause where = new AllClause();
    ListClause variables = new NoneClause();

    while(reader.hasMoreChildren()) {
      reader.moveDown();

      String nodeName = reader.getNodeName();
      if(nodeName.equals("name")) {
        name = reader.getValue();
      } else if(nodeName.equals("select")) {
        select = (SelectClause) context.convertAnother(context.currentObject(), getClass(reader.getAttribute("class")));
      } else if(nodeName.equals("where")) {
        where = (WhereClause) context.convertAnother(context.currentObject(), getClass(reader.getAttribute("class")));
      } else if(nodeName.equals("variables")) {
        variables = (ListClause) context.convertAnother(context.currentObject(), getClass(reader.getAttribute("class")));
      } else if(nodeName.equals("from")) {
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
      } else {
        throw new RuntimeException("Unexpected view child node: " + nodeName);
      }

      reader.moveUp();
    }

    View.Builder viewBuilder = new View.Builder(name, from);
    viewBuilder.select(select);
    viewBuilder.where(where);
    viewBuilder.list(variables);

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
    return new ValueTableReference(reader.getValue());
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
