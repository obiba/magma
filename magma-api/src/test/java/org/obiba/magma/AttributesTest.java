/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.magma;

import java.util.Locale;

import org.junit.Test;

import static org.fest.assertions.api.Assertions.assertThat;

public class AttributesTest extends MagmaTest {

  @Test
  public void test_decodeFromHeader_handlesNameOnly() {
    Attribute attribute = Attributes.decodeFromHeader("label").build();
    assertThat(attribute.getName()).isEqualTo("label");
    assertThat(attribute.hasNamespace()).isFalse();
    assertThat(attribute.isLocalised()).isFalse();
  }

  @Test
  public void test_decodeFromHeader_handlesNameAndLocale() {
    Attribute attribute = Attributes.decodeFromHeader("label:en").build();
    assertThat(attribute.getName()).isEqualTo("label");
    assertThat(attribute.hasNamespace()).isFalse();
    assertThat(attribute.getLocale()).isEqualTo(new Locale("en"));
  }

  @Test
  public void test_decodeFromHeader_handlesNamespaceAndNameAndLocale() {
    Attribute attribute = Attributes.decodeFromHeader("rdf::label:en").build();
    assertThat(attribute.getName()).isEqualTo("label");
    assertThat(attribute.getNamespace()).isEqualTo("rdf");
    assertThat(attribute.getLocale()).isEqualTo(new Locale("en"));
  }

  @Test
  public void test_decodeFromHeader_handlesNamespaceAndName() {
    Attribute attribute = Attributes.decodeFromHeader("rdf::label").build();
    assertThat(attribute.getName()).isEqualTo("label");
    assertThat(attribute.getNamespace()).isEqualTo("rdf");
    assertThat(attribute.isLocalised()).isFalse();
  }

  @Test
  public void test_encodeForHeader_handlesName() {
    assertHeader("label", Attribute.Builder.newAttribute("label").build());
  }

  @Test
  public void test_encodeForHeader_handlesNameAndLocale() {
    assertHeader("label:en", Attribute.Builder.newAttribute("label").withLocale(new Locale("en")).build());
  }

  @Test
  public void test_encodeForHeader_handlesNamespaceAndNameAndLocale() {
    assertHeader("rdf::label:en",
        Attribute.Builder.newAttribute("label").withLocale(new Locale("en")).withNamespace("rdf").build());
  }

  @Test
  public void test_encodeForHeader_handlesNamespaceAndName() {
    assertHeader("rdf::label", Attribute.Builder.newAttribute("label").withNamespace("rdf").build());
  }

  private void assertHeader(String expectedHeader, Attribute attribute) {
    assertThat(expectedHeader).isEqualTo(Attributes.encodeForHeader(attribute));
  }

}
