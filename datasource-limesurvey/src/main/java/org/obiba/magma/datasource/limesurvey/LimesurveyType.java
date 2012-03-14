package org.obiba.magma.datasource.limesurvey;

import java.util.Arrays;
import java.util.List;

import org.obiba.magma.ValueType;
import org.obiba.magma.type.BinaryType;
import org.obiba.magma.type.DateType;
import org.obiba.magma.type.DecimalType;
import org.obiba.magma.type.TextType;

public enum LimesurveyType {
  ARRAY_5("A", TextType.get(), "1", "2", "3", "4", "5"), //
  ARRAY_10("B", TextType.get(), "1", "2", "3", "4", "5", "6", "7", "8", "9", "10"), //
  ARRAY_YNU("C", TextType.get(), "Y", "N"), //
  ARRAY_ISD("E", TextType.get(), "I", "S", "D"), //
  ARRAY_BY_COLUMN("H", TextType.get()), //
  ARRAY_DUAL_SCALE("1", TextType.get()), //
  ARRAY_NUMBERS(":", DecimalType.get()), //
  ARRAY_TEXT(";", TextType.get()), //
  DATE("D", DateType.get()), //
  FILE_UPLOAD("|", BinaryType.get()), // text/integer ?
  GENDER("G", TextType.get(), "M", "F"), //
  NUMERICAL_INPUT("N", DecimalType.get()), //
  MULTIPLE_NUMERICAL_INPUT("K", DecimalType.get()), //
  RANKING("R", TextType.get()), //
  TEXT_DISPLAY("X", TextType.get()), //
  YES_NO("Y", TextType.get(), "Y", "N"), //
  MULTIPLE_CHOICE("M", TextType.get(), "Y", "N"), //
  MULTIPLE_CHOICE_COMMENTS("P", TextType.get(), "Y", "N"), //
  FIVE_POINT_CHOICE("5", TextType.get(), "1", "2", "3", "4", "5"), //
  LIST_DROPDOWN("!", TextType.get()), //
  LIST_RADIO("L", TextType.get()), //
  LIST_WITH_COMMENT("O", TextType.get()), //
  SHORT_FREE_TEXT("S", TextType.get()), //
  LONG_FREE_TEXT("T", TextType.get()), //
  HUGE_FREE_TEXT("U", TextType.get()), //
  MULTIPLE_SHOR_TEXT("Q", TextType.get());

  private String label;

  private List<String> answers;

  private ValueType type;

  LimesurveyType(String label, ValueType type, String... answers) {
    this.label = label;
    this.type = type;
    this.answers = Arrays.asList(answers);
  }

  public boolean hasImplicitCategories() {
    return answers.isEmpty() == false;
  }

  public List<String> getImplicitAnswers() {
    return answers;
  }

  public static LimesurveyType _valueOf(String value) {
    for(LimesurveyType e : values()) {
      if(e.label.equals(value)) {
        return e;
      }
    }
    return null;
  }

  public ValueType getType() {
    return type;
  }

}
