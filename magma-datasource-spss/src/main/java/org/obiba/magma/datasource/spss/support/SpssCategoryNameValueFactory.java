/*
 * Copyright (c) 2013 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.magma.datasource.spss.support;

import org.obiba.magma.Value;
import org.obiba.magma.ValueType;
import org.opendatafoundation.data.spss.SPSSVariable;

public class SpssCategoryNameValueFactory extends SpssValueFactory {

  private final String category;

  public SpssCategoryNameValueFactory(String category, int variableIndex, SPSSVariable spssVariable,
      ValueType valueType) {
    super(variableIndex, spssVariable, valueType);
    this.category = category;
  }

  @Override
  public Value create() {
    try {
      return createValue();
    } catch(SpssInvalidCharacterException e) {
      throw new SpssDatasourceParsingException("Failed to create variable", spssVariable.getName(), variableIndex,
          "InvalidCategoryCharsetCharacter", variableIndex, spssVariable.getName(), e.getSource());
    }
  }

  @Override
  protected String getValue() {
    return category;
  }
}
