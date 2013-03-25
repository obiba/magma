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

import javax.annotation.Nonnull;

import org.obiba.magma.Datasource;
import org.obiba.magma.datasource.spss.SpssValueTable;
import org.opendatafoundation.data.spss.SPSSFile;

public class SpssValueTableFactory {

  @Nonnull
  private final Datasource datasource;

  @Nonnull
  private final String entityType;

  @Nonnull
  private final File file;

  @Nonnull
  private final String characterSet;

  private final String locale;

  private final String name;

  public SpssValueTableFactory(@Nonnull Datasource datasource, @Nonnull String entityType, @Nonnull File file,
      @Nonnull String characterSet, @Nonnull String locale) {
    this.datasource = datasource;
    this.entityType = entityType;
    this.file = file;
    this.characterSet = characterSet;
    this.locale = locale;
    name = createValidFileName(file);
  }

  public SpssValueTable create() {
    try {
      SPSSFile spssFile = new SPSSFile(file, Charset.forName(characterSet));
      spssFile.logFlag = false;

      return new SpssValueTable(datasource, name, entityType, locale, spssFile);
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

    if(postion > 0) {
      filename = filename.substring(0, postion);
    }

    return filename.replaceAll("[.:? ]", "");
  }

}
