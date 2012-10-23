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
import com.google.common.collect.Sets;
import org.obiba.magma.Attribute;
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
import org.obiba.magma.type.DateTimeType;
import org.obiba.magma.type.IntegerType;
import org.obiba.magma.type.TextType;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;

class LimesurveyValueTable extends AbstractValueTable {

  public static final String PARTICIPANT = "Participant";

  private final Integer sid;

  private Map<Integer, LimeQuestion> mapQuestions;

  private Map<Integer, List<LimeAnswer>> mapAnswers;

  private Map<Integer, LimeAttributes> mapAttributes;

  private Set<String> names;

  private LimesurveyParsingException exception;

  private LimesurveyElementProvider elementProvider;

  LimesurveyValueTable(LimesurveyDatasource datasource, String name, Integer sid) {
    super(datasource, name);
    this.sid = sid;
    elementProvider = new LimesurveyElementProviderJdbc(datasource, sid);
    LimesurveyVariableEntityProvider provider = new LimesurveyVariableEntityProvider(PARTICIPANT, datasource, sid);
    setVariableEntityProvider(provider);
  }

  @Override
  public void initialise() {
    super.initialise();
    names = Sets.newHashSet();
    exception = new LimesurveyParsingException("Limesurvey Root Exception",
        "parentLimeException");
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

  String quoteAndPrefix(String identifier) {
    return getDatasource().quoteAndPrefix(identifier);
  }

  private void initialiseVariableValueSources() {
    getSources().clear();
    mapQuestions = elementProvider.queryQuestions();
    mapAnswers = elementProvider.queryExplicitAnswers();
    buildImplicitAnswers();
    mapAttributes = elementProvider.queryAttributes();
    buildVariables();
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
    buildAdministrativeVariables();
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
          LimesurveyQuestionVariableValueSource variable = new LimesurveyQuestionVariableValueSource(vb, question,
              subQuestionFieldTitle);
          addLimesurveyVariableValueSource(variable);
        }
      }
    }
  }
  
  private void buildAdministrativeVariables() {
    Builder vb = Builder.newVariable("startdate", DateTimeType.get(), PARTICIPANT);
    addLimesurveyVariableValueSource(new LimesurveyVariableValueSource(vb));
    
    vb = Builder.newVariable("submitdate", DateTimeType.get(), PARTICIPANT);
    addLimesurveyVariableValueSource(new LimesurveyVariableValueSource(vb));

    vb = Builder.newVariable("startlanguage", TextType.get(), PARTICIPANT);
    addLimesurveyVariableValueSource(new LimesurveyVariableValueSource(vb));
    
    vb = Builder.newVariable("lastpage", IntegerType.get(), PARTICIPANT);
    addLimesurveyVariableValueSource(new LimesurveyVariableValueSource(vb));
  }

  private void buildFileCountIfnecessary(LimeQuestion question) {
    if(question.getLimesurveyType() == LimesurveyType.FILE_UPLOAD) {
      String name = question.getName() + " [filecount]";
      Variable.Builder fileCountVb = Variable.Builder.newVariable(name, IntegerType.get(), PARTICIPANT);
      LimesurveyQuestionVariableValueSource fileCount = new LimesurveyQuestionVariableValueSource(fileCountVb, question,
          "_filecount");
      addLimesurveyVariableValueSource(fileCount);
    }
  }

  private void buildLabelAttributes(LimeLocalizableEntity localizable, AttributeAwareBuilder<?> builder) {
    applyImplicitLabel(localizable, builder);
    for(Attribute attr : localizable.getMagmaAttributes(localizable instanceof LimeQuestion)) {
      builder.addAttribute(attr);
    }
  }

  private void applyImplicitLabel(LimeLocalizableEntity localizable, AttributeAwareBuilder<?> builder) {
    LimeAttributes lla = localizable.getImplicitLabel().get(localizable.getName());
    if(lla != null) {
      for(Attribute attr : lla.toMagmaAttributes(localizable instanceof LimeQuestion)) {
        builder.addAttribute(attr);
      }
    }
  }

  private boolean buildRanking(LimeQuestion question) {
    if(question.getLimesurveyType() == LimesurveyType.RANKING) {
      List<LimeAnswer> answers = mapAnswers.get(question.getQid());
      for(int nbChoices = 1; nbChoices < answers.size() + 1; nbChoices++) {
        Variable.Builder vb = build(question, question.getName() + " [" + nbChoices + "]");
        LimesurveyQuestionVariableValueSource variable = new LimesurveyQuestionVariableValueSource(vb, question,
            nbChoices + "");
        addLimesurveyVariableValueSource(variable);
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
        LimesurveyQuestionVariableValueSource variable = new LimesurveyQuestionVariableValueSource(vb, question,
            question.getName() + "#" + scale);
        addLimesurveyVariableValueSource(variable);
      }
      return true;
    }
    return false;
  }

  private void addLimesurveyVariableValueSource(VariableValueSource vvs) {
    String name = vvs.getVariable().getName();
    if(names.add(name) == false) {
      exception.addChild(
          new LimesurveyParsingException("'" + getName() + "' contains duplicated variable names: " + name,
              "LimeDuplicateVariableName", getName(), name));
    }
    addVariableValueSource(vvs);
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
    LimeAttributes limeAttributes = mapAttributes.get(question.getQid());
    if(limeAttributes != null) {
      builder.addAttributes(limeAttributes.toMagmaAttributes(true));
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
        LimesurveyQuestionVariableValueSource variable = new LimesurveyQuestionVariableValueSource(subVb, scalableQuestion,
            dualName);
        addLimesurveyVariableValueSource(variable);
      }
    }
    return true;
  }

  private void buildOtherVariableIfNecessary(LimeQuestion question) {
    if(question.isUseOther()) {
      Builder other = build(question, question.getName() + " [other]");
      buildSpecialLabel(question, other, "other");
      addLimesurveyVariableValueSource(new LimesurveyQuestionVariableValueSource(other, question, "other"));
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
    for(Attribute attr : question.getImplicitLabel().get(specialLabel).toMagmaAttributes(true)) {
      builder.addAttribute(attr);
    }
  }

  private void buildCommentVariableIfNecessary(LimeQuestion question, LimeQuestion parentQuestion) {
    if(question.getLimesurveyType().isCommentable() && hasSubQuestions(question) == false) {
      Builder comment = build(question, question.getName() + " [comment]");
      buildSpecialLabel(question, comment, "comment");
      addLimesurveyVariableValueSource(new LimesurveyQuestionVariableValueSource(comment, question, "comment"));
    } else if(parentQuestion != null && parentQuestion.getLimesurveyType().isCommentable()) {
      String hierarchicalVariableName = parentQuestion.getName() + " [" + question.getName() + "comment]";
      Builder comment = build(question, hierarchicalVariableName);
      buildSpecialLabel(question, comment, "comment");
      addLimesurveyVariableValueSource(
          new LimesurveyQuestionVariableValueSource(comment, question, question.getName() + "comment"));
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

    public LimesurveyVariableValueSource(Builder vb) {
      setVariable(vb.build());
    }

    protected void setVariable(Variable variable) {
      this.variable = variable;
    }
    
    @Override
    public ValueType getValueType() {
      return variable.getValueType();
    }

    @Override
    public Value getValue(ValueSet valueSet) {
      LimesurveyValueSet limesurveyValueSet = (LimesurveyValueSet) valueSet;
      return limesurveyValueSet.getValue(getVariable().getValueType(), getLimesurveyVariableField());
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
      return variable.getName();
    }

    @Override
    //TODO move into provider implementation
    public Iterable<Value> getValues(final SortedSet<VariableEntity> entities) {
      LimesurveyValueTable table = LimesurveyValueTable.this;
      final NamedParameterJdbcTemplate jdbcTemplate = new NamedParameterJdbcTemplate(getDatasource().getDataSource());
      final MapSqlParameterSource parameters = new MapSqlParameterSource();
      Iterable<String> ids = extractIdentifiers(entities);
      parameters.addValue("ids", Lists.newArrayList(ids));

      final String limesurveyVariableField = getLimesurveyVariableField();
      final StringBuilder sql = new StringBuilder();
      sql.append("SELECT " + quoteAndPrefix(limesurveyVariableField) + " FROM " + quoteAndPrefix(
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
  
  class LimesurveyQuestionVariableValueSource extends LimesurveyVariableValueSource {

    private LimeQuestion question;
    
    private String subQuestionFieldTitle = "";
    
    public LimesurveyQuestionVariableValueSource(Builder vb, LimeQuestion question, String subQuestionFieldTitle) {
      super(vb);
      this.question = question;
      this.subQuestionFieldTitle = subQuestionFieldTitle;
      vb.addAttribute(Attribute.Builder.newAttribute("SGQA").withNamespace(LimeAttributes.LIMESURVEY_NAMESPACE).withValue(getLimesurveyVariableField()).build());
      vb.addAttribute(Attribute.Builder.newAttribute("SGQ").withNamespace(LimeAttributes.LIMESURVEY_NAMESPACE).withValue(getSgqId()).build());
      setVariable(vb.build());
    }
    
    // SGQA identifier
    // see http://docs.limesurvey.org/tiki-index.php?page=SGQA+identifier&structure=English+Instructions+for+LimeSurvey
    public String getLimesurveyVariableField() {
      int qId = question.hasParentId() ? question.getParentQid() : question.getQid();
      return sid + "X" + question.getGroupId() + "X" + qId + subQuestionFieldTitle;
    }
    
    // SGQ
    public String getSgqId() {
      int qId = question.hasParentId() ? question.getParentQid() : question.getQid();
      return sid + "X" + question.getGroupId() + "X" + qId;
    }
    
  }
}
