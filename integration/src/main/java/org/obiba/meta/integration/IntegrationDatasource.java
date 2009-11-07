package org.obiba.meta.integration;

import java.util.Set;

import org.obiba.meta.Collection;
import org.obiba.meta.NoSuchValueSetException;
import org.obiba.meta.Occurrence;
import org.obiba.meta.ValueSet;
import org.obiba.meta.Variable;
import org.obiba.meta.VariableEntity;
import org.obiba.meta.beans.AbstractBeansDatasource;
import org.obiba.meta.beans.BeanValueSetProvider;
import org.obiba.meta.beans.BeanVariableValueSourceFactory;
import org.obiba.meta.beans.DefaultBeansValueSet;
import org.obiba.meta.beans.ValueSetBeanResolver;
import org.obiba.meta.integration.model.Action;
import org.obiba.meta.integration.model.Interview;
import org.obiba.meta.integration.model.Participant;
import org.obiba.meta.integration.service.IntegrationService;
import org.obiba.meta.js.JavascriptVariableBuilder;
import org.obiba.meta.js.JavascriptVariableValueSource;
import org.obiba.meta.support.CollectionBuilder;
import org.obiba.meta.support.OccurrenceBean;
import org.obiba.meta.support.ValueSetBean;
import org.obiba.meta.type.BooleanType;
import org.obiba.meta.type.IntegerType;
import org.obiba.meta.type.TextType;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSet.Builder;

public class IntegrationDatasource extends AbstractBeansDatasource {

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
    builder.add(new BeanValueSetProvider<Interview>("Participant", "participant.barcode") {

      @Override
      protected Iterable<Interview> loadBeans() {
        return service.getInterviews();
      }

      @Override
      public <T> T adapt(Class<T> type, ValueSet valueSet) {
        return (T) new DefaultBeansValueSet(valueSet, getBeanResolvers());
      }

      @Override
      public ValueSet getValueSet(VariableEntity entity) {
        return new ValueSetBean(this, null, entity, null, null);
      }

      @Override
      public Set<Occurrence> loadOccurrences(ValueSet valueSet, Variable variable) {
        if(variable.getOccurrenceGroup().equals("Action")) {
          Participant participant = getParticipant(valueSet);
          ImmutableSet.Builder<Occurrence> builder = ImmutableSet.builder();
          int order = 0;
          for(Action action : service.getActions(participant)) {
            builder.add(new OccurrenceBean(this, valueSet, variable.getOccurrenceGroup(), order++));
          }
          return builder.build();
        }
        throw new NoSuchValueSetException(valueSet.getVariableEntity());
      }

    });

    BeanVariableValueSourceFactory<Participant> variables = new BeanVariableValueSourceFactory<Participant>("Participant", Participant.class);
    variables.setProperties(ImmutableSet.of("barcode", "firstName", "lastName", "gender", "interview.startDate", "interview.endDate"));
    builder.add(variables);

    builder.add(new JavascriptVariableValueSource(Variable.Builder.newVariable(collection, "fullName", TextType.get(), "Participant").extend(JavascriptVariableBuilder.class).setScript("$('firstName') + ' ' + $('lastName')").build()));
    builder.add(new JavascriptVariableValueSource(Variable.Builder.newVariable(collection, "interviewYear", IntegerType.get(), "Participant").extend(JavascriptVariableBuilder.class).setScript("dateYear($('interview.startDate'))").build()));
    builder.add(new JavascriptVariableValueSource(Variable.Builder.newVariable(collection, "isMale", BooleanType.get(), "Participant").extend(JavascriptVariableBuilder.class).setScript("$('gender') == 'Male'").build()));
    builder.add(new JavascriptVariableValueSource(Variable.Builder.newVariable(collection, "isFemale", BooleanType.get(), "Participant").extend(JavascriptVariableBuilder.class).setScript("$('gender') == 'Female'").build()));

    BeanVariableValueSourceFactory<Action> actionFactory = new BeanVariableValueSourceFactory<Action>("Participant", Action.class);
    actionFactory.setProperties(ImmutableSet.of("stage"));
    actionFactory.setPrefix("Action");
    actionFactory.setOccurrenceGroup("Action");
    builder.add(actionFactory);

    return builder.build(this);
  }

  @Override
  protected Builder<ValueSetBeanResolver> buildResolvers(Builder<ValueSetBeanResolver> builder) {
    return builder.add(new ValueSetBeanResolver() {
      @Override
      public Object resolveBean(ValueSet valueSet, Class<?> type, Variable variable) {
        if(Participant.class.equals(type)) {
          return getParticipant(valueSet);
        }
        return null;
      }
    }).add(new ValueSetBeanResolver() {
      @Override
      public Object resolveBean(ValueSet valueSet, Class<?> type, Variable variable) {
        if(Action.class.equals(type)) {
          Occurrence occurrence = (Occurrence) valueSet;
          return service.getActions(getParticipant(occurrence)).get(occurrence.getOrder());
        }
        return null;
      }
    });
  }

  protected Participant getParticipant(ValueSet valueSet) {
    return service.getParticipant(valueSet.getVariableEntity().getIdentifier());
  }

}
