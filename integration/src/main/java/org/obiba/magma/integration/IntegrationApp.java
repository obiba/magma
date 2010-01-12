package org.obiba.magma.integration;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.NoSuchAlgorithmException;
import java.util.Date;

import org.obiba.magma.MagmaEngine;
import org.obiba.magma.Value;
import org.obiba.magma.ValueSequence;
import org.obiba.magma.ValueSet;
import org.obiba.magma.ValueTable;
import org.obiba.magma.Variable;
import org.obiba.magma.crypt.support.GeneratedKeyPairProvider;
import org.obiba.magma.datasource.crypt.EncryptedSecretKeyDatasourceEncryptionStrategy;
import org.obiba.magma.datasource.crypt.GeneratedSecretKeyDatasourceEncryptionStrategy;
import org.obiba.magma.datasource.fs.DatasourceCopier;
import org.obiba.magma.datasource.fs.FsDatasource;
import org.obiba.magma.datasource.hibernate.HibernateDatasource;
import org.obiba.magma.datasource.hibernate.HibernateDatasourceManager;
import org.obiba.magma.datasource.hibernate.SessionFactoryProvider;
import org.obiba.magma.datasource.hibernate.support.LocalSessionFactoryProvider;
import org.obiba.magma.integration.service.XStreamIntegrationServiceFactory;
import org.obiba.magma.js.MagmaJsExtension;
import org.obiba.magma.type.DateType;
import org.obiba.magma.type.TextType;
import org.obiba.magma.xstream.MagmaXStreamExtension;

/**
 */
public class IntegrationApp {

  public static void main(String[] args) throws IOException, NoSuchAlgorithmException {
    new MagmaEngine().extend(new MagmaJsExtension()).extend(new MagmaXStreamExtension());

    XStreamIntegrationServiceFactory factory = new XStreamIntegrationServiceFactory();
    IntegrationDatasource integrationDatasource = new IntegrationDatasource(factory.buildService(new InputStreamReader(IntegrationApp.class.getResourceAsStream("participants.xml"), "UTF-8")));

    MagmaEngine.get().addDatasource(integrationDatasource);

    File encrypted = new File("target", "output-encrypted.zip");
    if(encrypted.exists()) {
      encrypted.delete();
    }

    // Generate a new KeyPair.
    GeneratedKeyPairProvider keyPairProvider = new GeneratedKeyPairProvider();
    FsDatasource fs = new FsDatasource("export", encrypted, new GeneratedSecretKeyDatasourceEncryptionStrategy(keyPairProvider));
    MagmaEngine.get().addDatasource(fs);

    // Export the IntegrationDatasource to the FsDatasource
    DatasourceCopier copier = new DatasourceCopier();
    copier.copy(integrationDatasource.getName(), fs.getName());

    // Disconnect it from Magma
    MagmaEngine.get().removeDatasource(fs);

    // Read it back
    MagmaEngine.get().addDatasource(new FsDatasource("imported", encrypted, new EncryptedSecretKeyDatasourceEncryptionStrategy(keyPairProvider)));

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

    File decrypted = new File("target", "output-decrypted.zip");
    if(decrypted.exists()) {
      decrypted.delete();
    }
    fs = new FsDatasource("export", decrypted);
    MagmaEngine.get().addDatasource(fs);

    // Export the IntegrationDatasource to the FsDatasource
    copier.copy(integrationDatasource.getName(), fs.getName());

    // Disconnect it from Magma
    MagmaEngine.get().removeDatasource(fs);

    SessionFactoryProvider provider = new LocalSessionFactoryProvider("org.hsqldb.jdbcDriver", "jdbc:hsqldb:file:target/integration-hibernate.db;shutdown=true", "sa", "", "org.hibernate.dialect.HSQLDialect");
    HibernateDatasourceManager manager = new HibernateDatasourceManager(provider);
    MagmaEngine.get().addDatasourceManager(manager);

    String datasourceName = "integration-hibernate";
    HibernateDatasource ds = manager.listAvailableDatasources().contains(datasourceName) ? manager.open(datasourceName) : manager.create(datasourceName);

    try {
      provider.getSessionFactory().getCurrentSession().beginTransaction();
      MagmaEngine.get().addDatasource(ds);

      // Add some attributes to the HibernateDatasource.
      if(!ds.hasAttribute("Created by")) {
        ds.setAttributeValue("Created by", TextType.get().valueOf("Magma Integration App"));
      }

      if(!ds.hasAttribute("Created on")) {
        ds.setAttributeValue("Created on", DateType.get().valueOf(new Date()));
      }

      ds.setAttributeValue("Last connected", DateType.get().valueOf(new Date()));

      // Copy the data from the IntegrationDatasource to the HibernateDatasource.
      copier.copy(integrationDatasource, ds);

      MagmaEngine.get().removeDatasource(ds);

      provider.getSessionFactory().getCurrentSession().getTransaction().commit();
    } catch(RuntimeException e) {
      provider.getSessionFactory().getCurrentSession().getTransaction().rollback();
      e.printStackTrace();
      throw e;
    }

    MagmaEngine.get().shutdown();
  }
}
