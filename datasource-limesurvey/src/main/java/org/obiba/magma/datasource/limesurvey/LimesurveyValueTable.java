package org.obiba.magma.datasource.limesurvey;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.SortedSet;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.obiba.magma.AttributeAwareBuilder;
import org.obiba.magma.Category;
import org.obiba.magma.NoSuchValueSetException;
import org.obiba.magma.Timestamps;
import org.obiba.magma.Value;
import org.obiba.magma.ValueSet;
import org.obiba.magma.ValueType;
import org.obiba.magma.Variable;
import org.obiba.magma.Variable.Builder;
import org.obiba.magma.VariableEntity;
import org.obiba.magma.VariableValueSource;
import org.obiba.magma.VectorSource;
import org.obiba.magma.support.AbstractValueTable;
import org.obiba.magma.support.VariableEntityProvider;
import org.obiba.magma.type.IntegerType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;

class LimesurveyValueTable extends AbstractValueTable {

  public static final String PARTICIPANT = "Participant";

  private JdbcTemplate jdbcTemplate;

  private final Integer sid;

  private final String tablePrefix;

  private Map<Integer, LimeQuestion> mapQuestions = Maps.newHashMap();

  private Map<Integer, List<LimeAnswer>> mapAnswers = Maps.newHashMap();

  private Map<Integer, LimeAttributes> attributes = Maps.newHashMap();

  private Set<String> names = Sets.newHashSet();

  private LimesurveyParsingException exception = new LimesurveyParsingException("parent (useless)",
      "parentLimeException");

  LimesurveyValueTable(LimesurveyDatasource datasource, String name, Integer sid, String tablePrefix) {
    super(datasource, name);
    this.sid = sid;
    this.tablePrefix = tablePrefix;

    LimesurveyVariableEntityProvider provider = new LimesurveyVariableEntityProvider(PARTICIPANT, datasource, sid);
    provider.setTablePrefix(tablePrefix);
    setVariableEntityProvider(provider);
  }

  public LimesurveyValueTable(LimesurveyDatasource datasource, String name, Integer sid, String tablePrefix,
      VariableEntityProvider variableEntityProvider) {
    this(datasource, name, sid, tablePrefix);
    setVariableEntityProvider(variableEntityProvider);
  }

  @Override
  public void initialise() {
    super.initialise();
    initialiseVariableValueSources();
    getVariableEntityProvider().initialise();
    if(exception.getChildren().isEmpty() == false) {
      throw exception;
    }
  }

  @Override
  protected LimesurveyVariableEntityProvider getVariableEntityProvider() {
    return (LimesurveyVariableEntityProvider) super.getVariableEntityProvider();
  }

  String quoteIdentifier(String identifier) {
    return getDatasource().quoteIdentifier(identifier);
  }

  private void initialiseVariableValueSources() {
    getSources().clear();
    jdbcTemplate = new JdbcTemplate(getDatasource().getDataSource());
    queryQuestions();
    queryExplicitAnswers();
    buildImplicitAnswers();
    queryAttributes();
    buildVariables();
  }

  private void queryAttributes() {
    StringBuilder sqlAttr = new StringBuilder();
    sqlAttr.append("SELECT qid, attribute, value ");
    sqlAttr.append("FROM question_attributes ");
    SqlRowSet sqlRowSet = jdbcTemplate.queryForRowSet(sqlAttr.toString());
    while(sqlRowSet.next()) {
      int qid = sqlRowSet.getInt("qid");
      String key = sqlRowSet.getString("attribute");
      String value = sqlRowSet.getString("value");
      if(attributes.containsKey(qid)) {
        attributes.get(qid).attribute(key, value);
      } else {
        attributes.put(qid, LimeAttributes.create().attribute(key, value));
      }
    }

  }

