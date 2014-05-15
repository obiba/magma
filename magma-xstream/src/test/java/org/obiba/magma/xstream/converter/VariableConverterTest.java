package org.obiba.magma.xstream.converter;

import java.util.Locale;
import java.util.Set;

import org.junit.Test;
import org.obiba.magma.Attribute;
import org.obiba.magma.AttributeAware;
import org.obiba.magma.Category;
import org.obiba.magma.Variable;
import org.obiba.magma.test.AbstractMagmaTest;
import org.obiba.magma.type.TextType;

import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.thoughtworks.xstream.XStream;

import static org.fest.assertions.api.Assertions.assertThat;

public class VariableConverterTest extends AbstractMagmaTest {

  @Test
  public void testBasicVariable() {
    XStream xstream = new XStream();
    xstream.registerConverter(new VariableConverter(xstream.getMapper()));

    Variable v = newVariable().build();
    String xml = xstream.toXML(v);
    Variable unmarshalled = (Variable) xstream.fromXML(xml);

    assertThat(v.getName()).isEqualTo(unmarshalled.getName());
    assertThat(v.getValueType()).isEqualTo(unmarshalled.getValueType());
    assertThat(v.getEntityType()).isEqualTo(unmarshalled.getEntityType());
  }

  @Test
  public void testVariableWithAttributes() {
    XStream xstream = new XStream();
    xstream.registerConverter(new VariableConverter(xstream.getMapper()));
    xstream.registerConverter(new AttributeConverter());

    Variable v = newVariable()//
        .addAttribute("firstAttribute", "firstValue")//
        .addAttribute("secondAttribute", "secondValue", Locale.ENGLISH)//
        .addAttribute(Attribute.Builder.newAttribute("namespaced").withNamespace("ns1").withValue("ns1").build())//
        .addAttribute(Attribute.Builder.newAttribute("namespaced").withNamespace("ns2").withLocale(Locale.ENGLISH)
            .withValue("ns2").build())//
        .build();

    String xml = xstream.toXML(v);
    AttributeAware unmarshalled = (AttributeAware) xstream.fromXML(xml);
    assertThat(unmarshalled.hasAttribute("firstAttribute")).isTrue();
    assertThat(unmarshalled.getAttribute("firstAttribute").getValue().toString()).isEqualTo("firstValue");

    assertThat(unmarshalled.hasAttribute("secondAttribute")).isTrue();
    assertThat(unmarshalled.getAttribute("secondAttribute").isLocalised()).isTrue();
    assertThat(unmarshalled.getAttribute("secondAttribute", Locale.ENGLISH).getValue().toString())
        .isEqualTo("secondValue");

    assertThat(unmarshalled.hasAttribute("ns1", "namespaced")).isTrue();
    assertThat(unmarshalled.hasAttribute("ns1", "namespaced")).isTrue();
    assertThat(unmarshalled.getAttribute("ns1", "namespaced").getValue().toString()).isEqualTo("ns1");
  }

  @Test
  public void testVariableWithCategories() {
    XStream xstream = new XStream();
    xstream.registerConverter(new VariableConverter(xstream.getMapper()));
    xstream.registerConverter(new AttributeConverter());
    xstream.registerConverter(new CategoryConverter(xstream.getMapper()));

    final Set<String> names = ImmutableSet.of("YES", "NO", "DNK", "PNA");

    Variable v = newVariable().addCategories("YES", "NO")
        .addCategory(Category.Builder.newCategory("DNK").withCode("8888").build()).addCategory(
            Category.Builder.newCategory("PNA").withCode("9999").addAttribute(
                Attribute.Builder.newAttribute("label").withValue(Locale.ENGLISH, "Prefer not to answer").build())
                .build()).build();
    String xml = xstream.toXML(v);
    Variable unmarshalled = (Variable) xstream.fromXML(xml);
    assertThat(unmarshalled.getCategories()).isNotNull();

    assertThat(Iterables.any(unmarshalled.getCategories(), new Predicate<Category>() {
      @Override
      public boolean apply(Category input) {
        return names.contains(input.getName());
      }
    })).isTrue();
  }

  protected Variable.Builder newVariable() {
    return Variable.Builder.newVariable("Test.Variable", TextType.get(), "Participant");
  }

}
