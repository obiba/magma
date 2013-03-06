package org.obiba.magma.xstream.converter;

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

@SuppressWarnings("UnusedDeclaration")
public class ViewConverter implements Converter {

  @Override
  public boolean canConvert(@SuppressWarnings("rawtypes") Class type) {
    return View.class.isAssignableFrom(type);
  }

  @Override
  public void marshal(Object source, HierarchicalStreamWriter writer, MarshallingContext context) {
    View view = (View) source;

    writer.startNode("name");
    writer.setValue(view.getName());
    writer.endNode();

    writer.startNode("from");
    writer.addAttribute("class", getFromTableClass(view.getWrappedValueTable()));
    marshalFromTable(view.getWrappedValueTable(), context);
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

  @SuppressWarnings({ "PMD.NcssMethodCount", "OverlyLongMethod", "ConstantConditions" })
  @Override
  public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
    String name = null;
    ValueTable from = null;
    SelectClause select = new AllClause();
    WhereClause where = new AllClause();
    ListClause variables = new NoneClause();

    while(reader.hasMoreChildren()) {
      reader.moveDown();

      String nodeName = reader.getNodeName();
      //noinspection IfStatementWithTooManyBranches
      if("name".equals(nodeName)) {
        name = reader.getValue();
      } else if("select".equals(nodeName)) {
        select = (SelectClause) context.convertAnother(context.currentObject(), getClass(reader.getAttribute("class")));
      } else if("where".equals(nodeName)) {
        where = (WhereClause) context.convertAnother(context.currentObject(), getClass(reader.getAttribute("class")));
      } else if("variables".equals(nodeName)) {
        variables = (ListClause) context
            .convertAnother(context.currentObject(), getClass(reader.getAttribute("class")));
      } else if("from".equals(nodeName)) {
        from = unmarshalFromTable(reader, context);
      } else {
        throw new RuntimeException("Unexpected view child node: " + nodeName);
      }

      reader.moveUp();
    }
    return new View.Builder(name, from).select(select).where(where).list(variables).build();
  }

  @SuppressWarnings("ChainOfInstanceofChecks")
  private void marshalFromTable(ValueTable fromTable, MarshallingContext context) {
    if(fromTable instanceof JoinTable) {
      context.convertAnother(fromTable, JoinTableConverter.INSTANCE);
    } else if(fromTable instanceof ValueTableReference) {
      context.convertAnother(fromTable);
    } else {
      throw new RuntimeException("Unexpected from table type: " + fromTable.getClass().getSimpleName());
    }
  }

  private ValueTable unmarshalFromTable(HierarchicalStreamReader reader, UnmarshallingContext context) {
    String fromTableClass = reader.getAttribute("class");
    if(JoinTable.class.getName().equals(fromTableClass)) {
      return (ValueTable) context
          .convertAnother(context.currentObject(), getClass(fromTableClass), JoinTableConverter.INSTANCE);
    }
    if(ValueTableReference.class.getName().equals(fromTableClass)) {
      return (ValueTable) context.convertAnother(context.currentObject(), ValueTableReference.class);
    }
    throw new RuntimeException("Unexpected from table class: " + fromTableClass);
  }

  private String getFromTableClass(ValueTable valueTable) {
    return valueTable instanceof JoinTable ? JoinTable.class.getName() : ValueTableReference.class.getName();
  }

  private Class<?> getClass(String className) throws RuntimeException {
    try {
      return Class.forName(className);
    } catch(ClassNotFoundException ex) {
      throw new RuntimeException(ex);
    }
  }

}