  private void queryQuestions() {
    StringBuilder sqlQuestion = new StringBuilder();
    sqlQuestion.append("SELECT * FROM " + quoteIdentifier(tablePrefix + "questions") + " q JOIN " + quoteIdentifier(
        tablePrefix + "groups") + " g ");
    sqlQuestion.append("ON (q.gid=g.gid AND q.language=g.language) ");
    sqlQuestion.append("WHERE q.sid=? AND q.type!='X' "); // X are boilerplate questions
    sqlQuestion.append("ORDER BY group_order, question_order ASC ");
    SqlRowSet questionsRowSet = jdbcTemplate.queryForRowSet(sqlQuestion.toString(), sid);

    toQuestions(questionsRowSet);
  }

  private Map<Integer, LimeQuestion> toQuestions(SqlRowSet rows) {
    while(rows.next()) {
      int qid = rows.getInt("qid");
      String language = rows.getString("language");
      if(mapQuestions.containsKey(qid)) {
        LimeQuestion question = mapQuestions.get(qid);
        question.addLocalizableAttribute(language, rows.getString("question"));
      } else {
        LimeQuestion question = LimeQuestion.create();
        question.setName(rows.getString("title"));
        question.setQid(qid);
        question.setGroupId(rows.getInt("gid"));
        question.setParentQid(rows.getInt("parent_qid"));
        question.setType(LimesurveyType._valueOf(rows.getString("type")));
        question.addLocalizableAttribute(language, rows.getString("question"));
        question.setUseOther("Y".equals(rows.getString("other")));
        question.setScaleId(rows.getInt("scale_id"));
        mapQuestions.put(qid, question);
      }
    }
    return mapQuestions;
  }

