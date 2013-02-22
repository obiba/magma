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
import java.util.ArrayList;
import java.util.List;

import org.obiba.magma.datasource.spss.SpssDatasource;

public class SpssDatasourceFactory {

  public static final String DEFAULT_DATASOURCE_NAME = "spss";

  public SpssDatasource create(String path) {
    return create(new File(path));
  }

  public SpssDatasource create(File file) {
    List<File> files = new ArrayList<File>();
    files.add(file);

    return create(DEFAULT_DATASOURCE_NAME, files);
  }

  public SpssDatasource create(List<File> files) {
    return new SpssDatasource(DEFAULT_DATASOURCE_NAME, files);
  }

  public SpssDatasource create(String name, String path) {
    return create(name, new File(path));
  }

  public SpssDatasource create(String name, File file) {
    List<File> files = new ArrayList<File>();
    files.add(file);

    return create(name, files);
  }

  public SpssDatasource create(String name, List<File> files) {
    return new SpssDatasource(name, files);
  }
}
