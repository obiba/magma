package org.obiba.meta.integration.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.obiba.meta.integration.model.Interview;
import org.obiba.meta.integration.model.Participant;

public class InMemoryIntegrationService implements IntegrationService {

  private List<Participant> participants;

  public InMemoryIntegrationService(List<Participant> participants) {
    this.participants = participants;
  }

  @Override
  public Participant getParticipant(String barcode) {
    for(Participant p : getParticipants()) {
      if(barcode.equals(p.getBarcode())) {
        return p;
      }
    }
    throw new IllegalArgumentException("No such participant " + barcode);
  }

  @Override
  public List<Interview> getInterviews() {
    List<Interview> interviews = new ArrayList<Interview>();
    for(Participant p : getParticipants()) {
      if(p.getInterview() != null) {
        interviews.add(p.getInterview());
      }
    }
    return interviews;
  }

  @Override
  public List<Participant> getParticipants() {
    return Collections.unmodifiableList(participants);
  }

}
