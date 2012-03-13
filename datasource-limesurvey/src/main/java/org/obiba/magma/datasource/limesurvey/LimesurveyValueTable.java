package org.obiba.magma.datasource.limesurvey;

import java.util.Collections;
import java.util.LinkedHashSet;
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
import org.obiba.magma.type.TextType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;

import com.google.common.collect.HashMultimap;
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

  // HashMultimap<String, Map<String, Object>> : key is variable name, and Map are attributes
  // Map<String, HashMultimap<String, Map<String, Object>>> : key is variable name, key of HashMultimap is category name
  // and Map are attributes of category
  private void initialiseVariableValueSources() {
    getSources().clear();
    jdbcTemplate = ((LimesurveyDatasource) getDatasource()).getJdbcTemplate();

    HashMultimap<String, Map<String, Object>> mapQuestions = queryQuestions();
    Map<String, HashMultimap<String, Map<String, Object>>> mapQuestionsAnswers = queryAnswer(mapQuestions);
    buildVariables(mapQuestions, mapQuestionsAnswers);
  }

  private HashMultimap<String, Map<String, Object>> queryQuestions() {
    String sqlQuestion = "SELECT * FROM questions WHERE sid=?";
    SqlRowSet questions = jdbcTemplate.queryForRowSet(sqlQuestion.toString(), new Object[] { sid });
    return implodeToMap(questions, "title", new String[] { "question", "language", "qid", "type" });
  }

  private Map<String, HashMultimap<String, Map<String, Object>>> queryAnswer(HashMultimap<String, Map<String, Object>> mapQuestions) {
    Map<String, HashMultimap<String, Map<String, Object>>> mapQuestionsAnswers = Maps.newHashMap();

    String sqlAnswer = "SELECT * FROM answers WHERE qid=?";
    for(String title : mapQuestions.keySet()) {
      // TODO maybe avoid iterator().next()
      Map<String, Object> next = mapQuestions.get(title).iterator().next();
      String qid = asString(next.get("qid"));
      String questionType = asString(next.get("type"));
      HashMultimap<String, Map<String, Object>> mapAnswers = HashMultimap.create();
      if(LimesurveyCategoryGroupType.isExplicitCategory(questionType)) {
        SqlRowSet answers = jdbcTemplate.queryForRowSet(sqlAnswer, new Object[] { qid });
        mapAnswers = implodeToMap(answers, "code", new String[] { "answer", "language", "sortorder", "qid" });
      } else if(LimesurveyCategoryGroupType.isImplicitCategory(questionType)) {
        LimesurveyImplicitType type = LimesurveyImplicitType._valueOf(questionType);
        for(String category : type.getCategories()) {
          mapAnswers.put(category, Maps.<String, Object> newHashMap());
        }
      }
      mapQuestionsAnswers.put(title, mapAnswers);
    }

    return mapQuestionsAnswers;
  }

  private void buildVariables(HashMultimap<String, Map<String, Object>> mapQuestions, Map<String, HashMultimap<String, Map<String, Object>>> mapQuestionsAnswers) {
    for(String questionName : mapQuestions.keySet()) {
      // TODO put Participant elsewhere
      Variable.Builder vb = Variable.Builder.newVariable(questionName, TextType.get(), "Participant");

      for(Map<String, Object> questionAttr : mapQuestions.get(questionName)) {
        String questionLocale = asString(questionAttr.get("language"));
        String questionLabel = asString(questionAttr.get("question"));
        vb.addAttribute("label:" + questionLabel, questionLocale);
      }

      HashMultimap<String, Map<String, Object>> questionsMap = mapQuestionsAnswers.get(questionName);
      if(questionsMap != null) {
        for(String categoryName : questionsMap.keySet()) {
          Category.Builder cb = Category.Builder.newCategory(categoryName);
          for(Map<String, Object> categoryAttr : questionsMap.get(categoryName)) {
            String categoryLocale = asString(categoryAttr.get("language"));
            String categoryLabel = asString(categoryAttr.get("answer"));
            cb.addAttribute("label:" + categoryLocale, categoryLabel);
          }
          vb.addCategory(cb.build());
        }
      }
      LimesurveyVariableValueSource variable = new LimesurveyVariableValueSource(vb.build());
      addVariableValueSource(variable);
    }
  }

  private HashMultimap<String, Map<String, Object>> implodeToMap(SqlRowSet rows, String key, String... cols) {
    HashMultimap<String, Map<String, Object>> map = HashMultimap.create();
    while(rows.next()) {
      Map<String, Object> attr = Maps.newHashMap();
      for(String col : cols) {
        attr.put(col, rows.getString(col));
      }
      map.put(rows.getString(key), attr);
    }
    return map;
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
