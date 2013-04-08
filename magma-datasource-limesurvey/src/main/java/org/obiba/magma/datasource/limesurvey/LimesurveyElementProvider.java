package org.obiba.magma.datasource.limesurvey;

import java.util.List;
import java.util.Map;

public interface LimesurveyElementProvider {

  Map<Integer, LimeQuestion> queryQuestions();

  Map<Integer, List<LimeAnswer>> queryExplicitAnswers();

  Map<Integer, LimeAttributes> queryAttributes();

}
