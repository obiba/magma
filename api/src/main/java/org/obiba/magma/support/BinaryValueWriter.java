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
import java.io.FileOutputStream;
import java.util.List;

import org.obiba.magma.Attribute;
import org.obiba.magma.MagmaRuntimeException;
import org.obiba.magma.Value;
import org.obiba.magma.Variable;
import org.obiba.magma.VariableEntity;
import org.obiba.magma.type.TextType;

import com.google.common.collect.Lists;

/**
 *
 */
public class BinaryValueWriter {

  public static Value writeFileValue(File parent, Variable variable, VariableEntity entity, Value value) {
    return writeFileValue(new File(parent, variable.getName()), getFileName(variable, entity), getFileExtension(variable), value);
  }

  /**
   * Returns the value representing the file names that were written.
   * @param parent
   * @param name
   * @param extension
   * @param value
   * @return
   */
  private static Value writeFileValue(File parent, String name, String extension, Value value) {
    if(value.isNull()) return TextType.get().nullValue();

    Value rval;
    if(value.isSequence()) {
      int i = 1;
      List<Value> names = Lists.newArrayList();
      for(Value val : value.asSequence().getValue()) {
        names.add(writeFileValue(parent, name + "-" + i, extension, val));
        i++;
      }
      rval = TextType.get().sequenceOf(names);
    } else {
      File file = new File(parent, name + "." + extension);
      try {
        if(parent.exists() == false) {
          parent.mkdirs();
        } else if(file.exists()) {
          file.delete();
        }
        file.createNewFile();
        FileOutputStream out = new FileOutputStream(file);
        out.write((byte[]) value.getValue());
        out.close();
      } catch(Exception e) {
        throw new MagmaRuntimeException("Failed writing file: " + file.getAbsolutePath(), e);
      }
      rval = TextType.get().valueOf(parent.getName() + "/" + file.getName());
    }

    return rval;
  }

  private static String getFileName(Variable variable, VariableEntity entity) {
    String prefix = variable.getName();

    for(Attribute attr : variable.getAttributes()) {
      if(attr.getName().equalsIgnoreCase("filename") || attr.getName().equalsIgnoreCase("file-name")) {
        String name = variable.getAttributeStringValue(attr.getName());
        if(name.length() > 0) {
          prefix = name;
          break;
        }
      }
    }

    return prefix + "-" + entity.getIdentifier();
  }

  private static String getFileExtension(Variable variable) {
    String suffix = "bin";

    for(Attribute attr : variable.getAttributes()) {
      if(attr.getName().equalsIgnoreCase("fileextension") || attr.getName().equalsIgnoreCase("file-extension")) {
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

    return suffix;
  }
}
