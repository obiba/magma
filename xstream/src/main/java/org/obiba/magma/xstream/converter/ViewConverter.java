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
  // Instance Variables
  //

  private JoinTableConverter joinTableConverter = new JoinTableConverter();

  //
  // Converter Methods
  //

  public boolean canConvert(@SuppressWarnings("rawtypes") Class type) {
    return View.class.isAssignableFrom(type);
  }

  public void marshal(Object source, HierarchicalStreamWriter writer, MarshallingContext context) {
    View view = (View) source;

    writer.startNode("name");
    writer.setValue(view.getName());
    writer.endNode();

    writer.startNode("from");
    writer.addAttribute("class", getFromTableClass(view.getWrappedValueTable()));
    marshalFromTable(view.getWrappedValueTable(), writer, context);
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
        from = unmarshalFromTable(reader, context);
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

  private void marshalFromTable(ValueTable fromTable, HierarchicalStreamWriter writer, MarshallingContext context) {
    if(fromTable instanceof JoinTable) {
      context.convertAnother(fromTable, joinTableConverter);
    } else if(fromTable instanceof ValueTableReference) {
      context.convertAnother(fromTable);
    } else {
      throw new RuntimeException("Unexpected from table type: " + fromTable.getClass().getSimpleName());
    }
  }

  private ValueTable unmarshalFromTable(HierarchicalStreamReader reader, UnmarshallingContext context) {
    String fromTableClass = reader.getAttribute("class");
    if(JoinTable.class.getName().equals(fromTableClass)) {
      return (ValueTable) context.convertAnother(context.currentObject(), getClass(fromTableClass), joinTableConverter);
    } else if(ValueTableReference.class.getName().equals(fromTableClass)) {
      return (ValueTable) context.convertAnother(context.currentObject(), ValueTableReference.class);
    } else {
      throw new RuntimeException("Unexpected from table class: " + fromTableClass);
    }
  }

  private String getFromTableClass(ValueTable valueTable) {
    return (valueTable instanceof JoinTable) ? JoinTable.class.getName() : ValueTableReference.class.getName();
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

  //
  // Inner Classes
  //

  static class JoinTableConverter implements Converter {
    //
    // Converter Methods
    //

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
        ValueTableReference tableReference = (ValueTableReference) context.convertAnother(context.currentObject(), ValueTableReference.class);
        tables.add(tableReference);
        reader.moveUp();
      }

      reader.moveUp();

      return new JoinTable(tables, false);
    }
  }
}
