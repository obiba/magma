package org.obiba.meta.integration;

import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

import org.obiba.meta.Collection;
import org.obiba.meta.MetaEngine;
import org.obiba.meta.Occurrence;
import org.obiba.meta.ValueSet;
import org.obiba.meta.VariableEntity;
import org.obiba.meta.VariableValueSource;
import org.obiba.meta.integration.service.XStreamIntegrationServiceFactory;

/**
 */
public class IntegrationApp {

  public static void main(String[] args) throws UnsupportedEncodingException {
    new MetaEngine();
    XStreamIntegrationServiceFactory factory = new XStreamIntegrationServiceFactory();
    IntegrationDatasource integrationDatasource = new IntegrationDatasource(factory.buildService(new InputStreamReader(IntegrationApp.class.getResourceAsStream("participants.xml"), "UTF-8")));

    MetaEngine.get().addDatasource(integrationDatasource);

    for(Collection collection : integrationDatasource.getCollections()) {
      for(String entityType : collection.getEntityTypes()) {

        for(VariableEntity entity : collection.getEntities(entityType)) {

          ValueSet valueSet = collection.loadValueSet(entity);

          for(VariableValueSource source : collection.getVariableValueSources(entityType)) {

            if(source.getVariable().isRepeatable()) {
              for(Occurrence occurrence : collection.loadOccurrences(valueSet, source.getVariable())) {
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

}
