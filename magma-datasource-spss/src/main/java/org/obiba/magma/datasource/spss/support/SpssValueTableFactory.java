/*
 * Copyright (c) 2016 OBiBa. All rights reserved.
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

import javax.validation.constraints.NotNull;

import org.obiba.magma.Datasource;
import org.obiba.magma.datasource.spss.SpssValueTable;
import org.obiba.magma.support.DatasourceParsingException;
import org.opendatafoundation.data.spss.SPSSFile;

import com.google.common.base.Strings;

public class SpssValueTableFactory {

  @NotNull
  private final Datasource datasource;

  @NotNull
  private final String entityType;

  @NotNull
  private final File file;

  @NotNull
  private final String characterSet;

  private final String locale;

  private final String name;

  public SpssValueTableFactory(@NotNull Datasource datasource, @NotNull String entityType, @NotNull File file,
      @NotNull String characterSet, @NotNull String locale) {
    this.datasource = datasource;
    this.entityType = entityType;
    this.file = file;
    this.characterSet = characterSet;
    this.locale = locale;
    name = createValidFileName(file);
  }

  public SpssValueTable create() {
    try {
      SPSSFile spssFile = new SPSSFile(file,
          Strings.isNullOrEmpty(characterSet) ? null : Charset.forName(characterSet));
      spssFile.logFlag = false;

      return new SpssValueTable(datasource, name, entityType, locale, spssFile);
    } catch(IOException e) {
      String fileName = file.getName();
      throw new DatasourceParsingException("Could not open file " + fileName + " to create ValueTable.", e,
          "FailedToOpenFile", fileName);
    }
  }

  public String getName() {
    return name;
  }

  private String createValidFileName(File sourceFile) {
    String filename = sourceFile.getName();
    int postion = filename.lastIndexOf('.');

    if(postion > 0) {
      filename = filename.substring(0, postion);
    }

    return filename.replaceAll("[.:? ]", "");
  }

}
