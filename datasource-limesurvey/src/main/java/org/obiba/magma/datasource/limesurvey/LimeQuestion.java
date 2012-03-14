package org.obiba.magma.datasource.limesurvey;

public class LimeQuestion extends LimeLocalizableEntity {

  private int qid;

  private LimesurveyType type;

  public static LimeQuestion create() {
    return new LimeQuestion();
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

}