package org.obiba.meta.integration;

import java.io.InputStreamReader;
import java.util.List;

import org.obiba.meta.Collection;
import org.obiba.meta.MetaEngine;
import org.obiba.meta.ValueSetReference;
import org.obiba.meta.ValueSetReferenceResolver;
import org.obiba.meta.Variable;
import org.obiba.meta.VariableValueSource;
import org.obiba.meta.beans.BeanValueSetReferenceProvider;
import org.obiba.meta.beans.BeanVariableValueSourceFactory;
import org.obiba.meta.integration.model.Participant;
import org.obiba.meta.integration.service.IntegrationService;
import org.obiba.meta.integration.service.XStreamIntegrationServiceFactory;
import org.obiba.meta.js.JavascriptVariableValueSource;
import org.obiba.meta.support.CollectionBuilder;
import org.obiba.meta.support.DatasourceBean;
import org.obiba.meta.type.IntegerType;
import org.obiba.meta.type.TextType;

import com.google.common.collect.ImmutableSet;

/**
 */
public class IntegrationApp {

  public static void main(String[] args) {
    new MetaEngine();

    CollectionBuilder builder = new CollectionBuilder("integration-app");

    XStreamIntegrationServiceFactory factory = new XStreamIntegrationServiceFactory();
    final IntegrationService service = factory.buildService(new InputStreamReader(IntegrationApp.class.getResourceAsStream("participants.xml")));

    builder.add(new BeanValueSetReferenceProvider("Participant", "participant.barcode", "startDate") {
      @Override
      protected List<?> getBeans() {
        return service.getInterviews();
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

    builder.add(new JavascriptVariableValueSource(Variable.Builder.newVariable("integration-app", "fullName", TextType.get(), "Participant").addAttribute("script", "$('firstName') + ' ' + $('lastName')").build()));
    builder.add(new JavascriptVariableValueSource(Variable.Builder.newVariable("integration-app", "interviewYear", IntegerType.get(), "Participant").addAttribute("script", "dateYear($('interview.startDate'))").build()));

    Collection collection = builder.build();

    DatasourceBean d = new DatasourceBean();
    d.addCollection(collection);
    MetaEngine.get().addDatasource(d);

    for(String entityType : collection.getEntityTypes()) {
      for(ValueSetReference reference : collection.getValueSetReferences(entityType)) {
        for(VariableValueSource source : collection.getVariableValueSources(entityType)) {
          System.out.println(source.getVariable().getName() + "[" + source.getValueType().getName() + "]: " + source.getValue(reference));
        }
      }
    }

  }
}
