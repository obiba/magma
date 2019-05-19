/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

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

  private final List<Participant> participants;

  private final List<Action> actions;

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
    List<Interview> interviews = new ArrayList<>();
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
