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

import org.mozilla.javascript.Scriptable;
import org.obiba.magma.js.methods.BooleanMethods;
import org.obiba.magma.js.methods.CompareMethods;
import org.obiba.magma.js.methods.DateTimeMethods;
import org.obiba.magma.js.methods.GeoMethods;
import org.obiba.magma.js.methods.NumericMethods;
import org.obiba.magma.js.methods.ScriptableValueMethods;
import org.obiba.magma.js.methods.TextMethods;
import org.obiba.magma.js.methods.UnitMethods;
import org.obiba.magma.js.methods.ValueSequenceMethods;

/**
 * A factory class for constructing the {@code ScriptableValue} javascript prototype. This prototype defines all the
 * methods that can be invoked on {@code ScriptableValue}. These methods are provided by static methods of some Java
 * classes. Classes to inspect can be added to the factory through the {@code #addMethodProvider(Class)} method. For
 * each method of each class added to the factory, the factory will add a {@code FunctionObject} to the
 * {@code ScriptableValue}.
 * <p/>
 * For example, adding the following class to the factory will result in a {@code hello()} method to be added:
 * <p/>
 * <pre>
 * public final class HelloMethod {
 *   public static Object hello(Context ctx, Scriptable thisObj, Object[] args, Function funObj) {
 *     return &quot;Hello&quot;;
 *   }
 * }
 * </pre>
 * <p/>
 * The contributed method can then be invoked on any {@code ScriptableValue}:
 * <p/>
 * <pre>
 *   $('MyVar').hello()
 * </pre>
 * <p/>
 * would evaluate to "Hello".
 * <p/>
 * In order to chain these methods together, they should return {@code ScriptableValue} themselves. This allows
 * constructs like this one:
 * <p/>
 * <pre>
 *   $('BloodPressure.Systolic').avg().round(2).greaterThan($('BloodPressure.Dyastolic').avg().round(2))
 * </pre>
 */
public class ScriptableValuePrototypeFactory extends AbstractPrototypeFactory {

  public ScriptableValuePrototypeFactory() {
    addMethodProvider(BooleanMethods.class);
    addMethodProvider(DateTimeMethods.class);
    addMethodProvider(TextMethods.class);
    addMethodProvider(ScriptableValueMethods.class);
    addMethodProvider(ValueSequenceMethods.class);
    addMethodProvider(NumericMethods.class);
    addMethodProvider(CompareMethods.class);
    addMethodProvider(UnitMethods.class);
    addMethodProvider(GeoMethods.class);
  }

  @Override
  protected Scriptable newPrototype() {
    return new ScriptableValue();
  }
}
