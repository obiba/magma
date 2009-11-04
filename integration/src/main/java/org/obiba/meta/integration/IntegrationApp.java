package org.obiba.meta.integration;

import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.Set;

import org.obiba.meta.Collection;
import org.obiba.meta.MetaEngine;
import org.obiba.meta.Occurrence;
import org.obiba.meta.ValueSet;
import org.obiba.meta.ValueSetExtension;
import org.obiba.meta.Variable;
import org.obiba.meta.VariableEntity;
import org.obiba.meta.VariableValueSource;
import org.obiba.meta.beans.BeanValueSet;
import org.obiba.meta.beans.BeanValueSetProvider;
import org.obiba.meta.beans.BeanVariableValueSourceFactory;
import org.obiba.meta.integration.model.Action;
import org.obiba.meta.integration.model.Interview;
import org.obiba.meta.integration.model.Participant;
import org.obiba.meta.integration.service.IntegrationService;
import org.obiba.meta.integration.service.XStreamIntegrationServiceFactory;
import org.obiba.meta.js.JavascriptVariableBuilder;
import org.obiba.meta.js.JavascriptVariableValueSource;
import org.obiba.meta.support.CollectionBuilder;
import org.obiba.meta.support.DatasourceBean;
import org.obiba.meta.support.OccurrenceBean;
import org.obiba.meta.type.BooleanType;
import org.obiba.meta.type.IntegerType;
import org.obiba.meta.type.TextType;

import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;

/**
 */
public class IntegrationApp {

  public static void main(String[] args) throws UnsupportedEncodingException {
    new MetaEngine();

    CollectionBuilder builder = new CollectionBuilder("integration-app");

    XStreamIntegrationServiceFactory factory = new XStreamIntegrationServiceFactory();
    final IntegrationService service = factory.buildService(new InputStreamReader(IntegrationApp.class.getResourceAsStream("participants.xml"), "UTF-8"));

    builder.add(new BeanValueSetProvider<Interview>("Participant", "participant.barcode") {

      @Override
      protected Interview loadBean(final VariableEntity entity) {
        return Iterables.find(loadBeans(), new Predicate<Interview>() {
          @Override
          public boolean apply(Interview input) {
            return input.getParticipant().getBarcode().equals(entity.getIdentifier());
          }
        });
      }

      @Override
      protected Iterable<Interview> loadBeans() {
        return service.getInterviews();
      }

    });

    builder.add(Participant.class.getName(), new ValueSetExtension<BeanValueSet<Interview>, Participant>() {
      @Override
      public Participant extend(BeanValueSet<Interview> valueSet) {
        return valueSet.getBean().getParticipant();
      }
    });

    builder.add("Action", new ValueSetExtension<BeanValueSet<Interview>, Set<Occurrence>>() {

      @Override
      public Set<Occurrence> extend(BeanValueSet<Interview> valueSet) {
        Participant participant = valueSet.getBean().getParticipant();
        ImmutableSet.Builder<Occurrence> builder = ImmutableSet.builder();
        int order = 0;
        for(Action action : service.getActions(participant)) {
          builder.add(new OccurrenceBean(valueSet, "Action", order++));
        }
        return builder.build();
      }
    });

    builder.add(Action.class.getName(), new ValueSetExtension<Occurrence, Action>() {
      @Override
      public Action extend(Occurrence valueSet) {
        BeanValueSet<Interview> parent = (BeanValueSet<Interview>) valueSet.getParent();
        Participant participant = parent.getBean().getParticipant();
        return Iterables.get(service.getActions(participant), valueSet.getOrder());
      }
    });

    BeanVariableValueSourceFactory<Participant> variables = new BeanVariableValueSourceFactory<Participant>("Participant", Participant.class);
    variables.setProperties(ImmutableSet.of("barcode", "firstName", "lastName", "gender", "interview.startDate", "interview.endDate"));

    builder.add(variables);

    builder.add(new JavascriptVariableValueSource(Variable.Builder.newVariable("integration-app", "fullName", TextType.get(), "Participant").extend(JavascriptVariableBuilder.class).setScript("$('firstName') + ' ' + $('lastName')").build()));
    builder.add(new JavascriptVariableValueSource(Variable.Builder.newVariable("integration-app", "interviewYear", IntegerType.get(), "Participant").extend(JavascriptVariableBuilder.class).setScript("dateYear($('interview.startDate'))").build()));
    builder.add(new JavascriptVariableValueSource(Variable.Builder.newVariable("integration-app", "isMale", BooleanType.get(), "Participant").extend(JavascriptVariableBuilder.class).setScript("$('gender') == 'Male'").build()));
    builder.add(new JavascriptVariableValueSource(Variable.Builder.newVariable("integration-app", "isFemale", BooleanType.get(), "Participant").extend(JavascriptVariableBuilder.class).setScript("$('gender') == 'Female'").build()));
    /*
     * OccurrenceReferenceResolver<Action> actionResolver = new AbstractOccurrenceReferenceResolver<Action>() {
     * 
     * @Override public String getOccurrentGroup() { return "Action"; }
     * 
     * @Override protected Action resolveOccurrence(OccurrenceReference occurrence) { Participant participant =
     * service.getParticipant(occurrence.getVariableEntity().getIdentifier()); return
     * service.getActions(participant).get(occurrence.getOrder()); } };
     */
    BeanVariableValueSourceFactory<Action> actionFactory = new BeanVariableValueSourceFactory<Action>("Participant", Action.class);
    actionFactory.setProperties(ImmutableSet.of("stage"));
    actionFactory.setPrefix("Action");
    actionFactory.setOccurrenceGroup("Action");
    builder.add(actionFactory);

    Collection collection = builder.build();

    DatasourceBean d = new DatasourceBean();
    d.addCollection(collection);
    MetaEngine.get().addDatasource(d);

    for(String entityType : collection.getEntityTypes()) {
      for(VariableEntity entity : collection.getEntities(entityType)) {
        ValueSet valueSet = collection.loadValueSet(entity);
        for(VariableValueSource source : collection.getVariableValueSources(entityType)) {
          if(source.getVariable().isRepeatable()) {
            for(Occurrence occurrence : (Set<Occurrence>) valueSet.extend(source.getVariable().getOccurrenceGroup())) {
              System.out.println(source.getVariable().getName() + "[" + source.getValueType().getName() + "]@" + occurrence.getOrder() + ": " + source.getValue(occurrence));
            }

          } else {
            System.out.println(source.getVariable().getName() + "[" + source.getValueType().getName() + "]: " + source.getValue(valueSet));
          }
        }
      }
    }

  }

  public class IntegrationAppValueSet implements ValueSet {

    private VariableEntity entity;

    private Interview interview;

    IntegrationAppValueSet(VariableEntity entity, Interview interview) {
      this.entity = entity;
      this.interview = interview;
    }

    @Override
    public VariableEntity getVariableEntity() {
      return entity;
    }

    @Override
    public Date getEndDate() {
      return null;
    }

    @Override
    public Date getStartDate() {
      return null;
    }

    @Override
    public Collection getCollection() {
      return null;
    }

    @Override
    public <T> T extend(String extensionName) {
      return ((ValueSetExtension<IntegrationAppValueSet, T>) getCollection().getExtension(extensionName)).extend(this);
    }

    public Interview getInterview() {
      return interview;
    }

  }
}
