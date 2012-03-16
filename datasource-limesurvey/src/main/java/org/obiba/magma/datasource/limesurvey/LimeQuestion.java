package org.obiba.magma.datasource.limesurvey;

public class LimeQuestion extends LimeLocalizableEntity {

  private int qid;

  private int parentQid;

  private LimesurveyType type;

  // Other text field for question OR subquestions
  private boolean useOther;

  // For '2 dimensions' questions, if =1 : X axis
  private int scaleId;

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

  public boolean hasParentId() {
    return parentQid != 0;
  }

  public int getParentQid() {
    return parentQid;
  }

  public void setParentQid(int parentQid) {
    this.parentQid = parentQid;
  }

  public void setType(LimesurveyType type) {
    this.type = type;
  }

  public LimesurveyType getLimesurveyType() {
    return type;
  }

  public boolean isUseOther() {
    return useOther;
  }

  public void setUseOther(boolean other) {
    this.useOther = other;
  }

  public boolean isScaleEqual1() {
    return scaleId == 1;
  }

  public void setScaleId(int scaleId) {
    this.scaleId = scaleId;
  }

}