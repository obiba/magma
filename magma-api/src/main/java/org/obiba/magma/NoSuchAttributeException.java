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

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

public class NoSuchAttributeException extends MagmaRuntimeException {

  private static final long serialVersionUID = 5887330656285998606L;

  @NotNull
  private final String attributeName;

  public NoSuchAttributeException(@NotNull String attribute, @NotNull String attributeAware) {
    this(attribute, null, attributeAware);
  }

  public NoSuchAttributeException(@NotNull String attribute, @Nullable Locale locale, @NotNull String attributeAware) {
    super("No such attribute '" + attribute + (locale == null ? "'" : "'@" + locale.toString()) + " for '" +
        attributeAware + "'");
    attributeName = attribute;
  }

  @NotNull
  public String getAttributeName() {
    return attributeName;
  }

}
