/*
 * Copyright (c) 2016 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.magma.integration.model;

public class Action {

  private Participant participant;

  private String stage;

  public final Participant getParticipant() {
    return participant;
  }

  public final void setParticipant(Participant participant) {
    this.participant = participant;
  }

  public final String getStage() {
    return stage;
  }

  public final void setStage(String stage) {
    this.stage = stage;
  }

}
