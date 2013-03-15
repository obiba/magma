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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.obiba.magma.ValueLoader;

/**
 * Loads a binary value from a file path (absolute or relative).
 */
public class BinaryValueFileLoader implements ValueLoader, Serializable {

  private static final long serialVersionUID = 3762839094392540628L;

//  private static final Logger log = LoggerFactory.getLogger(BinaryValueFileLoader.class);

  @Nullable
  private File parent;

  private final String path;

  private byte[] value;

  @SuppressWarnings("UnusedDeclaration")
  public BinaryValueFileLoader(String path) {
    this((File) null, path);
  }

  @SuppressWarnings("UnusedDeclaration")
  public BinaryValueFileLoader(String parent, String path) {
    setParent(parent);
    this.path = path;
  }

  public BinaryValueFileLoader(@Nullable File parent, String path) {
    setParent(parent);
    this.path = path;
  }

  public void setParent(@Nullable String parent) {
    if(parent == null) {
      this.parent = null;
    } else {
      setParent(new File(parent));
    }
  }

  public void setParent(@Nullable File parent) {
    this.parent = parent;
  }

  @Override
  public boolean isNull() {
    return path == null || path.isEmpty();
  }

  @Nonnull
  @Override
  @edu.umd.cs.findbugs.annotations.SuppressWarnings("EI_EXPOSE_REP")
  public Object getValue() {
    if(value == null) {
      value = BinaryValueFileHelper.readValue(parent, path);
    }
    return value;
  }

}