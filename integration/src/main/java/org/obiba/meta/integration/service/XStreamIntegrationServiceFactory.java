package org.obiba.meta.integration.service;

import java.io.Reader;
import java.util.List;

import org.obiba.meta.integration.model.Action;
import org.obiba.meta.integration.model.Interview;
import org.obiba.meta.integration.model.Participant;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.thoughtworks.xstream.XStream;

public class XStreamIntegrationServiceFactory {

  private XStream xstream;

  public XStreamIntegrationServiceFactory() {
    xstream = new XStream();
    xstream.alias("participant", Participant.class);
    xstream.alias("interview", Interview.class);
    xstream.alias("action", Action.class);
    xstream.setMode(XStream.ID_REFERENCES);
  }

  public IntegrationService buildService(Reader xmlReader) {
    List<Object> objects = (List<Object>) xstream.fromXML(xmlReader);
    return new InMemoryIntegrationService(ImmutableList.copyOf(Iterables.filter(objects, Participant.class)), ImmutableList.copyOf(Iterables.filter(objects, Action.class)));
  }
}
