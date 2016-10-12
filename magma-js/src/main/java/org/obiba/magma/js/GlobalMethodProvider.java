/*
 * Copyright (c) 2016 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.magma.js;

import java.lang.reflect.Method;
import java.util.Collection;

import javax.validation.constraints.NotNull;

import org.obiba.magma.js.methods.AbstractGlobalMethodProvider;

/**
 * Provides global JavaScript extension methods.
 * <p/>
 * Consider extending {@link AbstractGlobalMethodProvider} instead of implementing this interface directly.
 */
public interface GlobalMethodProvider {

  /**
   * Returns a collection of Java {@code Method} to be exposed in the global JavaScript scope.
   */
  Collection<Method> getJavaScriptExtensionMethods();

  /**
   * Returns the name of the JavaScript method. This method allows renaming the Java method before exposing it in the
   * JavaScript scope.
   *
   * @param method the Java method to rename.
   * @return the name of the JavaScript method.
   */
  String getJavaScriptMethodName(@NotNull Method method);

}
