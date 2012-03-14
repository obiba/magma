package org.obiba.magma.datasource.limesurvey;

public class LimeQuestion extends LimeLocalizableEntity {

  private int qid;

  private LimesurveyType type;

  private boolean other;

  private LimeQuestion() {
    super();
  }

  private LimeQuestion(String name) {
    super(name);
  }

  public static LimeQuestion create() {
    return new LimeQuestion();
  }

  public static LimeQuestion create(String name) {
    return new LimeQuestion(name);
  }

  public int getQid() {
    return qid;
  }

  public void setQid(int qid) {
    this.qid = qid;
  }

  public void setType(LimesurveyType type) {
    this.type = type;
  }

  public LimesurveyType getLimesurveyType() {
    return type;
  }

  public boolean isOther() {
    return other;
  }

  public void setOther(boolean other) {
    this.other = other;
  }

}