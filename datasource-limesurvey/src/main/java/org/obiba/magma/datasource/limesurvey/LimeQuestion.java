package org.obiba.magma.datasource.limesurvey;

import java.util.HashMap;
import java.util.Map;

public class LimeQuestion extends LimeLocalizableEntity {

  private int qid;

  private int parentQid;

  private LimesurveyType type;

  // Other text field for question OR subquestions
  private boolean useOther;

  private int scaleId;

  private int groupId;

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

  public int getGroupId() {
    return groupId;
  }

  public void setGroupId(int groupId) {
    this.groupId = groupId;
  }

  @Override
  public Map<String, LimeLocalizableAttributes> getImplicitLabel() {
    return new HashMap<String, LimeLocalizableAttributes>() {

      private static final long serialVersionUID = -4789211495142871372L;
      {
        put("other", LimeLocalizableAttributes.create().localizableAttribute("en", "Other").localizableAttribute("fr", "Autre"));
        put("comment", LimeLocalizableAttributes.create().localizableAttribute("en", "Comment").localizableAttribute("fr", "Commentaire"));
      }
    };
  }

}