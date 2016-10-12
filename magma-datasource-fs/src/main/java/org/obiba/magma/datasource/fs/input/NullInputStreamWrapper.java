/*
 * Copyright (c) 2016 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.magma.datasource.fs.input;

import java.io.InputStream;

import org.obiba.magma.datasource.fs.InputStreamWrapper;

import de.schlichtherle.io.File;

/**
 *
 */
public class NullInputStreamWrapper implements InputStreamWrapper {

  @Override
  public InputStream wrap(InputStream os, File entry) {
    return os;
  }

}
