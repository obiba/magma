/*
 * Copyright (c) 2016 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

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
