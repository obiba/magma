/*
 * Copyright (c) 2017 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.magma.beans;

import org.obiba.magma.MagmaRuntimeException;
import org.obiba.magma.ValueSet;

public class NoSuchBeanException extends MagmaRuntimeException {

  private static final long serialVersionUID = 559153486087896008L;

  public NoSuchBeanException(ValueSet valueSet, Class<?> beanType, String message) {
    super(message);
  }
}
