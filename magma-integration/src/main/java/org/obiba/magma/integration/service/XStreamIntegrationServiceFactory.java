package org.obiba.magma.integration.service;

import java.io.Reader;

import org.obiba.magma.integration.model.Action;
import org.obiba.magma.integration.model.Interview;
import org.obiba.magma.integration.model.Participant;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.thoughtworks.xstream.XStream;

public class XStreamIntegrationServiceFactory {

  private final XStream xstream;

  public XStreamIntegrationServiceFactory() {
    xstream = new XStream();
    xstream.alias("participant", Participant.class);
    xstream.alias("interview", Interview.class);
    xstream.alias("action", Action.class);
    xstream.setMode(XStream.ID_REFERENCES);
  }

  public IntegrationService buildService(Reader xmlReader) {
    @SuppressWarnings("unchecked")
    Iterable<Object> objects = (Iterable<Object>) xstream.fromXML(xmlReader);
    return new InMemoryIntegrationService(ImmutableList.copyOf(Iterables.filter(objects, Participant.class)),
        ImmutableList.copyOf(Iterables.filter(objects, Action.class)));
  }
}
