package org.obiba.meta.integration;

import java.util.Set;

import org.obiba.meta.Collection;
import org.obiba.meta.Occurrence;
import org.obiba.meta.ValueSet;
import org.obiba.meta.Variable;
import org.obiba.meta.beans.AbstractBeanValueSetProvider;
import org.obiba.meta.beans.BeanVariableValueSourceFactory;
import org.obiba.meta.beans.OccurrenceProvider;
import org.obiba.meta.beans.ValueSetBeanResolver;
import org.obiba.meta.integration.model.Action;
import org.obiba.meta.integration.model.Interview;
import org.obiba.meta.integration.model.Participant;
import org.obiba.meta.integration.service.IntegrationService;
import org.obiba.meta.js.JavascriptVariableBuilder;
import org.obiba.meta.js.JavascriptVariableValueSource;
import org.obiba.meta.support.AbstractDatasource;
import org.obiba.meta.support.CollectionBuilder;
import org.obiba.meta.support.OccurrenceBean;
import org.obiba.meta.type.BooleanType;
import org.obiba.meta.type.IntegerType;
import org.obiba.meta.type.TextType;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSet.Builder;

public class IntegrationDatasource extends AbstractDatasource {

  private IntegrationService service;

  IntegrationDatasource(IntegrationService service) {
    this.service = service;
  }

  @Override
  protected Set<String> getCollectionNames() {
    return ImmutableSet.of("integration-app");
  }

  @Override
  protected Collection initialiseCollection(String collection) {

    CollectionBuilder builder = new CollectionBuilder(collection);
    AbstractBeanValueSetProvider<Interview> provider = new AbstractBeanValueSetProvider<Interview>("Participant", "participant.barcode") {

      @Override
      protected Iterable<Interview> loadBeans() {
        return service.getInterviews();
      }
    };
    provider.setOccurrenceProviders(buildOccurrenceProviders(new ImmutableSet.Builder<OccurrenceProvider>()).build());
    builder.add(provider);

    BeanVariableValueSourceFactory<Participant> variables = new BeanVariableValueSourceFactory<Participant>("Participant", Participant.class);
    variables.setProperties(ImmutableSet.of("barcode", "firstName", "lastName", "gender", "interview.startDate", "interview.endDate"));
    builder.add(variables.createSources(collection, new ParticipantBeanResolver()));

    builder.add(new JavascriptVariableValueSource(Variable.Builder.newVariable(collection, "fullName", TextType.get(), "Participant").extend(JavascriptVariableBuilder.class).setScript("$('firstName') + ' ' + $('lastName')").build()));
    builder.add(new JavascriptVariableValueSource(Variable.Builder.newVariable(collection, "interviewYear", IntegerType.get(), "Participant").extend(JavascriptVariableBuilder.class).setScript("dateYear($('interview.startDate'))").build()));
    builder.add(new JavascriptVariableValueSource(Variable.Builder.newVariable(collection, "isMale", BooleanType.get(), "Participant").extend(JavascriptVariableBuilder.class).setScript("$('gender') == 'Male'").build()));
    builder.add(new JavascriptVariableValueSource(Variable.Builder.newVariable(collection, "isFemale", BooleanType.get(), "Participant").extend(JavascriptVariableBuilder.class).setScript("$('gender') == 'Female'").build()));

    BeanVariableValueSourceFactory<Action> actionFactory = new BeanVariableValueSourceFactory<Action>("Participant", Action.class);
    actionFactory.setProperties(ImmutableSet.of("stage"));
    actionFactory.setPrefix("Action");
    actionFactory.setOccurrenceGroup("Action");
    builder.add(actionFactory.createSources(collection, new ActionBeanResolver()));

    return builder.build(this);
  }

  protected Builder<OccurrenceProvider> buildOccurrenceProviders(Builder<OccurrenceProvider> builder) {
    return builder.add(new OccurrenceProvider() {

      @Override
      public boolean occurenceOf(Variable variable) {
        return "Action".equals(variable.getOccurrenceGroup());
      }

      @Override
      public Set<Occurrence> loadOccurrences(ValueSet valueSet, Variable variable) {
        Participant participant = getParticipant(valueSet);
        ImmutableSet.Builder<Occurrence> builder = ImmutableSet.builder();
        int order = 0;
        for(Action action : service.getActions(participant)) {
          builder.add(new OccurrenceBean(valueSet, variable.getOccurrenceGroup(), order++));
        }
        return builder.build();
      }
    });

  }

  protected Participant getParticipant(ValueSet valueSet) {
    return service.getParticipant(valueSet.getVariableEntity().getIdentifier());
  }

  private class ParticipantBeanResolver implements ValueSetBeanResolver {
    @Override
    public Object resolve(Class<?> type, ValueSet valueSet, Variable variable) {
      return getParticipant(valueSet);
    }

    @Override
    public boolean resolves(Class<?> type) {
      return Participant.class.equals(type);
    }
  }

  private class ActionBeanResolver implements ValueSetBeanResolver {
    @Override
    public Object resolve(Class<?> type, ValueSet valueSet, Variable variable) {
      Occurrence occurrence = (Occurrence) valueSet;
      return service.getActions(getParticipant(occurrence)).get(occurrence.getOrder());
    }

    @Override
    public boolean resolves(Class<?> type) {
      return Action.class.equals(type);
    }
  }

}
