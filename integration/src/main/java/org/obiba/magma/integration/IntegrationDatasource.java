package org.obiba.magma.integration;

import java.util.Set;

import org.obiba.magma.Collection;
import org.obiba.magma.Occurrence;
import org.obiba.magma.ValueSet;
import org.obiba.magma.Variable;
import org.obiba.magma.beans.AbstractBeanValueSetProvider;
import org.obiba.magma.beans.BeanVariableValueSourceFactory;
import org.obiba.magma.beans.OccurrenceProvider;
import org.obiba.magma.beans.ValueSetBeanResolver;
import org.obiba.magma.integration.model.Action;
import org.obiba.magma.integration.model.Interview;
import org.obiba.magma.integration.model.Participant;
import org.obiba.magma.integration.service.IntegrationService;
import org.obiba.magma.js.JavascriptVariableBuilder;
import org.obiba.magma.js.JavascriptVariableValueSource;
import org.obiba.magma.support.AbstractDatasource;
import org.obiba.magma.support.CollectionBuilder;
import org.obiba.magma.support.OccurrenceBean;
import org.obiba.magma.type.BooleanType;
import org.obiba.magma.type.IntegerType;
import org.obiba.magma.type.TextType;

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
    builder.add(new JavascriptVariableValueSource(Variable.Builder.newVariable(collection, "interviewYear", IntegerType.get(), "Participant").extend(JavascriptVariableBuilder.class).setScript("$('interview.startDate').year()").build()));
    builder.add(new JavascriptVariableValueSource(Variable.Builder.newVariable(collection, "isMale", BooleanType.get(), "Participant").extend(JavascriptVariableBuilder.class).setScript("$('gender').any('Male')").build()));
    builder.add(new JavascriptVariableValueSource(Variable.Builder.newVariable(collection, "isFemale", BooleanType.get(), "Participant").extend(JavascriptVariableBuilder.class).setScript("$('gender').any('Female')").build()));

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
      public boolean providesOccurrencesOf(Variable variable) {
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
