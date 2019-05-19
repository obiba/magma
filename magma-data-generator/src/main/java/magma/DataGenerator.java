/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package magma;

import java.io.File;
import java.io.IOException;

import org.obiba.magma.MagmaEngine;
import org.obiba.magma.ValueTable;
import org.obiba.magma.datasource.csv.CsvDatasource;
import org.obiba.magma.datasource.excel.ExcelDatasource;
import org.obiba.magma.datasource.generated.GeneratedValueTable;
import org.obiba.magma.js.MagmaJsExtension;
import org.obiba.magma.support.DatasourceCopier;
import org.obiba.magma.support.DatasourceParsingException;
import org.obiba.magma.support.Disposables;
import org.obiba.magma.support.Initialisables;
import org.obiba.magma.support.MultithreadedDatasourceCopier;

import com.google.common.collect.Sets;

/**
 * Invoke as {@code java -cp jars magma.DataGenerator -i dictionary.xlsx -o output-file.csv -n qty} where {@code qty} is
 * the number of rows to generate.
 */
@SuppressWarnings("UseOfSystemOutOrSystemErr")
public class DataGenerator {

  private static final int DEFAULT_NB_ENTITIES = 500;

  private DataGenerator() {
  }

  @SuppressWarnings({ "OverlyLongMethod", "PMD.NcssMethodCount" })
  public static void main(String... args) throws IOException {
    Integer number = DEFAULT_NB_ENTITIES;
    String outputFileName = "output.csv";
    String inputFileName = "input.xlsx";

    for(int i = 0; i < args.length; i++) {
      if("-n".equalsIgnoreCase(args[i])) {
        number = Integer.parseInt(args[++i]);
      } else if("-o".equalsIgnoreCase(args[i])) {
        outputFileName = args[++i];
      } else if("-i".equalsIgnoreCase(args[i])) {
        inputFileName = args[++i];
      }
    }

    File inputFile = new File(inputFileName);
    if(!inputFile.exists()) {
      System.err.println(String.format("Input file %s does not exist.", inputFileName));
      return;
    }

    File outputFile = new File(outputFileName);
    if(outputFile.exists() && !outputFile.delete()) {
      System.err.println(String.format("Cannot delete output file %s.", outputFile));
      return;
    }
    if(!outputFile.createNewFile()) {
      System.err.println(String.format("Cannot create output file %s.", outputFile));
      return;
    }

    new MagmaEngine().extend(new MagmaJsExtension());

    ExcelDatasource eds = new ExcelDatasource("input", inputFile);
    Initialisables.initialise(eds);

    ValueTable table = eds.getValueTables().iterator().next();

    CsvDatasource target = new CsvDatasource("target");
    target.addValueTable(table, outputFile);

    try {
      Initialisables.initialise(target);
    } catch(DatasourceParsingException e) {
      e.printList();
      return;
    }

    ValueTable generated = new GeneratedValueTable(null, Sets.newLinkedHashSet(table.getVariables()), number);

    MultithreadedDatasourceCopier.Builder.newCopier()
        .withCopier(DatasourceCopier.Builder.newCopier().dontCopyMetadata()).from(generated).to(target)
        .as(table.getName()).build().copy();

    Disposables.dispose(eds, target);

  }
}
