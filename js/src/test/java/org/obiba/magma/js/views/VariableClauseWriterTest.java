package org.obiba.magma.js.views;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.obiba.magma.MagmaEngine;
import org.obiba.magma.Variable;
import org.obiba.magma.type.IntegerType;
import org.obiba.magma.xstream.DefaultXStreamFactory;

import com.thoughtworks.xstream.XStream;

/**
 * Test XStream configuration needed to produce the VariableClause portion of the opal-config.xml file.
 */
public class VariableClauseWriterTest {

  private XStream xstream;

  private Variable yearVariable;

  private Variable smokingAge;

  private Variable sex;

  private Set<Variable> variables;

  @Before
  public void setUp() throws Exception {
    new MagmaEngine();
    xstream = new DefaultXStreamFactory().createXStream();

    yearVariable = buildYear();
    smokingAge = buildSmokingAge();
    sex = buildSex();

    variables = new HashSet<Variable>();
    variables.add(yearVariable);
    variables.add(smokingAge);
    variables.add(sex);

  }

  @After
  public void tearDown() throws Exception {
    MagmaEngine.get().shutdown();
  }

  private Variable buildYear() {
    Variable.Builder yearBuilder = Variable.Builder.newVariable("GENERIC_128", IntegerType.get(), "Participant");
    yearBuilder.addAttribute("label", "Birth Year", Locale.CANADA);
    yearBuilder.addAttribute("URI", "http://www.datashaper.org/owl/2009/10/generic.owl#GENERIC_128");
    yearBuilder.addAttribute("script", "$('Admin.Participant.birthDate').year()");
    return yearBuilder.build();
  }

  private Variable buildSmokingAge() {
    Variable.Builder yearBuilder = Variable.Builder.newVariable("CPT_498", IntegerType.get(), "Participant");
    yearBuilder.addAttribute("label", "Age cigarette smoking on most days onset", Locale.CANADA);
    yearBuilder.addAttribute("URI", "http://www.datashaper.org/owl/2009/10/cpt.owl#CPT_498");
    String script = "smokeValue = $('STARTED_HABITUAL_SMOKING_AGE');\n" //
        + "if(smokeValue.any('AGE')) {\n" //
        + "   $('STARTED_HABITUAL_SMOKING_AGE.AGE');\n" //
        + "} else if(smokeValue.any('YEAR')) {\n" //
        + "   $('STARTED_HABITUAL_SMOKING_AGE.YEAR') - $('Admin.Participant.birthDate').year();\n" //
        + "} else {\n" //
        + "   // Code is text. It must be converted to the 'integer' type.\n" //
        + "   $var('STARTED_HABITUAL_SMOKING_AGE').category(smokeValue).code().type('integer');\n" //
        + "}\n"; //
    yearBuilder.addAttribute("script", script);
    return yearBuilder.build();
  }

  private Variable buildSex() {
    Variable.Builder yearBuilder = Variable.Builder.newVariable("GENERIC_129", IntegerType.get(), "Participant");
    yearBuilder.addAttribute("label", "Gender", Locale.CANADA);
    yearBuilder.addAttribute("URI", "http://www.datashaper.org/owl/2009/10/generic.owl#GENERIC_129");
    yearBuilder.addAttribute("sameAs", "HealthQuestionnaireIdentification.SEX");
    return yearBuilder.build();
  }

  @Test
  public void testWriteVariableClauseToXml() throws Exception {
    VariablesClause variablesClause = new VariablesClause();
    variablesClause.setVariables(variables);
    String xml = xstream.toXML(variablesClause);
    System.out.println(xml);
  }

  @Test
  public void testWriteVariableClauseToSpringResource() throws Exception {
    VariablesClause variablesClause = new VariablesClause();
    variablesClause.setVariables(variables);
    String xml = xstream.toXML(variablesClause);
    System.out.println(xml);
  }
}
