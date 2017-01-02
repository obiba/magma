/*
 * Copyright (c) 2017 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

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
