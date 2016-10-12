/*
 * Copyright (c) 2016 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.magma.datasource.limesurvey;

import java.util.Map;

import javax.annotation.Nullable;

import org.obiba.magma.Attribute;

public abstract class LimeLocalizableEntity {

  @Nullable
  private String name;

  private final LimeAttributes localizableAttributes;

  public LimeLocalizableEntity() {
    localizableAttributes = LimeAttributes.create();
  }

  public LimeLocalizableEntity(@Nullable String name) {
    this();
    this.name = name;
  }

  @Nullable
  public String getName() {
    return name;
  }

  public void setName(@Nullable String name) {
    this.name = name;
  }

  public Iterable<Attribute> getMagmaAttributes(boolean keepOriginalLocalizable) {
    return localizableAttributes.toMagmaAttributes(keepOriginalLocalizable);
  }

  public void addLocalizableAttribute(String key, String value) {
    localizableAttributes.attribute(key, value);
  }

  public abstract Map<String, LimeAttributes> getImplicitLabel();
}
