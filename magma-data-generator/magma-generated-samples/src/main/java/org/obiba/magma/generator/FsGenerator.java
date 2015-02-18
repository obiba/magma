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
import org.obiba.magma.datasource.excel.ExcelDatasource;
import org.obiba.magma.datasource.fs.FsDatasource;
import org.obiba.magma.datasource.generated.GeneratedValueTable;
import org.obiba.magma.js.MagmaJsExtension;
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

  private static final int NB_ENTITIES_MIN = 1000;

  private static final int NB_ENTITIES_MAX = 2000;

  private FsGenerator() {}

  public static void main(String... args) throws Exception {
    new MagmaEngine().extend(new MagmaXStreamExtension()).extend(new MagmaJsExtension());

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
    log.info("Input file lookup in {}", inputFile.getAbsolutePath());

    if (inputFile.isDirectory()) {
      for (File sourceFile : inputFile.listFiles(new FileFilter() {
        @Override
        public boolean accept(File pathname) {
          return pathname.getName().endsWith(".zip") || pathname.getName().endsWith(".xlsx");
        }
      })) {
        generate(sourceFile, randInt(NB_ENTITIES_MIN, NB_ENTITIES_MAX));
      }
    } else if (inputFile.getName().endsWith(".zip")) {
      generate(inputFile, randInt(NB_ENTITIES_MIN, NB_ENTITIES_MAX));
    }
  }

  public static void generate(File sourceFile, int nbEntities) throws Exception {
    File generatedFolder = getDestinationFolder(sourceFile);

    String destinationFileName = sourceFile.getName();
    Datasource fsDatasource;
    if (sourceFile.getName().endsWith(".zip")) {
      fsDatasource = new FsDatasource("source", sourceFile);
    } else {
      fsDatasource = new ExcelDatasource("source", sourceFile);
      destinationFileName = sourceFile.getName().replace(".xlsx","") + ".zip";
    }
    Datasource targetDatasource = new FsDatasource("target", new File(generatedFolder, destinationFileName));

    generate(fsDatasource, targetDatasource, nbEntities);
  }

  private static void generate(Datasource source, Datasource target, int nbEntities)  throws Exception {
    Initialisables.initialise(target, source);

    for (ValueTable table : source.getValueTables()) {
      Stopwatch stopwatch = Stopwatch.createStarted();
      ValueTable toCopy = table;
      if (table.getValueSetCount() == 0) {
        toCopy = new GeneratedValueTable(target, Lists.newArrayList(table.getVariables()),
            nbEntities);
      }
      DatasourceCopier.Builder.newCopier().build().copy(toCopy, table.getName(), target);
      log.info("Data generated and copied for {} ({}) in {}", table.getName(), source.getName(), stopwatch);
    }

    Disposables.dispose(target, source);
  }

  private static File getDestinationFolder(File sourceFile) {
    File generatedFolder = new File(sourceFile.getParentFile(), "generated");
    if (!generatedFolder.exists() && !generatedFolder.mkdirs()) {
      throw new RuntimeException("Cannot create directory: " + generatedFolder.getAbsolutePath());
    }
    return generatedFolder;
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
