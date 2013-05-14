/*
 * Copyright (c) 2013 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.magma.datasource.neo4j.domain;

import java.util.Locale;

import org.springframework.data.neo4j.annotation.RelatedTo;

import com.google.common.base.Strings;

import static org.neo4j.graphdb.Direction.INCOMING;

public class AttributeNode extends AbstractValueAwareNode {

  @RelatedTo(type = "HAS_ATTRIBUTES", direction = INCOMING)
  private AbstractAttributeAwareNode parent;

  private String name;

  private String namespace;

  private String localeStr;

  public AbstractAttributeAwareNode getParent() {
    return parent;
  }

  public void setParent(AbstractAttributeAwareNode parent) {
    this.parent = parent;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getNamespace() {
    return namespace;
  }

  public void setNamespace(String namespace) {
    this.namespace = namespace;
  }

  public String getLocaleStr() {
    return localeStr;
  }

  public void setLocaleStr(String localeStr) {
    this.localeStr = localeStr;
  }

  public Locale getLocale() {
    return isLocalised() ? new Locale(localeStr) : null;
  }

  public boolean isLocalised() {
    return !Strings.isNullOrEmpty(localeStr);
  }

}
