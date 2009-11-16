package org.obiba.magma.integration;

import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

import org.obiba.magma.Collection;
import org.obiba.magma.MagmaEngine;
import org.obiba.magma.Occurrence;
import org.obiba.magma.ValueSet;
import org.obiba.magma.VariableEntity;
import org.obiba.magma.VariableValueSource;
import org.obiba.magma.integration.service.XStreamIntegrationServiceFactory;
import org.obiba.magma.js.MagmaJsExtension;

/**
 */
public class IntegrationApp {

  public static void main(String[] args) throws UnsupportedEncodingException {
    new MagmaEngine().extend(new MagmaJsExtension());

    XStreamIntegrationServiceFactory factory = new XStreamIntegrationServiceFactory();
    IntegrationDatasource integrationDatasource = new IntegrationDatasource(factory.buildService(new InputStreamReader(IntegrationApp.class.getResourceAsStream("participants.xml"), "UTF-8")));

    MagmaEngine.get().addDatasource(integrationDatasource);

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
