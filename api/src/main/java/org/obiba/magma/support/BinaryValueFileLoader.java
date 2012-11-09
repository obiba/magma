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
import java.io.Serializable;

import org.obiba.magma.ValueLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Loads a binary value from a file path (absolute or relative).
 */
public class BinaryValueFileLoader implements ValueLoader, Serializable {

  private static final long serialVersionUID = 3762839094392540628L;

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
      value = BinaryValueFileHelper.readValue(parent, path);
    }
    return value;
  }

}