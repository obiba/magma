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
import java.util.ArrayList;
import java.util.List;

import javax.validation.constraints.NotNull;

import org.obiba.magma.AbstractDatasourceFactory;
import org.obiba.magma.Datasource;
import org.obiba.magma.datasource.spss.SpssDatasource;

import com.google.common.base.Strings;

public class SpssDatasourceFactory extends AbstractDatasourceFactory {

  public static final String DEFAULT_DATASOURCE_NAME = "spss";

  public static final String DEFAULT_CHARACTER_SET = "ISO-8859-1";

  private static final String DEFAULT_ENTITY_TYPE = "Participant";

  private static final String DEFAULT_LOCALE = "en";

  //
  // Data members
  //

  private final List<File> files = new ArrayList<>();

  private String characterSet;

  private String entityType;

  private String locale;

  private String idVariable;

  public void setFile(String path) {
    setFile(new File(path));
  }

  public void setFile(File file) {
    files.add(file);
  }

  public void addFile(String path) {
    files.add(new File(path));
  }

  public void addFile(File file) {
    files.add(file);
  }

  public void setCharacterSet(String characterSet) {
    this.characterSet = characterSet;
  }

  public void setEntityType(String entityType) {
    this.entityType = entityType;
  }

  public void setLocale(String locale) {
    this.locale = locale;
  }

  public void setIdVariable(String idVariable) {
    this.idVariable = idVariable;
  }

  @Override
  public Datasource create() {
    return internalCreate();
  }

  @NotNull
  @Override
  protected Datasource internalCreate() {
    if(Strings.isNullOrEmpty(characterSet)) {
      characterSet = DEFAULT_CHARACTER_SET;
    }

    if(Strings.isNullOrEmpty(entityType)) {
      entityType = DEFAULT_ENTITY_TYPE;
    }

    if(Strings.isNullOrEmpty(locale)) {
      locale = DEFAULT_LOCALE;
    }

    return new SpssDatasource(getName(), files, characterSet, entityType, locale, idVariable);
  }
}
