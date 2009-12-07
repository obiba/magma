package org.obiba.magma.datasource.fs;

import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.obiba.magma.Attribute;
import org.obiba.magma.MagmaEngine;
import org.obiba.magma.MagmaRuntimeException;
import org.obiba.magma.ValueTable;
import org.obiba.magma.ValueTableWriter;
import org.obiba.magma.datasource.fs.output.NullOutputStreamWrapper;
import org.obiba.magma.support.AbstractDatasource;
import org.obiba.magma.xstream.MagmaXStreamExtension;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.thoughtworks.xstream.XStream;

import de.schlichtherle.io.ArchiveException;
import de.schlichtherle.io.File;
import de.schlichtherle.io.FileInputStream;
import de.schlichtherle.io.FileOutputStream;

/**
 * Implements a {@code Datasource} on top of an archive file in the local file system.
 */
public class FsDatasource extends AbstractDatasource {

  /**
   * Consistently use UTF-8 character set for reading and writing.
   */
  private static final Charset CHARSET = Charset.availableCharsets().get("UTF-8");

  private File datasourceArchive;

  private OutputStreamWrapper outputStreamWrapper;

  public FsDatasource(String name, String filename, OutputStreamWrapper outputStreamWrapper) {
    super(name, "fs");
    this.datasourceArchive = new File(filename);
    this.outputStreamWrapper = outputStreamWrapper;
  }

  public FsDatasource(String name, String filename) {
    this(name, filename, new NullOutputStreamWrapper());
  }

  @Override
  protected void onInitialise() {
    if(datasourceArchive.exists()) {
      Reader reader = null;
      try {
        List<Attribute> attributes = (List<Attribute>) getXStreamInstance().fromXML(reader = new InputStreamReader(new FileInputStream(new File(datasourceArchive, "metadata.xml")), CHARSET));
        for(Attribute a : attributes) {
          getInstanceAttributes().put(a.getName(), a);
        }
      } catch(FileNotFoundException e) {
        throw new MagmaRuntimeException(e);
      } finally {
        Closeables.closeQuietly(reader);
      }
    } else {
      getInstanceAttributes().put("version", Attribute.Builder.newAttribute("version").withValue("1").build());
    }
  }

  public ValueTableWriter createWriter(String name) {
    FsValueTable valueTable = null;
    if(hasValueTable(name)) {
      valueTable = (FsValueTable) getValueTable(name);
    } else {
      addValueTable(valueTable = new FsValueTable(this, name));
    }
    return new FsValueTableWriter(valueTable, getXStreamInstance());
  }

  @Override
  public void onDispose() {
    try {
      writeMetadata();
    } finally {
      try {
        File.umount(datasourceArchive);
      } catch(ArchiveException e) {
        throw new MagmaRuntimeException(e);
      }
    }
  }

  protected void writeMetadata() {
    Writer writer = null;
    try {
      getXStreamInstance().toXML(new LinkedList<Attribute>(getInstanceAttributes().values()), writer = new OutputStreamWriter(new FileOutputStream(new File(datasourceArchive, "metadata.xml")), CHARSET));
    } catch(FileNotFoundException e) {
      throw new MagmaRuntimeException(e);
    } finally {
      Closeables.closeQuietly(writer);
    }
  }

  @Override
  protected Set<String> getValueTableNames() {
    if(datasourceArchive.exists()) {
      java.io.File[] files = datasourceArchive.listFiles(new FileFilter() {
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

  File getEntry(String name) {
    return new File(datasourceArchive, name);
  }

  XStream getXStreamInstance() {
    // TODO: Use the FsDatasource version to obtain the proper XStream instance
    return MagmaEngine.get().getExtension(MagmaXStreamExtension.class).getXStreamFactory().createXStream();
  }

  <T> T readEntry(File entry, InputCallback<T> callback) {
    if(entry.exists()) {
      Reader reader = null;
      try {
        return callback.readEntry(reader = createReader(entry));
      } catch(FileNotFoundException e) {
        // this cannot happen since we tested file.exists().
        throw new MagmaRuntimeException(e);
      } catch(IOException e) {
        throw new MagmaRuntimeException(e);
      } finally {
        Closeables.closeQuietly(reader);
      }
    }
    return null;
  }

  <T> T readEntry(String name, InputCallback<T> callback) {
    return readEntry(new File(datasourceArchive, name), callback);
  }

  <T> T writeEntry(File file, OutputCallback<T> callback) {
    Writer writer = null;
    try {
      return callback.writeEntry(writer = createWriter(file));
    } catch(IOException e) {
      throw new MagmaRuntimeException(e);
    } finally {
      Closeables.closeQuietly(writer);
    }
  }

  <T> T writeEntry(String name, OutputCallback<T> callback) {
    return writeEntry(new File(datasourceArchive, name), callback);
  }

  Reader createReader(File entry) {
    try {
      return new InputStreamReader(new FileInputStream(entry), CHARSET);
    } catch(FileNotFoundException e) {
      throw new MagmaRuntimeException(e);
    }
  }

  Writer createWriter(File entry) {
    try {
      return new OutputStreamWriter(outputStreamWrapper.wrap(new FileOutputStream(entry), entry), CHARSET);
    } catch(FileNotFoundException e) {
      throw new MagmaRuntimeException(e);
    }
  }

  interface InputCallback<T> {
    T readEntry(Reader reader) throws IOException;
  }

  interface OutputCallback<T> {
    T writeEntry(Writer writer) throws IOException;
  }

}
