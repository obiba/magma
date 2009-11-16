package org.obiba.meta.integration.model;

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
