/*******************************************************************************
 * Copyright (c) 2012 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.magma.support;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.util.List;
import java.util.regex.Pattern;

import org.obiba.magma.Attribute;
import org.obiba.magma.MagmaRuntimeException;
import org.obiba.magma.Value;
import org.obiba.magma.Variable;
import org.obiba.magma.VariableEntity;
import org.obiba.magma.type.TextType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.google.common.io.Files;

/**
 *
 */
@SuppressWarnings("UnusedDeclaration")
public class BinaryValueFileHelper {

  private static final Logger log = LoggerFactory.getLogger(BinaryValueFileHelper.class);

  private BinaryValueFileHelper() {
  }

  /**
   * Read a file into a binary value.
   *
   * @param parent
   * @param path
   * @return
   */
  @SuppressWarnings({ "ResultOfMethodCallIgnored", "PMD.RV_RETURN_VALUE_IGNORED_BAD_PRACTICE" })
  public static byte[] readValue(File parent, String path) {
    byte[] value = null;
    try {
      File file = new File(path);
      if(!file.isAbsolute() && parent != null) {
        file = new File(parent, path);
      }
      log.debug("Loading binary from: {}", file.getAbsolutePath());
      FileInputStream fin = new FileInputStream(file);
      value = new byte[(int) file.length()];
      fin.read(value);
      fin.close();
      log.debug("Binary loaded from: {}", file.getAbsolutePath());
    } catch(Exception e) {
      throw new MagmaRuntimeException("File cannot be read: " + path, e);
    }
    return value;
  }

  /**
   * Remove the file (or files in case of a repeatable variable) for the given variable and entity.
   *
   * @param parent
   * @param variable
   * @param entity
   */
  @SuppressWarnings({ "ResultOfMethodCallIgnored", "PMD.RV_RETURN_VALUE_IGNORED_BAD_PRACTICE" })
  public static void removeValue(File parent, Variable variable, VariableEntity entity) {
    final String name = getFileName(variable, entity);
    final String extension = getFileExtension(variable);
    if(variable.isRepeatable()) {
      for(File file : parent.listFiles(new FilenameFilter() {

        @Override
        public boolean accept(File f, String n) {
          return Pattern.matches(name + "-\\d+" + "\\." + extension, n);
        }
      })) {
        file.delete();
      }
    } else {
      removeFileValue(parent, name, extension);
    }
  }

  /**
   * @return <code>true</code> if and only if the file or directory is
   *         successfully deleted; <code>false</code> otherwise
   * @see java.io.File#delete()
   */
  private static boolean removeFileValue(File parent, String name, String extension) {
    File file = new File(parent, name + "." + extension);
    return file.delete();
  }

  /**
   * Write binary in file or files in case of a sequence of values.
   *
   * @param parent
   * @param variable
   * @param entity
   * @param value
   * @return
   */
  public static Value writeValue(File parent, Variable variable, VariableEntity entity, Value value) {
    return writeFileValue(parent, getFileName(variable, entity), getFileExtension(variable), value);
  }

  /**
   * Returns the value representing the file names that were written.
   *
   * @param parent
   * @param name
   * @param extension
   * @param value
   * @return
   */
  @SuppressWarnings({ "ResultOfMethodCallIgnored", "PMD.RV_RETURN_VALUE_IGNORED_BAD_PRACTICE" })
  private static Value writeFileValue(File parent, String name, String extension, Value value) {
    if(value.isSequence()) return writeFileValueSequence(parent, name, extension, value);

    if(value.isNull()) {
      removeFileValue(parent, name, extension);
      return TextType.get().nullValue();
    }

    File file = new File(parent, name + "." + extension);
    File tmpFile = new File(parent, file.getName() + ".tmp");
    try {
      if(!parent.exists()) {
        parent.mkdirs();
      }
      tmpFile.createNewFile();
      FileOutputStream out = new FileOutputStream(tmpFile);
      out.write((byte[]) value.getValue());
      out.close();
      if(file.exists()) {
        file.delete();
      }
      Files.move(tmpFile, file);
      log.debug("File written: {}", file.getAbsolutePath());
    } catch(Exception e) {
      throw new MagmaRuntimeException("Failed writing file: " + file.getAbsolutePath(), e);
    }
    return TextType.get().valueOf(file.getName());
  }

  /**
   * Returns the value sequence representing the file names that were written.
   *
   * @param parent
   * @param name
   * @param extension
   * @param value
   * @return
   */
  @SuppressWarnings({ "ResultOfMethodCallIgnored", "PMD.RV_RETURN_VALUE_IGNORED_BAD_PRACTICE" })
  private static Value writeFileValueSequence(File parent, @SuppressWarnings("TypeMayBeWeakened") final String name,
      final String extension, Value value) {
    if(value.isNull()) {
      // remove all files given a pattern
      for(File f : parent.listFiles(new FilenameFilter() {

        @Override
        public boolean accept(File dir, String n) {
          return Pattern.matches(name + "-\\d+\\." + extension, name);
        }
      })) {
        f.delete();
      }
      return TextType.get().nullSequence();
    }

    int i = 1;
    List<Value> names = Lists.newArrayList();
    for(Value val : value.asSequence().getValue()) {
      names.add(writeFileValue(parent, name + "-" + i, extension, val));
      i++;
    }
    // if list was shortened, remove the remaining ones
    File remaining = new File(parent, name + "-" + i + "." + extension);
    while(remaining.exists()) {
      remaining.delete();
      i++;
    }
    return TextType.get().sequenceOf(names);
  }

  /**
   * Get file name from the "filename" or "file-name" variable attribute if any, otherwise use the variable name.
   *
   * @param variable
   * @param entity
   * @return
   */
  private static String getFileName(Variable variable, VariableEntity entity) {
    String prefix = variable.getName();

    for(Attribute attr : variable.getAttributes()) {
      if("filename".equalsIgnoreCase(attr.getName()) || "file-name".equalsIgnoreCase(attr.getName())) {
        String name = variable.getAttributeStringValue(attr.getName());
        if(name.length() > 0) {
          prefix = name;
          break;
        }
      }
    }

    return prefix + "-" + entity.getIdentifier();
  }

  /**
   * Get the file extension from the "fileextension" or "file-extension" variable attribute if any.
   *
   * @param variable
   * @return
   */
  private static String getFileExtension(@SuppressWarnings("TypeMayBeWeakened") Variable variable) {
    String suffix = "bin";

    for(Attribute attr : variable.getAttributes()) {
      if("fileextension".equalsIgnoreCase(attr.getName()) || "file-extension".equalsIgnoreCase(attr.getName())) {
        String extension = variable.getAttributeStringValue(attr.getName());
        if(extension.startsWith(".")) {
          extension = extension.substring(1);
        }
        if(extension.length() > 0) {
          suffix = extension;
          break;
        }
      }
    }

    // TODO get file extension from variable mime-type

    return suffix;
  }
}
