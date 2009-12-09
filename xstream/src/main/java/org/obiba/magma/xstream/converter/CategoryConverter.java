package org.obiba.magma.xstream.converter;

import org.obiba.magma.Attribute;
import org.obiba.magma.Category;

import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import com.thoughtworks.xstream.mapper.Mapper;

/**
 * Converts a {@code Category} instance.
 * <p>
 * Resulting XML:
 * 
 * <pre>
 * &lt;category name="categoryName" code="8888"&gt;
 *   &lt;attributes&gt;
 *     ...
 *   &lt;/attributes&gt;
 * &lt;/attribute>
 * </pre>
 */
public class CategoryConverter extends AbstractAttributeAwareConverter {

  public CategoryConverter(Mapper mapper) {
    super(mapper);
  }

  @SuppressWarnings("unchecked")
  public boolean canConvert(Class type) {
    return Category.class.isAssignableFrom(type);
  }

  public void marshal(Object source, HierarchicalStreamWriter writer, MarshallingContext context) {
    Category category = (Category) source;
    writer.addAttribute("name", category.getName());
    if(category.isMissing()) {
      writer.addAttribute("missing", Boolean.toString(true));
    }
    if(category.getCode() != null) {
      writer.addAttribute("code", category.getCode());
    }
    marshallAttributes(category, writer, context);
  }

  public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
    String name = reader.getAttribute("name");
    String missing = reader.getAttribute("missing");
    String code = reader.getAttribute("code");

    Category.Builder builder = Category.Builder.newCategory(name).withCode(code).missing(missing != null && Boolean.valueOf(missing));
    while(reader.hasMoreChildren()) {
      reader.moveDown();
      if(isAttributesNode(reader.getNodeName())) {
        unmarshallAttributes(builder, reader, context);
      }
      reader.moveUp();
    }
    return builder.build();
  }

  @Override
  void addAttribute(Object current, Attribute attribute) {
    Category.Builder builder = (Category.Builder) current;
    builder.addAttribute(attribute);
  }
}
