package org.obiba.meta.integration.service;

import java.util.List;

import org.obiba.meta.integration.model.Interview;
import org.obiba.meta.integration.model.Participant;

public interface IntegrationService {

  public Participant getParticipant(String barcode);

  public List<Participant> getParticipants();

  public List<Interview> getInterviews();

}
