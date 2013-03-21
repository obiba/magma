package org.obiba.magma.integration.service;

import java.util.List;

import org.obiba.magma.integration.model.Action;
import org.obiba.magma.integration.model.Interview;
import org.obiba.magma.integration.model.Participant;

public interface IntegrationService {

  Participant getParticipant(String barcode);

  List<Participant> getParticipants();

  List<Interview> getInterviews();

  List<Action> getActions(Participant participant);
}
