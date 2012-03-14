package org.obiba.magma.datasource.limesurvey;

public class LimeAnswer extends LimeLocalizableEntity {

  private LimeAnswer() {
    super();
  }

  private LimeAnswer(String name) {
    super(name);
  }

  public static LimeAnswer create() {
    return new LimeAnswer();
  }

  public static LimeAnswer create(String name) {
    return new LimeAnswer(name);
  }
}