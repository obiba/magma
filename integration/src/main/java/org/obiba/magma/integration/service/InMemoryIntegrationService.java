package org.obiba.magma.integration.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.obiba.magma.integration.model.Action;
import org.obiba.magma.integration.model.Interview;
import org.obiba.magma.integration.model.Participant;

import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;

public class InMemoryIntegrationService implements IntegrationService {

  private List<Participant> participants;

  private List<Action> actions;

  public InMemoryIntegrationService(List<Participant> participants, List<Action> actions) {
    this.participants = participants;
    this.actions = actions;
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

  @Override
  public List<Action> getActions(final Participant participant) {
    return ImmutableList.copyOf(Iterables.filter(actions, new Predicate<Action>() {
      @Override
      public boolean apply(Action input) {
        return input.getParticipant().equals(participant);
      }
    }));
  }

}
