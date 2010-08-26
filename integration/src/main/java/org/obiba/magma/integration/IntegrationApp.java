package org.obiba.magma.integration;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.Properties;

import org.obiba.magma.Datasource;
import org.obiba.magma.MagmaEngine;
import org.obiba.magma.Value;
import org.obiba.magma.ValueSequence;
import org.obiba.magma.ValueSet;
import org.obiba.magma.ValueTable;
import org.obiba.magma.Variable;
import org.obiba.magma.crypt.support.GeneratedKeyPairProvider;
import org.obiba.magma.datasource.crypt.EncryptedSecretKeyDatasourceEncryptionStrategy;
import org.obiba.magma.datasource.crypt.GeneratedSecretKeyDatasourceEncryptionStrategy;
import org.obiba.magma.datasource.csv.CsvDatasource;
import org.obiba.magma.datasource.excel.ExcelDatasource;
import org.obiba.magma.datasource.fs.FsDatasource;
import org.obiba.magma.datasource.hibernate.SessionFactoryProvider;
import org.obiba.magma.datasource.hibernate.support.HibernateDatasourceFactory;
import org.obiba.magma.datasource.hibernate.support.LocalSessionFactoryProvider;
import org.obiba.magma.datasource.jdbc.JdbcDatasourceFactory;
import org.obiba.magma.datasource.jdbc.JdbcDatasourceSettings;
import org.obiba.magma.integration.service.XStreamIntegrationServiceFactory;
import org.obiba.magma.js.MagmaJsExtension;
import org.obiba.magma.support.DatasourceCopier;
import org.obiba.magma.type.DateTimeType;
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
    deleteFile(encrypted);

    // Generate a new KeyPair.
    GeneratedKeyPairProvider keyPairProvider = new GeneratedKeyPairProvider();
    GeneratedSecretKeyDatasourceEncryptionStrategy generatedEncryptionStrategy = new GeneratedSecretKeyDatasourceEncryptionStrategy();
    generatedEncryptionStrategy.setKeyProvider(keyPairProvider);
    FsDatasource fs = new FsDatasource("export", encrypted, generatedEncryptionStrategy);
    MagmaEngine.get().addDatasource(fs);

    // Export the IntegrationDatasource to the FsDatasource
    DatasourceCopier copier = DatasourceCopier.Builder.newCopier().build();
    copier.copy(integrationDatasource.getName(), fs.getName());

    // Disconnect it from Magma
    MagmaEngine.get().removeDatasource(fs);

    // Read it back
    EncryptedSecretKeyDatasourceEncryptionStrategy encryptedEncryptionStrategy = new EncryptedSecretKeyDatasourceEncryptionStrategy();
    encryptedEncryptionStrategy.setKeyProvider(keyPairProvider);
    MagmaEngine.get().addDatasource(new FsDatasource("imported", encrypted, encryptedEncryptionStrategy));

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
    deleteFile(decrypted);
    fs = new FsDatasource("export", decrypted);
    MagmaEngine.get().addDatasource(fs);

    // Export the IntegrationDatasource to the FsDatasource
    copier.copy(integrationDatasource.getName(), fs.getName());

    // Disconnect it from Magma
    MagmaEngine.get().removeDatasource(fs);

    SessionFactoryProvider provider = new LocalSessionFactoryProvider("org.hsqldb.jdbcDriver", "jdbc:hsqldb:file:target/integration-hibernate.db;shutdown=true", "sa", "", "org.hibernate.dialect.HSQLDialect");
    HibernateDatasourceFactory hdsFactory = new HibernateDatasourceFactory("integration-hibernate", provider);

    try {
      // This is uncool. We have to initialise the DatasourceFactory before passing it to Magma, because we need to
      // start a transaction before the datasource initialises itself. Starting the transaction requires the
      // SessionFactory which is created by the DatasourceFactory. Ideally, we would have passed the factory to Magma
      // directly.
      hdsFactory.initialise();
      // Start a transaction before passing the Datasource to Magma. A tx is required for the Datasource to initialise
      // correctly.
      provider.getSessionFactory().getCurrentSession().beginTransaction();
      Datasource ds = MagmaEngine.get().addDatasource(hdsFactory.create());

      // Add some attributes to the HibernateDatasource.
      if(!ds.hasAttribute("Created by")) {
        ds.setAttributeValue("Created by", TextType.get().valueOf("Magma Integration App"));
      }

      if(!ds.hasAttribute("Created on")) {
        ds.setAttributeValue("Created on", DateTimeType.get().valueOf(new Date()));
      }

      ds.setAttributeValue("Last connected", DateTimeType.get().valueOf(new Date()));

      // Copy the data from the IntegrationDatasource to the HibernateDatasource.
      copier.copy(integrationDatasource, ds);

      MagmaEngine.get().removeDatasource(ds);

      provider.getSessionFactory().getCurrentSession().getTransaction().commit();
    } catch(RuntimeException e) {
      try {
        provider.getSessionFactory().getCurrentSession().getTransaction().rollback();
      } catch(Exception ignore) {
      }
      e.printStackTrace();
      throw e;
    }

    // CSV Datasource

    File csvDataFile = new File("target", "data.csv");
    deleteFile(csvDataFile);
    createFile(csvDataFile);
    File csvVariablesFile = new File("target", "variables.csv");
    deleteFile(csvVariablesFile);
    csvVariablesFile.createNewFile();
    CsvDatasource csvDatasource = new CsvDatasource("csv");
    csvDatasource.addValueTable("integration-app", csvVariablesFile, csvDataFile);
    csvDatasource.setVariablesHeader("integration-app", "name#valueType#entityType#mimeType#unit#occurrenceGroup#repeatable#script".split("#"));
    MagmaEngine.get().addDatasource(csvDatasource);
    DatasourceCopier.Builder.newCopier().dontCopyNullValues().build().copy(integrationDatasource, csvDatasource);

    // Excel Datasource

    File excelFile = new File("target", "excel-datasource.xls");
    deleteFile(excelFile);
    ExcelDatasource ed = new ExcelDatasource("excel", excelFile);
    MagmaEngine.get().addDatasource(ed);

    DatasourceCopier.Builder.newCopier().dontCopyValues().build().copy(integrationDatasource, ed);

    JdbcDatasourceFactory jdbcDatasourceFactory = new JdbcDatasourceFactory();
    jdbcDatasourceFactory.setName("jdbc");
    jdbcDatasourceFactory.setJdbcProperties(getJdbcProperties());
    jdbcDatasourceFactory.setDatasourceSettings(new JdbcDatasourceSettings("Participant", null, null, true));
    Datasource jd = jdbcDatasourceFactory.create();
    MagmaEngine.get().addDatasource(jd);
    DatasourceCopier.Builder.newCopier().dontCopyNullValues().build().copy(integrationDatasource, jd);

    MagmaEngine.get().shutdown();
  }

  private static Properties getJdbcProperties() {
    Properties jdbcProperties = new Properties();
    jdbcProperties.setProperty(JdbcDatasourceFactory.DRIVER_CLASS_NAME, "org.hsqldb.jdbcDriver");
    jdbcProperties.setProperty(JdbcDatasourceFactory.URL, "jdbc:hsqldb:file:target/datasource_jdbc.db;shutdown=true");
    jdbcProperties.setProperty(JdbcDatasourceFactory.USERNAME, "sa");
    jdbcProperties.setProperty(JdbcDatasourceFactory.PASSWORD, "");

    return jdbcProperties;
  }

  private static void deleteFile(File file) {
    if(file.exists()) {
      if(!file.delete()) {
        System.err.println("Failed to delete file: " + file.getPath());
      }
    }
  }

  private static void createFile(File file) throws IOException {
    boolean fileDidNotExist = file.createNewFile();
    if(fileDidNotExist) {
      System.out.println("Created file: " + file.getPath());
    } else {
      System.out.println("File already exists: " + file.getPath());
    }

  }
}
