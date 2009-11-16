package org.obiba.magma.integration.service;

import java.util.List;

import org.obiba.magma.integration.model.Action;
import org.obiba.magma.integration.model.Interview;
import org.obiba.magma.integration.model.Participant;

public interface IntegrationService {

  public Participant getParticipant(String barcode);

  public List<Participant> getParticipants();

  public List<Interview> getInterviews();

  public List<Action> getActions(Participant participant);
}
