package org.obiba.magma.xstream.converter;

import org.obiba.magma.Attribute;
import org.obiba.magma.Category;
import org.obiba.magma.ValueType;
import org.obiba.magma.Variable;

import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import com.thoughtworks.xstream.mapper.Mapper;

/**
 * Converts an {@code Variable} instance.
 * <p>
 * Resulting XML:
 * 
 * <pre>
 * &lt;variable collection="onyx-basline" name="HQ.SMOKER" valueType="text" entityType="Participant"&gt;
 *   &lt;attributes&gt;
 *     &lt;attribute name="label" valueType="text" locale="en"&gt;
 *       &lt;value&gt;Do you smoke?&lt;/value&gt;
 *     &lt;/attribute>
 *     &lt;attribute name="label" valueType="text" locale="fr"&gt;
 *       &lt;value&gt;Fumez-vous?&lt;/value&gt;
 *     &lt;/attribute>
 *     ...
 *   &lt;/attributes&gt;
 *   &lt;categories&gt;
 *     &lt;category name="YES" code="1"&gt;
 *       &lt;attributes&gt;
 *         &lt;attribute name="label" valueType="text" locale="en"&gt;
 *           &lt;value&gt;Yes&lt;/value&gt;
 *         &lt;/attribute>
 *         &lt;attribute name="label" valueType="text" locale="fr"&gt;
 *           &lt;value&gt;Oui&lt;/value&gt;
 *         &lt;/attribute>
 *       &lt;/attributes&gt;
 *     &lt;/category&gt;
 *     ...
 *   &lt;/categories&gt;
 * &lt;/variable>
 * </pre>
 */
public class VariableConverter extends AbstractAttributeAwareConverter {

  public VariableConverter(Mapper mapper) {
    super(mapper);
  }

  @SuppressWarnings("unchecked")
  public boolean canConvert(Class type) {
    return Variable.class.isAssignableFrom(type);
  }

  public void marshal(Object source, HierarchicalStreamWriter writer, MarshallingContext context) {
    Variable variable = (Variable) source;
    writer.addAttribute("name", variable.getName());
    writer.addAttribute("collection", variable.getCollection());
    writer.addAttribute("valueType", variable.getValueType().getName());
    writer.addAttribute("entityType", variable.getEntityType());
    if(variable.isRepeatable()) {
      writer.addAttribute("repeatable", "true");
      writer.addAttribute("occurrenceGroup", variable.getOccurrenceGroup());
    }
    marshallAttributes(variable, writer, context);
    marshallCategories(variable, writer, context);
  }

  public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
    Variable.Builder builder = Variable.Builder.newVariable(reader.getAttribute("collection"), reader.getAttribute("name"), ValueType.Factory.forName(reader.getAttribute("valueType")), reader.getAttribute("entityType"));

    while(reader.hasMoreChildren()) {
      reader.moveDown();
      if(isAttributesNode(reader.getNodeName())) {
        unmarshallAttributes(builder, reader, context);
      } else if("categories".equals(reader.getNodeName())) {
        while(reader.hasMoreChildren()) {
          Category category = readChildItem(reader, context, builder);
          builder.addCategory(category);
        }
      }
      reader.moveUp();
    }

    return builder.build();
  }

  @Override
  void addAttribute(Object current, Attribute attribute) {
    Variable.Builder builder = (Variable.Builder) current;
    builder.addAttribute(attribute);
  }

  protected void marshallCategories(Variable variable, HierarchicalStreamWriter writer, MarshallingContext context) {
    if(variable.hasCategories()) {
      writer.startNode("categories");
      for(Category c : variable.getCategories()) {
        writeItem(c, context, writer);
      }
      writer.endNode();
    }
  }

}
