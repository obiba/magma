/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.magma.support;

import org.obiba.magma.Variable;
import org.obiba.magma.type.BooleanType;
import org.obiba.magma.type.TextType;

/**
 * Determines the nature of a variable by inspecting its {@code ValueType} and any associated {@code Category} instance.
 */
public enum VariableNature {

  /**
   * A categorical variable: its value can take one of the predefined {@code Category}.
   */
  CATEGORICAL,

  /**
   * A continuous variable: its value can take any value of it's {@code ValueType}. Some values may have a particular
   * meaning: they indicate a missing value. These are defined as missing {@code Category} instances.
   */
  CONTINUOUS,

  /**
   * A temporal variable: it's value is a date or time.
   */
  TEMPORAL,

  /**
   * A geo variable: it's value is a point, line or polygon.
   */
  GEO,

  /**
   * A binary variable: it's a binary file
   */
  BINARY,

  /**
   * None of the above. Variables with {@code LocaleType} will be of this nature.
   */
  UNDETERMINED;

  /**
   * Nature, considering the categories or the value type.
   *
   * @param variable
   * @return
   */
  public static VariableNature getNature(Variable variable) {
    if(variable.hasCategories() && !variable.areAllCategoriesMissing()) {
      return CATEGORICAL;
    }
    return getNatureFromValueType(variable);
  }

  /**
   * Nature without considering the categories.
   *
   * @param variable
   * @return
   */
  private static VariableNature getNatureFromValueType(Variable variable) {
    if(variable.getValueType().isNumeric()) {
      return CONTINUOUS;
    }
    if(variable.getValueType().isDateTime()) {
      return TEMPORAL;
    }
    if(variable.getValueType().equals(BooleanType.get())) {
      return CATEGORICAL;
    }
    if(variable.getValueType().isGeo()) {
      return GEO;
    }
    if(variable.getValueType().isBinary()) {
      return BINARY;
    }
    return UNDETERMINED;
  }

}

