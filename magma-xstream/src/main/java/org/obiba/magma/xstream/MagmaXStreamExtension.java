/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.magma.xstream;

import java.util.Map;

import org.obiba.magma.MagmaEngineExtension;

import com.google.common.collect.ImmutableMap;

public class MagmaXStreamExtension implements MagmaEngineExtension {

  private static final long serialVersionUID = 4697988112637998169L;

  private final transient XStreamFactory currentFactory = new DefaultXStreamFactory();

  @edu.umd.cs.findbugs.annotations.SuppressWarnings("SE_TRANSIENT_FIELD_NOT_RESTORED")
  private final transient Map<String, ? extends XStreamFactory> compatibleFactories = ImmutableMap
      .of("1", currentFactory);

  @Override
  public String getName() {
    return "magma-xstream";
  }

  @Override
  public void initialise() {
  }

  public XStreamFactory getXStreamFactory() {
    return currentFactory;
  }

  public XStreamFactory getXStreamFactory(String version) {
    return compatibleFactories.get(version);
  }
}
