/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/

package org.obiba.magma.datasource.fs.output;

import java.io.OutputStream;

import org.obiba.magma.datasource.fs.OutputStreamWrapper;

import de.schlichtherle.io.File;

/**
 * 
 */
public class NullOutputStreamWrapper implements OutputStreamWrapper {

  @Override
  public OutputStream wrap(OutputStream os, File entry) {
    return os;
  }

}
