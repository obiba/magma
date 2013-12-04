package org.obiba.magma.datasource.limesurvey;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.jdbc.support.rowset.SqlRowSet;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class LimesurveyElementProviderJdbc implements LimesurveyElementProvider {

  private final LimesurveyDatasource datasource;

  private final int sid;

  private final Map<Integer, LimeQuestion> mapQuestions;

  private final Map<Integer, List<LimeAnswer>> mapAnswers;

  private final Map<Integer, LimeAttributes> mapAttributes;

  public LimesurveyElementProviderJdbc(LimesurveyDatasource datasource, int sid) {
    this.datasource = datasource;
    this.sid = sid;
    mapQuestions = new LinkedHashMap<Integer, LimeQuestion>();
    mapAnswers = Maps.newHashMap();
    mapAttributes = Maps.newHashMap();
  }

  @Override
  public Map<Integer, LimeQuestion> queryQuestions() {
    String sqlQuestion = "SELECT * FROM " + datasource.quoteAndPrefix("questions") + " q JOIN " +
        datasource.quoteAndPrefix("groups") +
        " g " //
        + "ON (q.gid=g.gid AND q.language=g.language) " //
        + "WHERE q.sid=? AND q.type!='X' " // X are boilerplate questions
        + "ORDER BY group_order, question_order ASC ";
    SqlRowSet questionsRowSet = datasource.getJdbcTemplate().queryForRowSet(sqlQuestion, sid);

    return toQuestions(questionsRowSet);
  }

  @Override
  public Map<Integer, List<LimeAnswer>> queryExplicitAnswers() {
    String sqlAnswer = "SELECT * FROM " + datasource.quoteAndPrefix("answers") + " WHERE qid=? ORDER BY sortorder";
    for(LimeQuestion question : mapQuestions.values()) {
      SqlRowSet answersRowset = datasource.getJdbcTemplate().queryForRowSet(sqlAnswer, question.getQid());
      List<LimeAnswer> answersList = toAnswers(question, answersRowset);
      mapAnswers.put(question.getQid(), answersList);
    }
    return mapAnswers;
  }

  @Override
  public Map<Integer, LimeAttributes> queryAttributes() {
    StringBuilder sqlAttr = new StringBuilder();
    sqlAttr.append("SELECT qid, attribute, value ");
    sqlAttr.append("FROM ").append(datasource.quoteAndPrefix("question_attributes")).append(" ");
    SqlRowSet sqlRowSet = datasource.getJdbcTemplate().queryForRowSet(sqlAttr.toString());
    while(sqlRowSet.next()) {
      int qid = sqlRowSet.getInt("qid");
      String key = sqlRowSet.getString("attribute");
      String value = sqlRowSet.getString("value");
      if(mapAttributes.containsKey(qid)) {
        mapAttributes.get(qid).attribute(key, value);
      } else {
        mapAttributes.put(qid, LimeAttributes.create().attribute(key, value));
      }
    }
    StringBuilder sqlHelp = new StringBuilder();
    sqlHelp.append("SELECT qid, help, language ");
    sqlHelp.append("FROM ").append(datasource.quoteAndPrefix("questions")).append("");
    datasource.getJdbcTemplate().query(sqlHelp.toString(), new RowCallbackHandler() {
      @Override
      public void processRow(ResultSet rs) throws SQLException {
        String help = rs.getString("help");
        int qid = rs.getInt("qid");
        String key = "help:" + rs.getString("language");
        if(mapAttributes.containsKey(qid)) {
          mapAttributes.get(qid).attribute(key, help);
        } else {
          mapAttributes.put(qid, LimeAttributes.create().attribute(key, help));
        }
      }
    });
    return mapAttributes;
  }

  private Map<Integer, LimeQuestion> toQuestions(SqlRowSet rows) {
    while(rows.next()) {
      int qid = rows.getInt("qid");
      String language = rows.getString("language");
      if(mapQuestions.containsKey(qid)) {
        LimeQuestion question = mapQuestions.get(qid);
        question.addLocalizableAttribute("label:" + language, rows.getString("question"));
      } else {
        LimeQuestion question = LimeQuestion.create();
        question.setName(rows.getString("title"));
        question.setQid(qid);
        question.setGroupId(rows.getInt("gid"));
        question.setParentQid(rows.getInt("parent_qid"));
        question.setType(LimesurveyType._valueOf(rows.getString("type")));
        question.addLocalizableAttribute("label:" + language, rows.getString("question"));
        question.setUseOther("Y".equals(rows.getString("other")));
        question.setScaleId(rows.getInt("scale_id"));
        mapQuestions.put(qid, question);
      }
    }
    return mapQuestions;
  }

  @SuppressWarnings("PMD.NcssMethodCount")
  private List<LimeAnswer> toAnswers(LimeQuestion question, SqlRowSet rows) {
    List<LimeAnswer> answers = Lists.newArrayList();
    Map<String, LimeAnswer> internAnswers = Maps.newHashMap();
    while(rows.next()) {
      String answerName = rows.getString("code");
      String language = rows.getString("language");
      String label = rows.getString("answer");
      Integer scaleId = rows.getInt("scale_id");
      if(internAnswers.containsKey(answerName + scaleId)) {
        LimeAnswer answer = internAnswers.get(answerName + scaleId);
        answer.addLocalizableAttribute("label:" + language, label);
      } else {
        LimeAnswer answer = LimeAnswer.create(answerName);
        answer.setSortorder(rows.getInt("sortorder"));
        answer.setScaleId(rows.getInt("scale_id"));
        answer.addLocalizableAttribute("label:" + language, label);
        internAnswers.put(answerName + scaleId, answer);
        answers.add(answer);
      }
    }
    if(question.isUseOther()) {
      LimeAnswer answer = LimeAnswer.create("-oth-");
      answers.add(answer);
    }
    return answers;
  }

}
