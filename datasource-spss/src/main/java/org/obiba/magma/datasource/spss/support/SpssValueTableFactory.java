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
import java.io.IOException;
import java.nio.charset.Charset;

import org.obiba.magma.datasource.spss.SpssDatasource;
import org.obiba.magma.datasource.spss.SpssValueTable;
import org.opendatafoundation.data.spss.SPSSFile;

public class  SpssValueTableFactory {

  private final File file;

  private final String characterSet;

  private String name;

  public SpssValueTableFactory(File file, String characterSet) {
    this.file = file;
    this.characterSet = characterSet;
    name = createValidFileName(file);
  }

  public SpssValueTable create(SpssDatasource datasource, String entityType) {
    try {
      SPSSFile spssFile = new SPSSFile(file, Charset.forName(characterSet));
      spssFile.logFlag = false;

      return new SpssValueTable(datasource, name, entityType, spssFile);
    } catch(IOException e) {
      throw new SpssDatasourceParsingException(e, "FailedToOpenFile", file.getName());
    }
  }

  public String getName() {
    return name;
  }

  private String createValidFileName(File sourceFile) {
    String filename = sourceFile.getName();
    int postion = filename.lastIndexOf('.');

    if (postion > 0) {
      filename = filename.substring(0, postion);
    }

    return filename.replaceAll("[.:? ]", "");
  }

}
