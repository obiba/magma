/*******************************************************************************
 * Copyright (c) 2012 OBiBa. All rights reserved.
 *  
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *  
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.magma;

import java.util.Locale;

import junit.framework.Assert;

import org.junit.Test;
import org.obiba.magma.test.AbstractMagmaTest;

public class AttributesTest extends AbstractMagmaTest {

  @Test
  public void test_decodeFromHeader_handlesNameOnly() {
    Attribute attribute = Attributes.decodeFromHeader("label").build();
    Assert.assertEquals("label", attribute.getName());
    Assert.assertNull(attribute.getNamespace());
    Assert.assertNull(attribute.getLocale());
  }

  @Test
  public void test_decodeFromHeader_handlesNameAndLocale() {
    Attribute attribute = Attributes.decodeFromHeader("label:en").build();
    Assert.assertEquals("label", attribute.getName());
    Assert.assertNull(attribute.getNamespace());
    Assert.assertEquals(new Locale("en"), attribute.getLocale());
  }

  @Test
  public void test_decodeFromHeader_handlesNamespaceAndNameAndLocale() {
    Attribute attribute = Attributes.decodeFromHeader("rdf::label:en").build();
    Assert.assertEquals("label", attribute.getName());
    Assert.assertEquals("rdf", attribute.getNamespace());
    Assert.assertEquals(new Locale("en"), attribute.getLocale());
  }

  @Test
  public void test_decodeFromHeader_handlesNamespaceAndName() {
    Attribute attribute = Attributes.decodeFromHeader("rdf::label").build();
    Assert.assertEquals("label", attribute.getName());
    Assert.assertEquals("rdf", attribute.getNamespace());
    Assert.assertNull(attribute.getLocale());
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
    assertHeader("rdf::label:en", Attribute.Builder.newAttribute("label").withLocale(new Locale("en")).withNamespace("rdf").build());
  }

  @Test
  public void test_encodeForHeader_handlesNamespaceAndName() {
    assertHeader("rdf::label", Attribute.Builder.newAttribute("label").withNamespace("rdf").build());
  }

  private void assertHeader(String expectedHeader, Attribute attribute) {
    Assert.assertEquals(expectedHeader, Attributes.encodeForHeader(attribute));
  }

}
