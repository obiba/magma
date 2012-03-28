package org.obiba.magma.datasource.limesurvey;

import java.util.Arrays;
import java.util.List;

import org.obiba.magma.ValueType;
import org.obiba.magma.type.DateType;
import org.obiba.magma.type.DecimalType;
import org.obiba.magma.type.TextType;

import com.google.common.collect.Lists;

public enum LimesurveyType {
  ARRAY_5("A", TextType.get(), new String[] { "1", "2", "3", "4", "5" }), //
  ARRAY_10("B", TextType.get(), new String[] { "1", "2", "3", "4", "5", "6", "7", "8", "9", "10" }), //
  ARRAY_YNU("C", TextType.get(), new String[] { "Y", "N" }), //
  ARRAY_ISD("E", TextType.get(), new String[] { "I", "S", "D" }), //
  ARRAY_BY_COLUMN("H", TextType.get()), //
  ARRAY_DUAL_SCALE("1", TextType.get()), //
  ARRAY_NUMBERS(":", DecimalType.get()), //
  ARRAY_TEXT(";", TextType.get()), //
  DATE("D", DateType.get()), //
  FILE_UPLOAD("|", TextType.get()), //
  GENDER("G", TextType.get(), new String[] { "M", "F" }), //
  NUMERICAL_INPUT("N", DecimalType.get()), //
  MULTIPLE_NUMERICAL_INPUT("K", DecimalType.get()), //
  RANKING("R", TextType.get()), //
  YES_NO("Y", TextType.get(), new String[] { "Y", "N" }), //
  MULTIPLE_CHOICE("M", TextType.get(), new String[] { "Y", "N" }), //
  MULTIPLE_CHOICE_COMMENTS("P", TextType.get(), true, new String[] { "Y", "N" }), //
  FIVE_POINT_CHOICE("5", TextType.get(), new String[] { "1", "2", "3", "4", "5" }), //
  LIST_DROPDOWN("!", TextType.get()), //
  LIST_RADIO("L", TextType.get()), //
  LIST_WITH_COMMENT("O", TextType.get(), true), //
  SHORT_FREE_TEXT("S", TextType.get()), //
  LONG_FREE_TEXT("T", TextType.get()), //
  HUGE_FREE_TEXT("U", TextType.get()), //
  MULTIPLE_SHORT_TEXT("Q", TextType.get());

  private String label;

  private List<String> answers;

  private ValueType type;

  private boolean commentable;

  LimesurveyType(String label, ValueType type, String... answers) {
    this.label = label;
    this.type = type;
    this.answers = Arrays.asList(answers);
    this.commentable=false;
  }

  LimesurveyType(String label, ValueType type, boolean commentable, String... answers) {
    this.label = label;
    this.type = type;
    this.commentable = commentable;
    this.answers = Arrays.asList(answers);
  }

  public boolean hasImplicitCategories() {
    return answers.isEmpty() == false;
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
