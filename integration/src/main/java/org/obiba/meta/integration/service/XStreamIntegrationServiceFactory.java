package org.obiba.meta.integration.service;

import java.io.Reader;
import java.util.List;

import org.obiba.meta.integration.model.Interview;
import org.obiba.meta.integration.model.Participant;

import com.thoughtworks.xstream.XStream;

public class XStreamIntegrationServiceFactory {

  private XStream xstream;

  public XStreamIntegrationServiceFactory() {
    xstream = new XStream();
    xstream.alias("participant", Participant.class);
    xstream.alias("interview", Interview.class);
    xstream.setMode(XStream.ID_REFERENCES);
  }

  public IntegrationService buildService(Reader xmlReader) {
    List<Participant> participants = (List<Participant>) xstream.fromXML(xmlReader);
    return new InMemoryIntegrationService(participants);
  }
}
