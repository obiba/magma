package org.obiba.magma.datasource.generated;

import java.io.File;
import java.io.IOException;

import org.obiba.magma.Value;
import org.obiba.magma.ValueType;
import org.obiba.magma.Variable;

import com.google.common.io.Resources;

public class BinaryVariableValueGenerator extends GeneratedVariableValueSource {

  public static final String PATH = File.separator +
      BinaryVariableValueGenerator.class.getName().replaceAll("\\.", File.separator) + ".class";

  public BinaryVariableValueGenerator(Variable variable) {
    super(variable);
  }

  @Override
  protected Value nextValue(Variable variable, GeneratedValueSet valueSet) {
    try {
      return ValueType.Factory.newValue(getBytes());
    } catch(IOException e) {
      throw new RuntimeException("Cannot load " + PATH + " as Binary value");
    }
  }

  private static byte[] getBytes() throws IOException {
    return Resources.toByteArray(BinaryVariableValueGenerator.class.getResource(PATH));
  }

  public static long getLength() throws IOException {
    return getBytes().length;
  }

}
