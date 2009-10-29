package org.obiba.meta.integration;

import java.io.InputStreamReader;
import java.util.List;
import java.util.Set;

import org.obiba.meta.MetaEngine;
import org.obiba.meta.ValueSetReference;
import org.obiba.meta.ValueSetReferenceResolver;
import org.obiba.meta.VariableValueSource;
import org.obiba.meta.beans.BeanValueSetReferenceProvider;
import org.obiba.meta.beans.BeanVariableValueSourceFactory;
import org.obiba.meta.integration.model.Participant;
import org.obiba.meta.integration.service.IntegrationService;
import org.obiba.meta.integration.service.XStreamIntegrationServiceFactory;

import com.google.common.collect.ImmutableSet;

/**
 */
public class IntegrationApp {

  public static void main(String[] args) {
    new MetaEngine();
    XStreamIntegrationServiceFactory factory = new XStreamIntegrationServiceFactory();
    final IntegrationService service = factory.buildService(new InputStreamReader(IntegrationApp.class.getResourceAsStream("participants.xml")));
    BeanValueSetReferenceProvider provider = new BeanValueSetReferenceProvider("Participant", "participant.barcode", "startDate") {
      @Override
      protected List<?> getBeans() {
        return service.getInterviews();
      }
    };

    BeanVariableValueSourceFactory variables = new BeanVariableValueSourceFactory(Participant.class);
    variables.setProperties(ImmutableSet.of("barcode", "firstName", "lastName", "gender", "interview.startDate", "interview.endDate"));
    variables.setResolver(new ValueSetReferenceResolver<Participant>() {

      @Override
      public boolean canResolve(ValueSetReference reference) {
        return true;
      }

      @Override
      public Participant resolve(ValueSetReference reference) {
        return service.getParticipant(reference.getVariableEntity().getIdentifier());
      }

    });

    Set<VariableValueSource> sources = variables.createSources();
    for (VariableValueSource source : sources) {
      for (ValueSetReference valueSetReference : provider.getValueSetReferences()) {
        System.out.println(source.getVariable().getName() + ": " + source.getValue(valueSetReference));
      }
    }
  }
}
