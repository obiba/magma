/*
 * Copyright (c) 2012 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.magma.datasource.spss;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.obiba.magma.ValueTable;
import org.obiba.magma.datasource.spss.support.SpssValueTableFactory;
import org.obiba.magma.support.AbstractDatasource;

public class SpssDatasource extends AbstractDatasource {

  private static final String DEFAULT_ENTITY_TYPE = "Participant";

  private final List<File> spssFiles;

  private Map<String, SpssValueTable> valueTablesMapOnInit = new LinkedHashMap<String, SpssValueTable>();


  public SpssDatasource(String name, List<File> spssFiles) {
    super(name, "spss");
    this.spssFiles = spssFiles;
  }

  @Override
  protected void onInitialise() {
    SpssValueTableFactory factory = new SpssValueTableFactory();

    for (File spssFile : spssFiles) {
      try {
        String tableName = spssFile.getName();

        if (!valueTablesMapOnInit.containsKey(tableName)) {
          valueTablesMapOnInit.put(tableName, factory.create(this, tableName, DEFAULT_ENTITY_TYPE, spssFile));
        }
      }
      catch(FileNotFoundException exception) {
        // TODO log or notify that the file did not exist
      }
      catch(Exception e) {
        System.out.print(e);
      }
    }
  }

  @Override
  protected Set<String> getValueTableNames() {
    return valueTablesMapOnInit.keySet();
  }

  @Override
  protected ValueTable initialiseValueTable(String tableName) {
    return valueTablesMapOnInit.get(tableName);
  }

}
