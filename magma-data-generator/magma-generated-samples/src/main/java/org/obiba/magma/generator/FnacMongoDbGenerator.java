/*
 * Copyright (c) 2017 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.magma.generator;

import java.net.UnknownHostException;

import org.obiba.core.util.FileUtil;
import org.obiba.magma.Datasource;
import org.obiba.magma.DatasourceFactory;
import org.obiba.magma.MagmaEngine;
import org.obiba.magma.ValueTable;
import org.obiba.magma.Variable;
import org.obiba.magma.datasource.fs.FsDatasource;
import org.obiba.magma.datasource.generated.GeneratedValueTable;
import org.obiba.magma.datasource.mongodb.MongoDBDatasourceFactory;
import org.obiba.magma.support.DatasourceCopier;
import org.obiba.magma.support.Initialisables;
import org.obiba.magma.xstream.MagmaXStreamExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Stopwatch;
import com.google.common.collect.Lists;
import com.mongodb.MongoClient;

public class FnacMongoDbGenerator {

  private static final Logger log = LoggerFactory.getLogger(FnacMongoDbGenerator.class);

  private static final String MONGO_DB_TEST = "big-ds";

  private static final String MONGO_DB_URL = "mongodb://localhost/" + MONGO_DB_TEST;

  private static final String DATASOURCE = "ds";

  private static final int NB_ENTITIES = 100_000;

  private FnacMongoDbGenerator() {}

  public static void main(String... args) throws Exception {
    setup();
    generate();
    MagmaEngine.get().shutdown();
  }

  public static void generate() throws Exception {

    Stopwatch stopwatch = Stopwatch.createStarted();

    DatasourceFactory factory = new MongoDBDatasourceFactory(DATASOURCE, MONGO_DB_URL);
    Datasource datasource = factory.create();

    Datasource fsDatasource = new FsDatasource("fs", FileUtil.getFileFromResource("FNAC.zip"));
    Initialisables.initialise(fsDatasource);

    Iterable<Variable> variables = fsDatasource.getValueTable("FNAC").getVariables();
//    for(Variable variable : variables) {
//      if(variable.getValueType().isNumeric()) {
//        if(!variable.hasAttribute("minimun")) {
//          log.warn("No min for {} - {}", variable.getName(), variable.getValueType());
////          Variable.Builder builder = Variable.Builder.sameAs(variable).addAttribute()
//        }
//        if(!variable.hasAttribute("maximum")) {
//          log.warn("No max for {} - {}", variable.getName(), variable.getValueType());
//        }
//      }
//    }

    ValueTable generatedValueTable = new GeneratedValueTable(datasource, Lists.newArrayList(variables), NB_ENTITIES);

    DatasourceCopier.Builder.newCopier().build().copy(generatedValueTable, "FNAC", datasource);

    log.info("Data generated in {}", stopwatch);

  }

  private static void setup() throws Exception {
    new MagmaEngine().extend(new MagmaXStreamExtension());
    setupMongoDB();
  }

  private static void setupMongoDB() throws UnknownHostException {
    MongoClient client = new MongoClient();
    client.dropDatabase(MONGO_DB_TEST);
    client.close();
  }

}
