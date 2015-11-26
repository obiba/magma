package org.obiba.magma.test;

import java.io.IOException;

import de.flapdoodle.embed.mongo.MongodExecutable;
import de.flapdoodle.embed.mongo.MongodStarter;
import de.flapdoodle.embed.mongo.config.MongodConfigBuilder;
import de.flapdoodle.embed.mongo.config.Net;
import de.flapdoodle.embed.mongo.distribution.Version;
import de.flapdoodle.embed.process.runtime.Network;


public class EmbeddedMongoProcessWrapper {

  private int dbPort = 12345;

  private static final MongodStarter starter = MongodStarter.getDefaultInstance();

  private static MongodExecutable mongodExe;

  public EmbeddedMongoProcessWrapper() {
  }

  public EmbeddedMongoProcessWrapper(int dbPort) {
    this.dbPort = dbPort;
  }

  public void start() throws IOException {
    mongodExe = starter.prepare(new MongodConfigBuilder().version(Version.Main.V3_0).net(new Net(dbPort, Network.localhostIsIPv6())).build());
    mongodExe.start();
  }

  public void stop() {
    mongodExe.stop();
  }
}
