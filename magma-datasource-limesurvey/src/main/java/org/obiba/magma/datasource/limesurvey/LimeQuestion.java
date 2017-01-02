/*
 * Copyright (c) 2017 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

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
  }

  public static LimeQuestion create() {
    return new LimeQuestion();
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
    useOther = other;
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
  public Map<String, LimeAttributes> getImplicitLabel() {
    return new HashMap<String, LimeAttributes>() {
      private static final long serialVersionUID = -4789211495142871372L;

      {
        put("other", LimeAttributes.create().attribute("label:en", "Other").attribute("label:fr", "Autre"));
        put("comment", LimeAttributes.create().attribute("label:en", "Comment").attribute("label:fr", "Commentaire"));
      }
    };
  }

}