/*
 * Copyright (c) 2016 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.magma.test;

import java.io.IOException;

import de.flapdoodle.embed.mongo.MongodExecutable;
import de.flapdoodle.embed.mongo.MongodStarter;
import de.flapdoodle.embed.mongo.config.MongodConfigBuilder;
import de.flapdoodle.embed.mongo.config.Net;
import de.flapdoodle.embed.mongo.distribution.Version;
import de.flapdoodle.embed.process.runtime.Network;

public class EmbeddedMongoProcessWrapper {

  private int dbPort;

  private static final MongodStarter starter = MongodStarter.getDefaultInstance();

  private static MongodExecutable mongodExe;

  private String serverSocketAddress;

  public EmbeddedMongoProcessWrapper() {
    this(0);
  }

  public EmbeddedMongoProcessWrapper(int dbPort) {
    this.dbPort = dbPort;
  }

  public String getServerSocketAddress() {
    return serverSocketAddress;
  }

  public void start() throws IOException {
    Net net = this.dbPort == 0 ? new Net() : new Net(this.dbPort, Network.localhostIsIPv6());
    serverSocketAddress = String.format("localhost:%s", net.getPort());
    mongodExe = starter.prepare(new MongodConfigBuilder().version(Version.Main.V3_0).net(net).build());
    mongodExe.start();
  }

  public void stop() {
    mongodExe.stop();
  }
}
