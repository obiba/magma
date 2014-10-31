/*
 * Copyright (c) 2014 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.magma.generator;

import java.io.File;
import java.io.FileFilter;
import java.util.Random;

import org.obiba.magma.Datasource;
import org.obiba.magma.MagmaEngine;
import org.obiba.magma.ValueTable;
import org.obiba.magma.datasource.fs.FsDatasource;
import org.obiba.magma.datasource.generated.GeneratedValueTable;
import org.obiba.magma.support.DatasourceCopier;
import org.obiba.magma.support.Disposables;
import org.obiba.magma.support.Initialisables;
import org.obiba.magma.xstream.MagmaXStreamExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Stopwatch;
import com.google.common.collect.Lists;

public class FsGenerator {

  private static final Logger log = LoggerFactory.getLogger(FsGenerator.class);

  private static final int NB_ENTITIES_MIN = 500;

  private static final int NB_ENTITIES_MAX = 1000;

  private FsGenerator() {}

  public static void main(String... args) throws Exception {
    new MagmaEngine().extend(new MagmaXStreamExtension());

    if (args == null || args.length == 0) {
      generate(new File(System.getProperty("user.dir")));
    } else {
      for (String arg : args) {
        File inputFile = new File(arg);
        if (inputFile.exists()) {
          generate(inputFile);
        }
      }
    }

    MagmaEngine.get().shutdown();
  }

  public static void generate(File inputFile) throws Exception {
    log.info("Zip file lookup in {}", inputFile.getAbsolutePath());

    if (inputFile.isDirectory()) {
      for (File zipFile : inputFile.listFiles(new FileFilter() {
        @Override
        public boolean accept(File pathname) {
          return pathname.getName().endsWith(".zip");
        }
      })) {
        generate(zipFile, randInt(NB_ENTITIES_MIN, NB_ENTITIES_MAX));
      }
    } else if (inputFile.getName().endsWith(".zip")) {
      generate(inputFile, randInt(NB_ENTITIES_MIN, NB_ENTITIES_MAX));
    }
  }

  public static void generate(File zipFile, int nbEntities) throws Exception {
    File generatedFolder = new File(zipFile.getParentFile(), "generated");
    if (!generatedFolder.exists() && !generatedFolder.mkdirs()) {
      throw new RuntimeException("Cannot create directory: " + generatedFolder.getAbsolutePath());
    }
    Datasource targetDatasource = new FsDatasource("target", new File(generatedFolder, zipFile.getName()));
    Datasource fsDatasource = new FsDatasource("source", zipFile);

    Initialisables.initialise(targetDatasource, fsDatasource);

    for (ValueTable table : fsDatasource.getValueTables()) {
      Stopwatch stopwatch = Stopwatch.createStarted();
      ValueTable toCopy = table;
      if (table.getValueSetCount() == 0) {
        toCopy = new GeneratedValueTable(targetDatasource, Lists.newArrayList(table.getVariables()),
            nbEntities);
      }
      DatasourceCopier.Builder.newCopier().build().copy(toCopy, table.getName(), targetDatasource);
      log.info("Data generated and copied for {} ({}) in {}", table.getName(), zipFile.getName(), stopwatch);
    }

    Disposables.dispose(targetDatasource, fsDatasource);
  }

  /**
   * Returns a pseudo-random number between min and max, inclusive.
   * The difference between min and max can be at most
   * <code>Integer.MAX_VALUE - 1</code>.
   *
   * @param min Minimum value
   * @param max Maximum value.  Must be greater than min.
   * @return Integer between min and max, inclusive.
   * @see java.util.Random#nextInt(int)
   */
  public static int randInt(int min, int max) {

    // NOTE: Usually this should be a field rather than a method
    // variable so that it is not re-seeded every call.
    Random rand = new Random();

    // nextInt is normally exclusive of the top value,
    // so add 1 to make it inclusive
    int randomNum = rand.nextInt((max - min) + 1) + min;

    return randomNum;
  }

}
