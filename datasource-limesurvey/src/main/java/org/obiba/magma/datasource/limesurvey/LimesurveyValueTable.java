package org.obiba.magma.datasource.limesurvey;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.obiba.magma.Category;
import org.obiba.magma.Datasource;
import org.obiba.magma.Initialisable;
import org.obiba.magma.NoSuchValueSetException;
import org.obiba.magma.Timestamps;
import org.obiba.magma.Value;
import org.obiba.magma.ValueSet;
import org.obiba.magma.ValueType;
import org.obiba.magma.Variable;
import org.obiba.magma.VariableEntity;
import org.obiba.magma.VariableValueSource;
import org.obiba.magma.VectorSource;
import org.obiba.magma.support.AbstractValueTable;
import org.obiba.magma.support.AbstractVariableEntityProvider;
import org.obiba.magma.support.VariableEntityProvider;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class LimesurveyValueTable extends AbstractValueTable {

  private JdbcTemplate jdbcTemplate;

  private final Integer sid;

  public LimesurveyValueTable(Datasource datasource, String name, Integer sid) {
    super(datasource, name);
    this.sid = sid;
  }

  public LimesurveyValueTable(Datasource datasource, String name, Integer sid, VariableEntityProvider variableEntityProvider) {
    super(datasource, name, variableEntityProvider);
    this.sid = sid;
  }

  @Override
  public void initialise() {
    super.initialise();
    initialiseVariableValueSources();
  }

  private void initialiseVariableValueSources() {
    getSources().clear();
    jdbcTemplate = ((LimesurveyDatasource) getDatasource()).getJdbcTemplate();

    Map<String, LimeQuestion> mapQuestions = queryQuestions();
    Map<String, List<LimeAnswer>> mapAnswers = Maps.newHashMap();
    mapAnswers.putAll(queryExplicitAnswers(mapQuestions));
    mapAnswers.putAll(buildImplicitAnswers(mapQuestions));

    buildVariables(mapQuestions, mapAnswers);
  }

  private Map<String, LimeQuestion> queryQuestions() {
    String sqlQuestion = "SELECT * FROM questions WHERE sid=?";
    SqlRowSet questionsRowSet = jdbcTemplate.queryForRowSet(sqlQuestion.toString(), new Object[] { sid });
    return toQuestions(questionsRowSet);
  }

  private Map<String, LimeQuestion> toQuestions(SqlRowSet rows) {
    Map<String, LimeQuestion> questions = Maps.newHashMap();
    while(rows.next()) {
      String questionName = rows.getString("title");
      if(questions.containsKey(questionName)) {
        LimeQuestion question = questions.get(questionName);

        String language = rows.getString("language");
        String label = rows.getString("question");
        question.addLocalizableAttribute(language, label);
      } else {
        LimeQuestion question = LimeQuestion.create();
        int qid = rows.getInt("qid");
        String type = rows.getString("type");
        String language = rows.getString("language");
        String label = rows.getString("question");
        String other = rows.getString("other");

        question.setName(questionName);
        question.setQid(qid);
        question.setType(LimesurveyType._valueOf(type));
        question.addLocalizableAttribute(language, label);
        question.setOther("Y".equals(other) ? true : false);

        questions.put(questionName, question);
      }
    }
    return questions;

  }

  private Map<String, List<LimeAnswer>> queryExplicitAnswers(Map<String, LimeQuestion> mapQuestions) {
    Map<String, List<LimeAnswer>> answers = Maps.newHashMap();
    String sqlAnswer = "SELECT * FROM answers WHERE qid=?";
    for(LimeQuestion question : mapQuestions.values()) {
      SqlRowSet answersRowset = jdbcTemplate.queryForRowSet(sqlAnswer, new Object[] { question.getQid() });
      List<LimeAnswer> answersList = toAnswers(question, answersRowset);
      answers.put(question.getName(), answersList);
    }
    return answers;
  }

  private List<LimeAnswer> toAnswers(LimeQuestion question, SqlRowSet rows) {
    List<LimeAnswer> answers = Lists.newArrayList();
    Map<String, LimeAnswer> internAnswers = Maps.newHashMap();
    while(rows.next()) {
      String answerName = rows.getString("code");
      if(internAnswers.containsKey(answerName)) {
        LimeAnswer answer = internAnswers.get(answerName);

        String language = rows.getString("language");
        String label = rows.getString("answer");
        answer.addLocalizableAttribute(language, label);
      } else {
        LimeAnswer answer = LimeAnswer.create();
        String language = rows.getString("language");
        String label = rows.getString("answer");

        answer.setName(answerName);
        answer.addLocalizableAttribute(language, label);

        internAnswers.put(answerName, answer);
        answers.add(answer);
      }
    }

    return answers;

  }

  private Map<String, List<LimeAnswer>> buildImplicitAnswers(Map<String, LimeQuestion> mapQuestions) {
    Map<String, List<LimeAnswer>> mapAnswers = Maps.newHashMap();
    for(String questionName : mapQuestions.keySet()) {
      LimeQuestion question = mapQuestions.get(questionName);
      LimesurveyType type = question.getLimesurveyType();
      if(type.hasImplicitCategories()) {
        List<LimeAnswer> answers = Lists.newArrayList();
        for(String implicitAnswer : type.getImplicitAnswers()) {
          LimeAnswer answer = LimeAnswer.create(implicitAnswer);
          answers.add(answer);
        }
        if(question.isOther()) {
          LimeAnswer answer = LimeAnswer.create("-oth-");
          answers.add(answer);
        }

        mapAnswers.put(questionName, answers);
      }
    }
    return mapAnswers;
  }

  private void buildVariables(Map<String, LimeQuestion> mapQuestions, Map<String, List<LimeAnswer>> mapAnswers) {
    for(LimeQuestion question : mapQuestions.values()) {
      // TODO put Participant elsewhere
      Variable.Builder vb = Variable.Builder.newVariable(question.getName(), question.getLimesurveyType().getType(), "Participant");
      for(Map.Entry<String, String> attr : question.getLocalizableLabel().entrySet()) {
        vb.addAttribute(attr.getKey(), attr.getValue());
      }
      for(LimeAnswer answer : mapAnswers.get(question.getName())) {
        Category.Builder cb = Category.Builder.newCategory(answer.getName());
        for(Map.Entry<String, String> attr : answer.getLocalizableLabel().entrySet()) {
          cb.addAttribute(attr.getKey(), attr.getValue());
        }
        vb.addCategory(cb.build());
      }
      Variable build = vb.build();

      // System.out.println(build.getName() + " " + build.getValueType().getName() + " " + build.getAttributes());
      // for(Category c : build.getCategories()) {
      // System.out.println("    " + c.getName() + " " + c.getAttributes());
      // }

      LimesurveyVariableValueSource variable = new LimesurveyVariableValueSource(build);

      addVariableValueSource(variable);

    }
  }

  @Override
  public ValueSet getValueSet(VariableEntity entity) throws NoSuchValueSetException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Timestamps getTimestamps() {
    // TODO Auto-generated method stub
    return null;
  }

  class LimesurveyVariableEntityProvider extends AbstractVariableEntityProvider implements Initialisable {

    private Set<VariableEntity> entities = new LinkedHashSet<VariableEntity>();

    protected LimesurveyVariableEntityProvider(String entityType) {
      super(entityType);
    }

    @Override
    public void initialise() {
      // TODO Auto-generated method stub

    }

    @Override
    public Set<VariableEntity> getVariableEntities() {
      return Collections.unmodifiableSet(entities);
    }

  }

  class LimesurveyVariableValueSource implements VariableValueSource {

    private Variable variable;

    public LimesurveyVariableValueSource(Variable variable) {
      this.variable = variable;
    }

    @Override
    public ValueType getValueType() {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public Value getValue(ValueSet valueSet) {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public VectorSource asVectorSource() {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public Variable getVariable() {
      return variable;
    }

  }

  private String asString(Object o) {
    return (String) o;
  }

}
