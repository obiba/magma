package org.obiba.magma;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.junit.Test;
import org.obiba.magma.test.AbstractMagmaTest;
import org.obiba.magma.type.IntegerType;
import org.obiba.magma.type.TextType;

import static org.fest.assertions.api.Assertions.assertThat;

@SuppressWarnings({ "PMD.NcssMethodCount", "OverlyLongMethod" })
public class VariableTest extends AbstractMagmaTest {

  @Test(expected = IllegalArgumentException.class)
  public void testNullName() {
    Variable.Builder.newVariable(null, IntegerType.get(), "entityType");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullValueType() {
    Variable.Builder.newVariable("Name", null, "entityType");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullEntityType() {
    Variable.Builder.newVariable("Name", IntegerType.get(), null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testIllegalVariableName() {
    Variable.Builder.newVariable("Name:WithColon", IntegerType.get(), "entityType");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testEmptyVariableName() {
    Variable.Builder.newVariable("", IntegerType.get(), "entityType");
  }

  @Test
  public void test_isMissingValue_TextType() {
    Variable v = Variable.Builder.newVariable("name", TextType.get(), "entityType").addCategory("YES", null, false)
        .addCategory("DNK", null, true).build();
    assertThat(v.isMissingValue(TextType.get().valueOf("YES"))).isFalse();
    // Accepts unknown categories
    assertThat(v.isMissingValue(TextType.get().valueOf("No such category"))).isFalse();
    assertThat(v.isMissingValue(TextType.get().valueOf("DNK"))).isTrue();
    assertThat(v.isMissingValue(TextType.get().nullValue())).isTrue();
  }

  @Test
  public void test_isMissingValue_IntegerType() {
    Variable v = Variable.Builder.newVariable("name", IntegerType.get(), "entityType").addCategory("1", null, false)
        .addCategory("8888", null, true).build();
    assertThat(v.isMissingValue(IntegerType.get().valueOf(1))).isFalse();
    // Accepts unknown categories
    assertThat(v.isMissingValue(IntegerType.get().valueOf(2))).isFalse();
    assertThat(v.isMissingValue(IntegerType.get().valueOf(8888))).isTrue();
    assertThat(v.isMissingValue(IntegerType.get().nullValue())).isTrue();
  }

  @Test
  public void test_isMissingValue_withoutCategories() {
    Variable v = Variable.Builder.newVariable("name", IntegerType.get(), "entityType").build();
    // Accepts unknown categories
    assertThat(v.isMissingValue(IntegerType.get().valueOf(1))).isFalse();
    assertThat(v.isMissingValue(IntegerType.get().nullValue())).isTrue();
  }

  @Test
  public void testOverrideReplacesOnlyExistingAttributes() throws Exception {
    Variable.Builder variableBuilder = Variable.Builder.newVariable("variable", TextType.get(), "Participant");
    variableBuilder.addAttribute("A.1", "Hello");
    variableBuilder.addAttribute("A.2", "World");
    variableBuilder.addAttribute("A.3", "How goes?");
    Variable variable = variableBuilder.build();

    Variable.Builder overrideBuilder = Variable.Builder.newVariable("override", TextType.get(), "Participant");
    overrideBuilder.addAttribute("A.2", "Earth");
    overrideBuilder.addAttribute("A.3", "How goes?");
    Variable override = overrideBuilder.build();

    Variable.Builder derivedBuilder = Variable.Builder.sameAs(variable);
    derivedBuilder.overrideWith(override);
    Variable derived = derivedBuilder.build();

    assertThat(derived.getAttribute("A.1").getValue()).isEqualTo(TextType.get().valueOf("Hello"));
    assertThat(derived.getAttribute("A.2").getValue()).isEqualTo(TextType.get().valueOf("Earth"));
    assertThat(derived.getAttribute("A.3").getValue()).isEqualTo(TextType.get().valueOf("How goes?"));
    assertThat(derived.getAttributes()).hasSize(3);
  }

  @Test
  public void testOverrideReplacesOnlyExistingAttributesWithLocales() throws Exception {
    Variable.Builder variableBuilder = Variable.Builder.newVariable("variable", TextType.get(), "Participant");
    variableBuilder.addAttribute("A.1", "Hello", Locale.CANADA);
    variableBuilder.addAttribute("A.2", "World", Locale.CANADA);
    variableBuilder.addAttribute("A.3", "How goes?", Locale.CANADA);
    variableBuilder.addAttribute("A.4", "Saturn", Locale.CANADA);
    variableBuilder.addAttribute("A.5", "Halifax", Locale.CANADA);
    Variable variable = variableBuilder.build();

    Variable.Builder overrideBuilder = Variable.Builder.newVariable("override", TextType.get(), "Participant");
    overrideBuilder.addAttribute("A.2", "Earth", Locale.CANADA);
    overrideBuilder.addAttribute("A.3", "How goes?", Locale.CANADA);
    overrideBuilder.addAttribute("A.4", "Mars", Locale.CANADA); // Overrides Saturn.
    overrideBuilder.addAttribute("A.5", "New York", Locale.US); // Does not override Halifax. Locale is different.
    Variable override = overrideBuilder.build();

    Variable.Builder derivedBuilder = Variable.Builder.sameAs(variable);
    derivedBuilder.overrideWith(override);
    Variable derived = derivedBuilder.build();

    assertThat(derived.getAttribute("A.1").getValue()).isEqualTo(TextType.get().valueOf("Hello"));
    assertThat(derived.getAttribute("A.2").getValue()).isEqualTo(TextType.get().valueOf("Earth"));
    assertThat(derived.getAttribute("A.3").getValue()).isEqualTo(TextType.get().valueOf("How goes?"));
    assertThat(derived.getAttribute("A.4").getValue()).isEqualTo(TextType.get().valueOf("Mars"));
    assertThat(derived.getAttributes("A.5")).hasSize(2);
    assertThat(derived.getAttributes()).hasSize(6);
  }

  @Test
  public void testOverrideWithMinimalVariablesPresentInTheOverrideVariable() throws Exception {
    Variable.Builder variableBuilder = Variable.Builder.newVariable("variable", IntegerType.get(), "Participant");
    variableBuilder.mimeType("text/html");
    variableBuilder.occurrenceGroup("group");
    variableBuilder.unit("kg");
    variableBuilder.repeatable();
    variableBuilder.index(10);
    Variable variable = variableBuilder.build();

    Variable.Builder overrideBuilder = Variable.Builder.newVariable("override", TextType.get(), "Workstation");
    Variable override = overrideBuilder.build();

    Variable.Builder derivedBuilder = Variable.Builder.sameAs(variable);
    derivedBuilder.overrideWith(override);
    Variable derived = derivedBuilder.build();

    // Overridden
    assertThat(derived.getName()).isEqualTo("override");
    assertThat(derived.getValueType()).isEqualTo(TextType.get());
    assertThat(derived.getEntityType()).isEqualTo("Workstation");
    // Not overridden
    assertThat(derived.getMimeType()).isEqualTo("text/html");
    assertThat(derived.getOccurrenceGroup()).isEqualTo("group");
    assertThat(derived.getUnit()).isEqualTo("kg");
    assertThat(derived.isRepeatable()).isFalse();
    assertThat(derived.getIndex()).isEqualTo(0);
  }

  @Test
  public void testOverrideWithSomeVariablesPresentInTheOverrideVariable() throws Exception {
    Variable.Builder variableBuilder = Variable.Builder.newVariable("variable", IntegerType.get(), "Participant");
    variableBuilder.mimeType("text/html");
    variableBuilder.occurrenceGroup("group");
    variableBuilder.unit("kg");
    variableBuilder.repeatable();
    Variable variable = variableBuilder.build();

    Variable.Builder overrideBuilder = Variable.Builder.newVariable("override", TextType.get(), "Workstation");
    overrideBuilder.mimeType("image/png");
    overrideBuilder.occurrenceGroup("occurrenceGroup");
    overrideBuilder.unit("cm");
    Variable override = overrideBuilder.build();

    Variable.Builder derivedBuilder = Variable.Builder.sameAs(variable);
    derivedBuilder.overrideWith(override);
    Variable derived = derivedBuilder.build();

    // Overridden
    assertThat(derived.getName()).isEqualTo("override");
    assertThat(derived.getValueType()).isEqualTo(TextType.get());
    assertThat(derived.getEntityType()).isEqualTo("Workstation");
    assertThat(derived.getMimeType()).isEqualTo("image/png");
    assertThat(derived.getOccurrenceGroup()).isEqualTo("occurrenceGroup");
    assertThat(derived.getUnit()).isEqualTo("cm");
    // Not overridden
    assertThat(derived.isRepeatable()).isFalse();
  }

  @Test
  public void testOverrideOfCategoryAttributes() throws Exception {
    Category.Builder pnaBuilder = Category.Builder.newCategory("PNA").withCode("88").missing(true);
    pnaBuilder.addAttribute("label", "Prefer not to answer", Locale.ENGLISH);
    pnaBuilder.addAttribute("A.1", "Hello", Locale.ENGLISH);
    Category pna = pnaBuilder.build();

    Variable.Builder variableBuilder = Variable.Builder.newVariable("variable", IntegerType.get(), "Participant");
    variableBuilder.addCategory(pna);
    Variable variable = variableBuilder.build();

    Category.Builder pnaOverrideBuilder = Category.Builder.newCategory("PNA").withCode("88").missing(true);
    pnaOverrideBuilder.addAttribute("label", "Prefer not to answer", Locale.ENGLISH);
    pnaOverrideBuilder.addAttribute("label", "Préfère ne pas répondre", Locale.FRENCH);
    pnaOverrideBuilder.addAttribute("A.1", "World", Locale.ENGLISH);
    pnaOverrideBuilder.addAttribute("A.2", "Earth");
    Category pnaOverride = pnaOverrideBuilder.build();

    Variable.Builder overrideBuilder = Variable.Builder.newVariable("override", TextType.get(), "Workstation");
    overrideBuilder.addCategory(pnaOverride);
    Variable override = overrideBuilder.build();

    Variable.Builder derivedBuilder = Variable.Builder.sameAs(variable);
    derivedBuilder.overrideWith(override);
    Variable derived = derivedBuilder.build();

    assertThat(derived.hasCategories()).isTrue();
    List<Category> categories = new ArrayList<>(derived.getCategories());
    assertThat(categories).hasSize(1);
    Category category = categories.get(0);
    assertThat(category.getAttribute("label", Locale.ENGLISH).getValue())
        .isEqualTo(TextType.get().valueOf("Prefer not to answer"));
    assertThat(category.getAttribute("label", Locale.FRENCH).getValue())
        .isEqualTo(TextType.get().valueOf("Préfère ne pas répondre"));
    assertThat(category.getAttribute("A.1", Locale.ENGLISH).getValue()).isEqualTo(TextType.get().valueOf("World"));
    assertThat(category.getAttribute("A.2").getValue()).isEqualTo(TextType.get().valueOf("Earth"));
    assertThat(category.getAttributes()).hasSize(4);
  }
}
