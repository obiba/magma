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
