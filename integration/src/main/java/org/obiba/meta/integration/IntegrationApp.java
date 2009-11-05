package org.obiba.meta.integration;

import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

import org.obiba.meta.Collection;
import org.obiba.meta.MetaEngine;
import org.obiba.meta.Occurrence;
import org.obiba.meta.ValueSet;
import org.obiba.meta.Variable;
import org.obiba.meta.VariableEntity;
import org.obiba.meta.VariableValueSource;
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

    XStreamIntegrationServiceFactory factory = new XStreamIntegrationServiceFactory();
    final IntegrationService service = factory.buildService(new InputStreamReader(IntegrationApp.class.getResourceAsStream("participants.xml"), "UTF-8"));

    IntegrationDatasource integrationDatasource = new IntegrationDatasource(service);

    CollectionBuilder builder = new CollectionBuilder("integration-app");

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

    Collection collection = builder.build(integrationDatasource);

    integrationDatasource.addCollection(collection);
    MetaEngine.get().addDatasource(integrationDatasource);

    for(String entityType : collection.getEntityTypes()) {

      for(VariableEntity entity : collection.getEntities(entityType)) {

        ValueSet valueSet = collection.loadValueSet(entity);

        for(VariableValueSource source : collection.getVariableValueSources(entityType)) {

          if(source.getVariable().isRepeatable()) {
            for(Occurrence occurrence : valueSet.connect().loadOccurrences(source.getVariable())) {
              System.out.println(source.getVariable().getName() + "[" + source.getValueType().getName() + "]@" + occurrence.getOrder() + ": " + source.getValue(occurrence));
            }

          } else {
            System.out.println(source.getVariable().getName() + "[" + source.getValueType().getName() + "]: " + source.getValue(valueSet));
          }
        }
      }
    }

  }

}
