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
import java.io.FileNotFoundException;

import org.obiba.magma.datasource.spss.SpssDatasource;
import org.obiba.magma.datasource.spss.SpssValueTable;
import org.opendatafoundation.data.spss.SPSSFile;

public class SpssValueTableFactory {

  public SpssValueTable create(SpssDatasource datasource, String name, String entityType, File file) throws FileNotFoundException {
    SpssValueTable valueTable = new SpssValueTable(datasource, name, entityType, file);
    System.out.print(">>>> " + valueTable.getName());
    valueTable.initialise();

    return valueTable;
  }
}
