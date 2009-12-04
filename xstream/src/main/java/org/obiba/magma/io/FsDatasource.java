package org.obiba.magma.io;

import java.io.FileFilter;
import java.util.Set;

import org.obiba.magma.ValueTable;
import org.obiba.magma.ValueTableWriter;
import org.obiba.magma.io.output.NullOutputStreamWrapper;
import org.obiba.magma.support.AbstractDatasource;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;

import de.schlichtherle.io.File;

/**
 * Implements a {@code Datasource} on top of a file in the local file system.
 */
public class FsDatasource extends AbstractDatasource {

  private File file;

  private OutputStreamWrapper outputStreamWrapper;

  public FsDatasource(String name, String filename, OutputStreamWrapper outputStreamWrapper) {
    super(name, "fs");
    this.file = new File(filename);
    this.outputStreamWrapper = outputStreamWrapper;
  }

  public FsDatasource(String name, String filename) {
    this(name, filename, new NullOutputStreamWrapper());
  }

  @Override
  protected Set<String> getValueTableNames() {
    if(file.exists()) {
      java.io.File[] files = file.listFiles(new FileFilter() {
        @Override
        public boolean accept(java.io.File pathname) {
          return pathname.isDirectory();
        }
      });
      Set<String> tableNames = Sets.newHashSet();
      for(java.io.File f : files) {
        tableNames.add(f.getName());
      }
      return tableNames;
    }
    return ImmutableSet.of();
  }

  @Override
  protected ValueTable initialiseValueTable(String tableName) {
    return new FsValueTable(this, tableName);
  }

  File getFile() {
    return file;
  }

  public ValueTableWriter createWriter(String valueTable) {
    FsValueTableWriter writer = new FsValueTableWriter(file, valueTable, outputStreamWrapper);
    writer.initialise();
    return writer;
  }
}
