package org.obiba.magma.integration;

import java.io.IOException;
import java.io.InputStreamReader;
import java.security.NoSuchAlgorithmException;

import org.obiba.magma.MagmaEngine;
import org.obiba.magma.Value;
import org.obiba.magma.ValueSequence;
import org.obiba.magma.ValueSet;
import org.obiba.magma.ValueTable;
import org.obiba.magma.ValueTableWriter;
import org.obiba.magma.Variable;
import org.obiba.magma.ValueTableWriter.ValueSetWriter;
import org.obiba.magma.ValueTableWriter.VariableWriter;
import org.obiba.magma.integration.service.XStreamIntegrationServiceFactory;
import org.obiba.magma.io.FsDatasource;
import org.obiba.magma.io.output.ChainedOutputStreamWrapper;
import org.obiba.magma.io.output.DigestOutputStreamWrapper;
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

    FsDatasource fs = new FsDatasource(integrationDatasource.getName(), "target/output.zip", new ChainedOutputStreamWrapper(new DigestOutputStreamWrapper()));

    for(ValueTable table : integrationDatasource.getValueTables()) {
      ValueTableWriter vtw = fs.createWriter(table.getName());
      VariableWriter vw = vtw.writeVariables(table.getEntityType());
      for(Variable variable : table.getVariables()) {
        vw.writeVariable(variable);
      }
      vw.close();
      for(ValueSet valueSet : table.getValueSets()) {
        ValueSetWriter vsw = vtw.writeValueSet(valueSet.getVariableEntity());
        for(Variable variable : table.getVariables()) {
          Value value = table.getValue(variable, valueSet);
          vsw.writeValue(variable, value);
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
        vsw.close();
      }
      vtw.close();

    }

    // Read it back
    MagmaEngine.get().addDatasource(new FsDatasource("persisted", "target/output.zip", new ChainedOutputStreamWrapper(new DigestOutputStreamWrapper())));

  }
}
