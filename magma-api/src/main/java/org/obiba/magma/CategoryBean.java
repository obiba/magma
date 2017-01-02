/*
 * Copyright (c) 2017 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.magma;

import java.io.Serializable;

import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.ListMultimap;

class CategoryBean extends AbstractAttributeAware implements Category, Serializable {

  ListMultimap<String, Attribute> attributes = LinkedListMultimap.create();

  String name;

  String code;

  boolean missing;

  CategoryBean(String name, String code) {
    this.name = name;
    this.code = code;
  }

  @Override
  public String getCode() {
    return code;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public boolean isMissing() {
    return missing;
  }

  @Override
  protected ListMultimap<String, Attribute> getInstanceAttributes() {
    return attributes;
  }
}
