/*
 * Copyright (c) 2012 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.magma.datasource.spss.support;

import java.io.File;

import org.obiba.magma.datasource.spss.SpssDatasource;
import org.obiba.magma.datasource.spss.SpssValueTable;

public class SpssValueTableFactory {

  private final File file;

  private String name;

  public SpssValueTableFactory(File file) {
    this.file = file;
    name = createValidFileName(file);
  }

  public SpssValueTable create(SpssDatasource datasource, String entityType) {
    return new SpssValueTable(datasource, name, entityType, file);
  }

  public String getName() {
    return name;
  }


  private String createValidFileName(File file) {
    String filename = file.getName();
    int postion = filename.lastIndexOf('.');

    if (postion > 0) {
      filename = filename.substring(0, postion);
    }

    return filename.replaceAll("[.:? ]", "");
  }

}
