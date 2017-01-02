/*
 * Copyright (c) 2017 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.magma.js;

import org.obiba.magma.Variable.Builder;

/**
 * A {@code Variable.Builder} extension for building javascript variables. To obtain an instance of this builder, use
 * the {@link Builder#extend(Class)} method by passing this type:
 * <p/>
 * <pre>
 * Variable.Builder builder = ...;
 *
 * builder.extend(JavascriptVariableBuilder.class).setScript(&quot;'Hello World!'&quot;);
 * ...
 * </pre>
 */
public class JavascriptVariableBuilder extends Builder {

  public static final String SCRIPT_ATTRIBUTE_NAME = "script";

  public JavascriptVariableBuilder(Builder builder) {
    super(builder);
  }

  public JavascriptVariableBuilder setScript(String script) {
    addAttribute(SCRIPT_ATTRIBUTE_NAME, script);
    return this;
  }

}
