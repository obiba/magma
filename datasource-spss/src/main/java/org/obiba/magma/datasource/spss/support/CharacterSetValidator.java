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

import java.awt.event.KeyEvent;

import javax.annotation.Nullable;

import com.google.common.base.Strings;

public final class CharacterSetValidator {

  private CharacterSetValidator() {
  }

  @SuppressWarnings("ConstantConditions")
  public static void validate(@Nullable String source) throws SpssInvalidCharacterException {
    if(Strings.isNullOrEmpty(source)) return;

    for(int i = 0; i < source.length(); i++) {
      if(!isPrintableChar(source.charAt(i))) {
        throw new SpssInvalidCharacterException("String contains a non-printable character'");
      }
    }

  }

  private static boolean isPrintableChar(char c) {
    Character.UnicodeBlock block = Character.UnicodeBlock.of(c);
    return !Character.isISOControl(c) && c != KeyEvent.CHAR_UNDEFINED && block != null &&
        block != Character.UnicodeBlock.SPECIALS;
  }
}