  private void queryExplicitAnswers() {
    String sqlAnswer = "SELECT * FROM " + quoteIdentifier(tablePrefix + "answers") + " WHERE qid=? ORDER BY sortorder";
    for(LimeQuestion question : mapQuestions.values()) {
      SqlRowSet answersRowset = jdbcTemplate.queryForRowSet(sqlAnswer, question.getQid());
      List<LimeAnswer> answersList = toAnswers(question, answersRowset);
      mapAnswers.put(question.getQid(), answersList);
    }
  }

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
        answer.addLocalizableAttribute(language, label);
      } else {
        LimeAnswer answer = LimeAnswer.create(answerName);
        answer.setSortorder(rows.getInt("sortorder"));
        answer.setScaleId(rows.getInt("scale_id"));
        answer.addLocalizableAttribute(language, label);
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

  private void buildImplicitAnswers() {
    for(Integer qid : mapQuestions.keySet()) {
      LimeQuestion question = mapQuestions.get(qid);
      LimesurveyType type = question.getLimesurveyType();
      List<LimeAnswer> answers = Lists.newArrayList();
      if(type.hasImplicitCategories()) {
        for(String implicitAnswer : type.getImplicitAnswers()) {
          LimeAnswer answer = LimeAnswer.create(implicitAnswer);
          answers.add(answer);
        }
        mapAnswers.put(qid, answers);
      }
    }
  }

  private void buildVariables() {
    for(LimeQuestion question : mapQuestions.values()) {
      if(buildRanking(question) == false) {
        LimeQuestion parentQuestion = null;
        boolean isDualScale = false;
        boolean isArraySubQuestion = false;
        // here are managed special case
        if(question.hasParentId()) {
          parentQuestion = getParentQuestion(question);
          isArraySubQuestion = buildArraySubQuestions(question, parentQuestion);
          isDualScale = buildArrayDualScale(question, parentQuestion);
        }
        Variable.Builder vb = null;
        // if not a special case
        if(isArraySubQuestion == false && isDualScale == false) {
          vb = buildVariable(question);
        }
        buildFileCountIfnecessary(question);
        buildOtherVariableIfNecessary(question);
        buildCommentVariableIfNecessary(question, parentQuestion);

        // we stop if we already built special variables cases
        if(vb != null) {
          buildLabelAttributes(question, vb);
          if(question.hasParentId()) {
            buildCategoriesForVariable(question, vb, mapAnswers.get(parentQuestion.getQid()));
          } else if(hasSubQuestions(question) == false) {
            buildCategoriesForVariable(question, vb, mapAnswers.get(question.getQid()));
          }
          String subQuestionFieldTitle = question.hasParentId() ? question.getName() : "";
          LimesurveyVariableValueSource variable = new LimesurveyVariableValueSource(vb.build(), question,
              subQuestionFieldTitle);
          createLimesurveyVariableValueSource(variable);
        }
      }
    }
  }

  private void buildFileCountIfnecessary(LimeQuestion question) {
    if(question.getLimesurveyType() == LimesurveyType.FILE_UPLOAD) {
      String name = question.getName() + " [filecount]";
      Variable.Builder fileCountVb = Variable.Builder.newVariable(name, IntegerType.get(), PARTICIPANT);
      LimesurveyVariableValueSource fileCount = new LimesurveyVariableValueSource(fileCountVb.build(), question,
          "_filecount");
      createLimesurveyVariableValueSource(fileCount);
    }
  }

  private void buildLabelAttributes(LimeLocalizableEntity localizable, AttributeAwareBuilder<?> builder) {
    applyImplicitLabel(localizable, builder);
    for(Map.Entry<String, String> attr : localizable.getLocalizableAttributes().entrySet()) {
      builder.addAttribute(attr.getKey(), attr.getValue());
    }
  }

  private void applyImplicitLabel(LimeLocalizableEntity localizable, AttributeAwareBuilder<?> builder) {
    LimeAttributes lla = localizable.getImplicitLabel().get(localizable.getName());
    if(lla != null) {
      for(Map.Entry<String, String> attrs : lla.getAttributes().entrySet()) {
        builder.addAttribute(attrs.getKey(), attrs.getValue());
      }
    }
  }

  private boolean buildRanking(LimeQuestion question) {
    if(question.getLimesurveyType() == LimesurveyType.RANKING) {
      List<LimeAnswer> answers = mapAnswers.get(question.getQid());
      for(int nbChoices = 1; nbChoices < answers.size() + 1; nbChoices++) {
        Variable.Builder vb = build(question, question.getName() + " [" + nbChoices + "]");
        LimesurveyVariableValueSource variable = new LimesurveyVariableValueSource(vb.build(), question,
            nbChoices + "");
        createLimesurveyVariableValueSource(variable);
      }
      return true;
    }
    return false;
  }

  private boolean buildArrayDualScale(LimeQuestion question, LimeQuestion parentQuestion) {
    if(parentQuestion.getLimesurveyType() == LimesurveyType.ARRAY_DUAL_SCALE) {
      for(int scale = 0; scale < 2; scale++) {
        String hierarchicalVariableName = parentQuestion.getName() + " [" + question.getName() + "][" + scale + "]";
        Variable.Builder vb = build(question, hierarchicalVariableName);
        buildLabelAttributes(question, vb);
        List<LimeAnswer> answers = mapAnswers.get(parentQuestion.getQid());
        for(LimeAnswer answer : answers) {
          if(scale == answer.getScaleId()) {
            Category.Builder cb = Category.Builder.newCategory(answer.getName());
            buildLabelAttributes(answer, cb);
            vb.addCategory(cb.build());
          }
        }
        LimesurveyVariableValueSource variable = new LimesurveyVariableValueSource(vb.build(), question,
            question.getName() + "#" + scale);
        createLimesurveyVariableValueSource(variable);
      }
      return true;
    }
    return false;
  }

  private void createLimesurveyVariableValueSource(LimesurveyVariableValueSource lvvs) {
    String name = lvvs.getVariable().getName();
    if(names.add(name) == false) {
      exception.addChild(new LimesurveyParsingException("Duplicate name", "duplicateVariableName",name));
    }
    addVariableValueSource(lvvs);
  }

  private Variable.Builder buildVariable(LimeQuestion question) {
    Variable.Builder vb;
    // do not create variable for parent question
    if(hasSubQuestions(question) == false) {
      String variableName = question.getName();
      if(question.hasParentId()) {
        LimeQuestion parentQuestion = getParentQuestion(question);
        String hierarchicalVariableName = parentQuestion.getName() + " [" + variableName + "]";
        vb = build(parentQuestion, hierarchicalVariableName);
      } else {
        vb = build(question, variableName);
      }
      return vb;
    }
    // question has subquestion then return null
    return null;
  }

  private Builder build(LimeQuestion question, String variableName) {
    Builder builder = Builder.newVariable(variableName, question.getLimesurveyType().getType(), PARTICIPANT);
    LimeAttributes limeAttributes = attributes.get(question.getQid());
    if(limeAttributes != null) {
      builder.addAttributes(limeAttributes.toMagmaAttributes());
    }
    return builder;
  }

  private boolean buildArraySubQuestions(LimeQuestion question, LimeQuestion parentQuestion) {
    List<LimeQuestion> scalableSubQuestions = getScaledOneSubQuestions(parentQuestion);
    if(scalableSubQuestions.isEmpty()) return false;
    if(question.isScaleEqual1() == false) {
      for(LimeQuestion scalableQuestion : scalableSubQuestions) {
        String dualName = question.getName() + "_" + scalableQuestion.getName();
        String arrayVariableName = parentQuestion.getName() + " [" + dualName + "]";
        Variable.Builder subVb = build(parentQuestion, arrayVariableName);
        buildLabelAttributes(scalableQuestion, subVb);
        LimesurveyVariableValueSource variable = new LimesurveyVariableValueSource(subVb.build(), scalableQuestion,
            dualName);
        createLimesurveyVariableValueSource(variable);
      }
    }
    return true;
  }

  private void buildOtherVariableIfNecessary(LimeQuestion question) {
    if(question.isUseOther()) {
      Builder other = build(question, question.getName() + " [other]");
      buildSpecialLabel(question, other, "other");
      createLimesurveyVariableValueSource(new LimesurveyVariableValueSource(other.build(), question, "other"));
    }
  }

  /**
   * Special label are "other" or "comment"
   *
   * @param question
   * @param builder
   * @param specialLabel
   */
  private void buildSpecialLabel(LimeQuestion question, Builder builder, String specialLabel) {
    for(Map.Entry<String, String> attrs : question.getImplicitLabel().get(specialLabel).getAttributes().entrySet()) {
      builder.addAttribute(attrs.getKey(), attrs.getValue());
    }
  }

  private void buildCommentVariableIfNecessary(LimeQuestion question, LimeQuestion parentQuestion) {
    if(question.getLimesurveyType().isCommentable() && hasSubQuestions(question) == false) {
      Builder comment = build(question, question.getName() + " [comment]");
      buildSpecialLabel(question, comment, "comment");
      createLimesurveyVariableValueSource(new LimesurveyVariableValueSource(comment.build(), question, "comment"));
    } else if(parentQuestion != null && parentQuestion.getLimesurveyType().isCommentable()) {
      String hierarchicalVariableName = parentQuestion.getName() + " [" + question.getName() + "comment]";
      Builder comment = build(question, hierarchicalVariableName);
      buildSpecialLabel(question, comment, "comment");
      createLimesurveyVariableValueSource(
          new LimesurveyVariableValueSource(comment.build(), question, question.getName() + "comment"));
    }
  }

  private void buildCategoriesForVariable(LimeQuestion question, Variable.Builder vb, List<LimeAnswer> limeAnswers) {
    for(LimeAnswer answer : limeAnswers) {
      Category.Builder cb = Category.Builder.newCategory(answer.getName());
      buildLabelAttributes(answer, cb);
      vb.addCategory(cb.build());
    }
  }

  private LimeQuestion getParentQuestion(LimeQuestion limeQuestion) {
    if(limeQuestion.hasParentId()) {
      return mapQuestions.get(limeQuestion.getParentQid());
    }
    return null;
  }

  private boolean hasSubQuestions(final LimeQuestion limeQuestion) {
    return Iterables.any(mapQuestions.values(), new Predicate<LimeQuestion>() {
      @Override
      public boolean apply(LimeQuestion question) {
        return question.getParentQid() == limeQuestion.getQid();
      }
    });
  }

  private List<LimeQuestion> getScaledOneSubQuestions(final LimeQuestion limeQuestion) {
    return Lists.newArrayList(Iterables.filter(mapQuestions.values(), new Predicate<LimeQuestion>() {
      @Override
      public boolean apply(LimeQuestion question) {
        return question.getParentQid() == limeQuestion.getQid() && question.isScaleEqual1();
      }
    }));
  }

  @Override
  public LimesurveyDatasource getDatasource() {
    return (LimesurveyDatasource) super.getDatasource();
  }

  public Integer getSid() {
    return sid;
  }

  public String getTablePrefix() {
    return tablePrefix;
  }

  @Override
  public ValueSet getValueSet(VariableEntity entity) throws NoSuchValueSetException {
    return new LimesurveyValueSet(this, entity);
  }

  @Override
  public Timestamps getTimestamps() {
    return new LimesurveyTimestamps(this);
  }

  class LimesurveyVariableValueSource implements VariableValueSource, VectorSource {

    private Variable variable;

    private LimeQuestion question;

    private String subQuestionFieldTitle = "";

    public LimesurveyVariableValueSource(Variable variable, LimeQuestion question, String subQuestionFieldTitle) {
      this.variable = variable;
      this.question = question;
      this.subQuestionFieldTitle = subQuestionFieldTitle;
    }

    @Override
    public ValueType getValueType() {
      return variable.getValueType();
    }

    @Override
    public Value getValue(ValueSet valueSet) {
      LimesurveyValueSet limesurveyValueSet = (LimesurveyValueSet) valueSet;
      return limesurveyValueSet.getValue(this);
    }

    @Override
    public VectorSource asVectorSource() {
      return this;
    }

    @Override
    public Variable getVariable() {
      return variable;
    }

    public String getLimesurveyVariableField() {
      int qId = question.hasParentId() ? question.getParentQid() : question.getQid();
      return sid + "X" + question.getGroupId() + "X" + qId + subQuestionFieldTitle;
    }

    @Override
    public Iterable<Value> getValues(final SortedSet<VariableEntity> entities) {
      LimesurveyValueTable table = LimesurveyValueTable.this;
      final NamedParameterJdbcTemplate jdbcTemplate = new NamedParameterJdbcTemplate(getDatasource().getDataSource());
      final MapSqlParameterSource parameters = new MapSqlParameterSource();
      Iterable<String> ids = extractIdentifiers(entities);
      parameters.addValue("ids", Lists.newArrayList(ids));

      final String limesurveyVariableField = getLimesurveyVariableField();
      final StringBuilder sql = new StringBuilder();
      sql.append("SELECT " + quoteIdentifier(limesurveyVariableField) + " FROM " + quoteIdentifier(
          "survey_" + table.getSid()) + " ");
      sql.append("WHERE token IN (:ids) ");
      sql.append("ORDER BY token");

      return new Iterable<Value>() {

        @Override
        public Iterator<Value> iterator() {
          return new Iterator<Value>() {

            private Iterator<VariableEntity> idsIterator;

            private SqlRowSet rows;

            {
              idsIterator = entities.iterator();
              if(Iterables.isEmpty(entities) == false) {
                rows = jdbcTemplate.queryForRowSet(sql.toString(), parameters);
              }
            }

            @Override
            public boolean hasNext() {
              return idsIterator.hasNext();
            }

            @Override
            public Value next() {
              if(hasNext() == false) {
                throw new NoSuchElementException();
              }
              idsIterator.next();
              rows.next();
              Object object = rows.getObject(limesurveyVariableField);
              return variable.getValueType().valueOf("".equals(object) ? null : object);
            }

            @Override
            public void remove() {
              throw new UnsupportedOperationException();
            }
          };
        }
      };
    }

    private Iterable<String> extractIdentifiers(SortedSet<VariableEntity> entities) {
      return Iterables.transform(entities, new Function<VariableEntity, String>() {

        @Override
        public String apply(VariableEntity input) {
          return input.getIdentifier();
        }
      });
    }

  }
}
