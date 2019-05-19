/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.magma.datasource.excel;

import org.obiba.core.util.FileUtil;
import org.obiba.magma.Initialisable;
import org.obiba.magma.MagmaEngine;

@SuppressWarnings("UseOfSystemOutOrSystemErr")
public class ExcelDatasourceProfile {

  private ExcelDatasourceProfile() {}

  public static void main(String... args) {
    new MagmaEngine();
    long before = System.currentTimeMillis();
    Initialisable datasource = new ExcelDatasource("big",
        FileUtil.getFileFromResource("org/obiba/magma/datasource/excel/big.xlsx"));
    datasource.initialise();
    long duration = System.currentTimeMillis() - before;
    System.out.println("duration=" + duration / 1000 + "s");
  }

}
