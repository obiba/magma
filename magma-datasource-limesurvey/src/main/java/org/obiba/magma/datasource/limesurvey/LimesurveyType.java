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

import java.util.Arrays;
import java.util.List;

import org.obiba.magma.ValueType;
import org.obiba.magma.type.DateType;
import org.obiba.magma.type.DecimalType;
import org.obiba.magma.type.TextType;

public enum LimesurveyType {
  ARRAY_5("A", TextType.get(), "1", "2", "3", "4", "5"), //
  ARRAY_10("B", TextType.get(), "1", "2", "3", "4", "5", "6", "7", "8", "9", "10"), //
  ARRAY_YNU("C", TextType.get(), "Y", "N", "U"), //
  ARRAY_ISD("E", TextType.get(), "I", "S", "D"), //
  ARRAY_BY_COLUMN("H", TextType.get()), //
  ARRAY_DUAL_SCALE("1", TextType.get()), //
  ARRAY_NUMBERS(":", DecimalType.get()), //
  ARRAY_TEXT(";", TextType.get()), //
  ARRAY_FLEXIBLE_LABELS("F", TextType.get()), //
  DATE("D", DateType.get()), //
  FILE_UPLOAD("|", TextType.get()), //
  GENDER("G", TextType.get(), "M", "F"), //
  NUMERICAL_INPUT("N", DecimalType.get()), //
  MULTIPLE_NUMERICAL_INPUT("K", DecimalType.get()), //
  RANKING("R", TextType.get()), //
  YES_NO("Y", TextType.get(), "Y", "N"), //
  MULTIPLE_CHOICE("M", TextType.get(), "Y", "N"), //
  MULTIPLE_CHOICE_COMMENTS("P", TextType.get(), true, "Y", "N"), //
  FIVE_POINT_CHOICE("5", TextType.get(), "1", "2", "3", "4", "5"), //
  LIST_DROPDOWN("!", TextType.get()), //
  LIST_RADIO("L", TextType.get()), //
  LIST_WITH_COMMENT("O", TextType.get(), true), //
  SHORT_FREE_TEXT("S", TextType.get()), //
  LONG_FREE_TEXT("T", TextType.get()), //
  HUGE_FREE_TEXT("U", TextType.get()), //
  MULTIPLE_SHORT_TEXT("Q", TextType.get());

  private final String label;

  private final List<String> answers;

  private final ValueType type;

  private final boolean commentable;

  LimesurveyType(String label, ValueType type, String... answers) {
    this.label = label;
    this.type = type;
    this.answers = Arrays.asList(answers);
    commentable = false;
  }

  LimesurveyType(String label, ValueType type, boolean commentable, String... answers) {
    this.label = label;
    this.type = type;
    this.commentable = commentable;
    this.answers = Arrays.asList(answers);
  }

  public boolean hasImplicitCategories() {
    return !answers.isEmpty();
  }

  public List<String> getImplicitAnswers() {
    return answers;
  }

  // special valueOf() because given value is different
  public static LimesurveyType _valueOf(String value) {
    for(LimesurveyType e : values()) {
      if(e.label.equals(value)) {
        return e;
      }
    }
    return null;
  }

  public boolean isCommentable() {
    return commentable;
  }

  public ValueType getType() {
    return type;
  }

}
