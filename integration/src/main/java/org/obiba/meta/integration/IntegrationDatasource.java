package org.obiba.meta.integration;

import java.util.Set;

import org.obiba.meta.NoSuchValueSetException;
import org.obiba.meta.Occurrence;
import org.obiba.meta.ValueSet;
import org.obiba.meta.Variable;
import org.obiba.meta.beans.BeanValueSetConnection;
import org.obiba.meta.beans.BeansDatasource;
import org.obiba.meta.beans.DefaultBeanValueSetConnection;
import org.obiba.meta.integration.model.Action;
import org.obiba.meta.integration.model.Participant;
import org.obiba.meta.integration.service.IntegrationService;
import org.obiba.meta.support.DatasourceBean;
import org.obiba.meta.support.OccurrenceBean;

import com.google.common.collect.ImmutableSet;

public class IntegrationDatasource extends DatasourceBean implements BeansDatasource {

  private IntegrationService service;

  IntegrationDatasource(IntegrationService service) {
    this.service = service;
  }

  @Override
  public BeanValueSetConnection createConnection(ValueSet valueSet) {
    return new DefaultBeanValueSetConnection(valueSet, this);
  }

  protected Participant getParticipant(BeanValueSetConnection connection) {
    return service.getParticipant(connection.getValueSet().getVariableEntity().getIdentifier());
  }

  @Override
  public <B> B resolveBean(BeanValueSetConnection connection, Class<B> type, Variable variable) {
    if(Participant.class.equals(type)) {
      return (B) getParticipant(connection);
    } else if(Action.class.equals(type)) {
      Occurrence occurrence = (Occurrence) connection.getValueSet();
      return (B) service.getActions(getParticipant(connection)).get(occurrence.getOrder());
    }
    throw new NoSuchValueSetException(connection.getValueSet().getVariableEntity());
  }

  @Override
  public Set<Occurrence> loadOccurrences(BeanValueSetConnection connection, Variable variable) {
    Participant participant = getParticipant(connection);
    ImmutableSet.Builder<Occurrence> builder = ImmutableSet.builder();
    int order = 0;
    for(Action action : service.getActions(participant)) {
      builder.add(new OccurrenceBean(connection.getValueSet(), variable.getOccurrenceGroup(), order++));
    }
    return builder.build();
  }
}
