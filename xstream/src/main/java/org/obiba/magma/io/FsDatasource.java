package org.obiba.magma.io;

import java.util.Set;

import org.obiba.magma.ValueTable;
import org.obiba.magma.ValueTableWriter;
import org.obiba.magma.support.AbstractDatasource;

import de.schlichtherle.io.File;

/**
 * Implements a {@code Datasource} on top of a file in the local file system.
 */
public class FsDatasource extends AbstractDatasource {

  private File file;

  public FsDatasource(String filename) {
    this.file = new File(filename);
  }

  @Override
  protected Set<String> getValueTableNames() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  protected ValueTable initialiseValueTable(String tableName) {
    // TODO Auto-generated method stub
    return null;
  }

  public ValueTableWriter createWriter(String valueTable) {
    FsValueTableWriter writer = new FsValueTableWriter(file, valueTable);
    writer.initialise();
    return writer;
  }
}
