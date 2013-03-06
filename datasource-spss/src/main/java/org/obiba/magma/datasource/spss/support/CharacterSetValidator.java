/*
 * Copyright (c) 2012 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.magma.datasource.spss.support;

import javax.annotation.Nullable;

import com.google.common.base.Strings;

public final class CharacterSetValidator {

  private CharacterSetValidator() {
  }

  @SuppressWarnings("ConstantConditions")
  public static void validate(@Nullable String source) throws SpssIsoControlCharacterException {
    if(Strings.isNullOrEmpty(source)) return;

    for(int i = 0; i < source.length(); i++) {
      char ch = source.charAt(i);
      if(Character.isISOControl(ch)) {
        throw new SpssIsoControlCharacterException("String contains ISO Control character '" + Character.toString(ch) + "'");
      }
    }

  }

}
