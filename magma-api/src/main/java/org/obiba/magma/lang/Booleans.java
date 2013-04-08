package org.obiba.magma.lang;

import javax.annotation.Nullable;

/**
 * Boolean helper methods.
 * <p/>
 * This class provides <a href="http://en.wikipedia.org/wiki/Ternary_logic"/>Ternary Logic</a> operations which allow
 * operating on {@code null} boolean values. The following truth table is implemented by these methods.
 * <table>
 * <tbody>
 * <tr>
 * <th><i>A</i></th>
 * <th><i>B</i></th>
 * <th><i>A</i> OR <i>B</i></th>
 * <th><i>A</i> AND <i>B</i></th>
 * <th>NOT <i>A</i></th>
 * </tr>
 * <tr>
 * <td>true</td>
 * <td>true</td>
 * <td>true</td>
 * <td>true</td>
 * <td>false</td>
 * </tr>
 * <tr>
 * <td>true</td>
 * <td>null</td>
 * <td>true</td>
 * <td>null</td>
 * <td>false</td>
 * </tr>
 * <tr>
 * <td>true</td>
 * <td>false</td>
 * <td>true</td>
 * <td>false</td>
 * <td>false</td>
 * </tr>
 * <tr>
 * <td>null</td>
 * <td>true</td>
 * <td>true</td>
 * <td>null</td>
 * <td>null</td>
 * </tr>
 * <tr>
 * <td>null</td>
 * <td>null</td>
 * <td>null</td>
 * <td>null</td>
 * <td>null</td>
 * </tr>
 * <tr>
 * <td>null</td>
 * <td>false</td>
 * <td>null</td>
 * <td>false</td>
 * <td>null</td>
 * </tr>
 * <tr>
 * <td>false</td>
 * <td>true</td>
 * <td>true</td>
 * <td>false</td>
 * <td>true</td>
 * </tr>
 * <tr>
 * <td>false</td>
 * <td>null</td>
 * <td>null</td>
 * <td>false</td>
 * <td>true</td>
 * </tr>
 * <tr>
 * <td>false</td>
 * <td>false</td>
 * <td>false</td>
 * <td>false</td>
 * <td>true</td>
 * </tr>
 * </tbody>
 * </table>
 */
public final class Booleans {

  private Booleans() {

  }

  /**
   * Implements ternary logic {@code AND} operation
   *
   * @param op1 first operand
   * @param op2 second operand
   * @return true, false or null (see truth table)
   */
  @edu.umd.cs.findbugs.annotations.SuppressWarnings(value = "NP_BOOLEAN_RETURN_NULL",
      justification = "Clients expect ternaryAnd to return null as a valid value.")
  public static Boolean ternaryAnd(@Nullable Boolean op1, @Nullable Boolean op2) {
    // If either operands is null, then the outcome is either null or false.
    if(isNull(op1) || isNull(op2)) {
      // The outcome is false if either operand is false
      if(isFalse(op1) || isFalse(op2)) return false;
      return null;
    }
    return op1 && op2;
  }

  /**
   * Implements ternary logic {@code OR} operation
   *
   * @param op1 first operand
   * @param op2 second operand
   * @return true, false or null (see truth table)
   */
  @edu.umd.cs.findbugs.annotations.SuppressWarnings(value = "NP_BOOLEAN_RETURN_NULL",
      justification = "Clients expect ternaryOr to return null as a valid value.")
  public static Boolean ternaryOr(@Nullable Boolean op1, @Nullable Boolean op2) {
    // If either operands is null, then the outcome is either null or true.
    if(isNull(op1) || isNull(op2)) {
      // The outcome is true if either operand is true
      if(isTrue(op1) || isTrue(op2)) return true;
      return null;
    }
    return op1 || op2;
  }

  /**
   * Implements ternary logic {@code NOT} operation
   *
   * @param op operand
   * @return true, false or null (see truth table)
   */
  @edu.umd.cs.findbugs.annotations.SuppressWarnings(value = "NP_BOOLEAN_RETURN_NULL",
      justification = "Clients expect ternaryNot to return null as a valid value.")
  public static Boolean ternaryNot(Boolean op) {
    // If operand is null, then the outcome is null.
    if(isNull(op)) return null;
    return !op;
  }

  /**
   * Returns true when {@code op} is {@code false}. This method returns false when {@code op} is {@code null} or {@code
   * true}.
   *
   * @param op value to test
   * @return true when {@code op} is {@code false}
   */
  public static boolean isFalse(Boolean op) {
    return op != null && !op;
  }

  /**
   * Returns true when {@code op} is {@code true}. This method returns false when {@code op} is {@code null} or {@code
   * false}.
   *
   * @param op value to test
   * @return true when {@code op} is {@code true}
   */
  public static boolean isTrue(Boolean op) {
    return op != null && op;
  }

  /**
   * Returns true when {@code op} is {@code null}.
   *
   * @param op value to test
   * @return true when {@code op} is {@code null}
   */
  private static boolean isNull(Boolean op) {
    return op == null;
  }

}
