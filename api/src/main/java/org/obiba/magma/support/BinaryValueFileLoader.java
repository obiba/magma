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

import org.obiba.magma.MagmaRuntimeException;
import org.obiba.magma.ValueLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Loads a value from a file path (absolute or relative).
 */
public class BinaryValueFileLoader implements ValueLoader {

  private static final Logger log = LoggerFactory.getLogger(BinaryValueFileLoader.class);

  private File parent;

  private String path;

  private byte[] value;

  public BinaryValueFileLoader(String path) {
    this((File) null, path);
  }

  public BinaryValueFileLoader(String parent, String path) {
    setParent(parent);
    this.path = path;
  }

  public BinaryValueFileLoader(File parent, String path) {
    setParent(parent);
    this.path = path;
  }

  public void setParent(String parent) {
    if(parent != null) {
      setParent(new File(parent));
    } else {
      this.parent = null;
    }
  }

  public void setParent(File parent) {
    this.parent = parent;
  }

  @Override
  public boolean isNull() {
    return path == null || path.length() == 0;
  }

  @Override
  public Object getValue() {
    if(value == null) {
      try {
        File file = new File(path);
        if(file.isAbsolute() == false && parent != null) {
          file = new File(parent, path);
        }
        log.debug("Loading binary from: {}", file.getAbsolutePath());
        FileInputStream fin = new FileInputStream(file);
        value = new byte[(int) file.length()];
        fin.read(value);
        fin.close();
        log.debug("Binary loaded from: {}", file.getAbsolutePath());
      } catch(Exception e) {
        value = null;
        throw new MagmaRuntimeException("File cannot be read: " + path, e);
      }
    }
    return value;
  }

}