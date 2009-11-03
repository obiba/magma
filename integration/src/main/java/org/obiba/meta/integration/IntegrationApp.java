package org.obiba.meta.integration;

import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Set;

import org.obiba.meta.AbstractOccurrenceReferenceResolver;
import org.obiba.meta.Collection;
import org.obiba.meta.MetaEngine;
import org.obiba.meta.OccurrenceReference;
import org.obiba.meta.OccurrenceReferenceResolver;
import org.obiba.meta.ValueSetReference;
import org.obiba.meta.ValueSetReferenceResolver;
import org.obiba.meta.Variable;
import org.obiba.meta.VariableValueSource;
import org.obiba.meta.beans.BeanValueSetReferenceProvider;
import org.obiba.meta.beans.BeanVariableValueSourceFactory;
import org.obiba.meta.integration.model.Action;
import org.obiba.meta.integration.model.Participant;
import org.obiba.meta.integration.service.IntegrationService;
import org.obiba.meta.integration.service.XStreamIntegrationServiceFactory;
import org.obiba.meta.js.JavascriptVariableBuilder;
import org.obiba.meta.js.JavascriptVariableValueSource;
import org.obiba.meta.support.CollectionBuilder;
import org.obiba.meta.support.DatasourceBean;
import org.obiba.meta.support.OccurrenceReferenceBean;
import org.obiba.meta.type.BooleanType;
import org.obiba.meta.type.IntegerType;
import org.obiba.meta.type.TextType;

import com.google.common.collect.ImmutableSet;

/**
 */
public class IntegrationApp {

  public static void main(String[] args) throws UnsupportedEncodingException {
    new MetaEngine();

    CollectionBuilder builder = new CollectionBuilder("integration-app");

    XStreamIntegrationServiceFactory factory = new XStreamIntegrationServiceFactory();
    final IntegrationService service = factory.buildService(new InputStreamReader(IntegrationApp.class.getResourceAsStream("participants.xml"), "UTF-8"));

    builder.add(new BeanValueSetReferenceProvider("Participant", "participant.barcode", "startDate") {
      @Override
      protected List<?> getBeans() {
        return service.getInterviews();
      }

      @Override
      public Set<OccurrenceReference> getOccurrenceReferences(ValueSetReference reference, Variable variable) {
        Participant participant = service.getParticipant(reference.getVariableEntity().getIdentifier());
        ImmutableSet.Builder<OccurrenceReference> builder = ImmutableSet.builder();
        int order = 0;
        for(Action action : service.getActions(participant)) {
          builder.add(new OccurrenceReferenceBean(reference, variable, order++));
        }
        return builder.build();
      }
    });

    ValueSetReferenceResolver<Participant> participantResolver = new ValueSetReferenceResolver<Participant>() {
      @Override
      public Participant resolve(ValueSetReference reference) {
        return service.getParticipant(reference.getVariableEntity().getIdentifier());
      }
    };

    BeanVariableValueSourceFactory<Participant> variables = new BeanVariableValueSourceFactory<Participant>("Participant", Participant.class, participantResolver);
    variables.setProperties(ImmutableSet.of("barcode", "firstName", "lastName", "gender", "interview.startDate", "interview.endDate"));

    builder.add(variables);

    builder.add(new JavascriptVariableValueSource(Variable.Builder.newVariable("integration-app", "fullName", TextType.get(), "Participant").extend(JavascriptVariableBuilder.class).setScript("$('firstName') + ' ' + $('lastName')").build()));
    builder.add(new JavascriptVariableValueSource(Variable.Builder.newVariable("integration-app", "interviewYear", IntegerType.get(), "Participant").extend(JavascriptVariableBuilder.class).setScript("dateYear($('interview.startDate'))").build()));
    builder.add(new JavascriptVariableValueSource(Variable.Builder.newVariable("integration-app", "isMale", BooleanType.get(), "Participant").extend(JavascriptVariableBuilder.class).setScript("$('gender') == 'Male'").build()));
    builder.add(new JavascriptVariableValueSource(Variable.Builder.newVariable("integration-app", "isFemale", BooleanType.get(), "Participant").extend(JavascriptVariableBuilder.class).setScript("$('gender') == 'Female'").build()));

    OccurrenceReferenceResolver<Action> actionResolver = new AbstractOccurrenceReferenceResolver<Action>() {
      @Override
      public String getOccurrentGroup() {
        return "Action";
      }

      @Override
      protected Action resolveOccurrence(OccurrenceReference occurrence) {
        Participant participant = service.getParticipant(occurrence.getVariableEntity().getIdentifier());
        return service.getActions(participant).get(occurrence.getOrder());
      }
    };

    BeanVariableValueSourceFactory<Action> actionFactory = new BeanVariableValueSourceFactory<Action>("Participant", Action.class, actionResolver);
    actionFactory.setProperties(ImmutableSet.of("stage"));
    actionFactory.setPrefix("Action");
    builder.add(actionFactory);

    Collection collection = builder.build();

    DatasourceBean d = new DatasourceBean();
    d.addCollection(collection);
    MetaEngine.get().addDatasource(d);

    for(String entityType : collection.getEntityTypes()) {
      for(ValueSetReference reference : collection.getValueSetReferences(entityType)) {
        for(VariableValueSource source : collection.getVariableValueSources(entityType)) {
          if(source.getVariable().isRepeatable()) {
            for(OccurrenceReference occurrence : collection.getOccurrenceReferences(reference, source.getVariable())) {
              System.out.println(source.getVariable().getName() + "[" + source.getValueType().getName() + "]@" + occurrence.getOrder() + ": " + source.getValue(occurrence));
            }
          } else {
            System.out.println(source.getVariable().getName() + "[" + source.getValueType().getName() + "]: " + source.getValue(reference));
          }
        }
      }
    }

  }
}
