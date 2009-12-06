package org.obiba.magma.integration;

import java.io.IOException;
import java.io.InputStreamReader;
import java.security.NoSuchAlgorithmException;

import org.obiba.magma.MagmaEngine;
import org.obiba.magma.Value;
import org.obiba.magma.ValueSequence;
import org.obiba.magma.ValueSet;
import org.obiba.magma.ValueTable;
import org.obiba.magma.Variable;
import org.obiba.magma.datasource.fs.DatasourceCopier;
import org.obiba.magma.datasource.fs.FsDatasource;
import org.obiba.magma.datasource.fs.output.DigestOutputStreamWrapper;
import org.obiba.magma.integration.service.XStreamIntegrationServiceFactory;
import org.obiba.magma.js.MagmaJsExtension;
import org.obiba.magma.xstream.MagmaXStreamExtension;

/**
 */
public class IntegrationApp {

  public static void main(String[] args) throws IOException, NoSuchAlgorithmException {
    new MagmaEngine().extend(new MagmaJsExtension()).extend(new MagmaXStreamExtension());

    XStreamIntegrationServiceFactory factory = new XStreamIntegrationServiceFactory();
    IntegrationDatasource integrationDatasource = new IntegrationDatasource(factory.buildService(new InputStreamReader(IntegrationApp.class.getResourceAsStream("participants.xml"), "UTF-8")));

    MagmaEngine.get().addDatasource(integrationDatasource);

    FsDatasource fs = new FsDatasource("export", "target/output.zip", new DigestOutputStreamWrapper());
    MagmaEngine.get().addDatasource(fs);

    // Export the IntegrationDatasource to the FsDatasource
    DatasourceCopier copier = new DatasourceCopier();
    copier.copy(integrationDatasource.getName(), fs.getName());

    // Disconnect it from Magma
    MagmaEngine.get().removeDatasource(fs);

    // Read it back
    MagmaEngine.get().addDatasource(new FsDatasource("imported", "target/output.zip"));

    // Dump its values
    for(ValueTable table : MagmaEngine.get().getDatasource("imported").getValueTables()) {
      for(ValueSet valueSet : table.getValueSets()) {
        for(Variable variable : table.getVariables()) {
          Value value = table.getValue(variable, valueSet);
          if(value.isSequence() && value.isNull() == false) {
            ValueSequence seq = value.asSequence();
            int order = 0;
            for(Value item : seq.getValues()) {
              System.out.println(variable.getName() + "[" + value.getValueType().getName() + "]@" + (order++) + ": " + item);
            }
          } else {
            System.out.println(variable.getName() + "[" + value.getValueType().getName() + "]: " + value);
          }
        }
      }
    }

    MagmaEngine.get().shutdown();
  }
}
