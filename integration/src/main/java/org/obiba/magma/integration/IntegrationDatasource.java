package org.obiba.magma.integration;

import java.util.Set;

import org.obiba.magma.ValueSet;
import org.obiba.magma.ValueTable;
import org.obiba.magma.Variable;
import org.obiba.magma.VariableValueSource;
import org.obiba.magma.beans.AbstractBeanVariableEntityProvider;
import org.obiba.magma.beans.BeanValueTable;
import org.obiba.magma.beans.BeanVariableValueSourceFactory;
import org.obiba.magma.beans.ValueSetBeanResolver;
import org.obiba.magma.integration.model.Action;
import org.obiba.magma.integration.model.Interview;
import org.obiba.magma.integration.model.Participant;
import org.obiba.magma.integration.service.IntegrationService;
import org.obiba.magma.js.JavascriptVariableBuilder;
import org.obiba.magma.js.JavascriptVariableValueSource;
import org.obiba.magma.support.AbstractDatasource;
import org.obiba.magma.support.VariableEntityProvider;
import org.obiba.magma.type.BooleanType;
import org.obiba.magma.type.IntegerType;
import org.obiba.magma.type.TextType;

import com.google.common.collect.ImmutableSet;

public class IntegrationDatasource extends AbstractDatasource {

  private IntegrationService service;

  IntegrationDatasource(IntegrationService service) {
    super("integration-ds", "beans");
    this.service = service;
  }

  @Override
  protected Set<String> getValueTableNames() {
    return ImmutableSet.of("integration-app");
  }

  @Override
  protected ValueTable initialiseValueTable(String tableName) {

    final ImmutableSet.Builder<VariableValueSource> sources = new ImmutableSet.Builder<VariableValueSource>();

    BeanVariableValueSourceFactory<Participant> variables = new BeanVariableValueSourceFactory<Participant>(
        "Participant", Participant.class);
    variables.setProperties(
        ImmutableSet.of("barcode", "firstName", "lastName", "gender", "interview.startDate", "interview.endDate"));
    sources.addAll(variables.createSources());

    sources.add(new JavascriptVariableValueSource(
        Variable.Builder.newVariable("fullName", TextType.get(), "Participant").extend(JavascriptVariableBuilder.class)
            .setScript("$('firstName') + ' ' + $('lastName')").build()));
    sources.add(new JavascriptVariableValueSource(
        Variable.Builder.newVariable("interviewYear", IntegerType.get(), "Participant")
            .extend(JavascriptVariableBuilder.class).setScript("$('interview.startDate').year()").build()));
    sources.add(new JavascriptVariableValueSource(
        Variable.Builder.newVariable("isMale", BooleanType.get(), "Participant").extend(JavascriptVariableBuilder.class)
            .setScript("$('gender').any('Male')").build()));
    sources.add(new JavascriptVariableValueSource(
        Variable.Builder.newVariable("isFemale", BooleanType.get(), "Participant")
            .extend(JavascriptVariableBuilder.class).setScript("$('gender').any('Female')").build()));

    BeanVariableValueSourceFactory<Action> actionFactory = new BeanVariableValueSourceFactory<Action>("Participant",
        Action.class);
    actionFactory.setProperties(ImmutableSet.of("stage"));
    actionFactory.setPrefix("Action");
    actionFactory.setOccurrenceGroup("Action");
    sources.addAll(actionFactory.createSources());

    VariableEntityProvider provider = new AbstractBeanVariableEntityProvider<Interview>("Participant",
        "participant.barcode") {

      @Override
      protected Iterable<Interview> loadBeans() {
        return IntegrationDatasource.this.service.getInterviews();
      }
    };

    BeanValueTable table = new BeanValueTable(this, tableName, provider);
    table.addResolver(new ParticipantBeanResolver());
    table.addResolver(new ActionBeanResolver());
    table.addVariableValueSources(sources.build());
    return table;
  }

  protected Participant getParticipant(ValueSet valueSet) {
    return service.getParticipant(valueSet.getVariableEntity().getIdentifier());
  }

  private class ParticipantBeanResolver implements ValueSetBeanResolver {
    @Override
    public Object resolve(Class<?> type, ValueSet valueSet, Variable variable) {
      return getParticipant(valueSet);
    }

    @Override
    public boolean resolves(Class<?> type) {
      return Participant.class.equals(type);
    }
  }

  private class ActionBeanResolver implements ValueSetBeanResolver {
    @Override
    public Object resolve(Class<?> type, ValueSet valueSet, Variable variable) {
      return service.getActions(getParticipant(valueSet));
    }

    @Override
    public boolean resolves(Class<?> type) {
      return Action.class.equals(type);
    }
  }

}
