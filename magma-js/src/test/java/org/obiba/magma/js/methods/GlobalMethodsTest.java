package org.obiba.magma.js.methods;

import org.junit.Ignore;
import org.junit.Test;
import org.obiba.magma.MagmaRuntimeException;
import org.obiba.magma.Value;
import org.obiba.magma.ValueSequence;
import org.obiba.magma.js.AbstractJsTest;
import org.obiba.magma.js.MagmaContextFactory;
import org.obiba.magma.js.ScriptableValue;
import org.obiba.magma.type.IntegerType;
import org.obiba.magma.type.TextType;

import static org.fest.assertions.api.Assertions.assertThat;

public class GlobalMethodsTest extends AbstractJsTest {

//  private static final Logger log = LoggerFactory.getLogger(GlobalMethodsTest.class);

  @Test
  public void test_newValue_inferred_int() throws Exception {
    ScriptableValue sv = GlobalMethods.newValue(MagmaContextFactory.createContext(),
        new Object[] { 1 });
    assertThat(sv.getValue().isNull()).isFalse();
    assertThat(sv.getValue().isSequence()).isFalse();
    assertThat((IntegerType) sv.getValueType()).isEqualTo(IntegerType.get());
    assertThat((Long) sv.getValue().getValue()).isEqualTo(1l);
  }

  @Test
  public void test_newValue_int() throws Exception {
    ScriptableValue sv = GlobalMethods
        .newValue(MagmaContextFactory.createContext(), new Object[] { "1", "integer" });
    assertThat(sv.getValue().isNull()).isFalse();
    assertThat(sv.getValue().isSequence()).isFalse();
    assertThat((IntegerType) sv.getValueType()).isEqualTo(IntegerType.get());
    assertThat((Long) sv.getValue().getValue()).isEqualTo(1l);
  }

  @Test(expected = MagmaRuntimeException.class)
  public void test_newValue_wrong_type() throws Exception {
    GlobalMethods.newValue(MagmaContextFactory.createContext(),
        new Object[] { "qwerty", "integer" });
  }

  @Test
  @Ignore
  public void test_newSequence_int() throws Exception {
    Object tmp = MagmaContextFactory.getEngine().evaluate("[1, 2, 3]");
    ScriptableValue sv = GlobalMethods
        .newSequence(MagmaContextFactory.createContext(), new Object[] { tmp });
    assertThat(sv.getValue().isSequence()).isTrue();
    assertThat((IntegerType) sv.getValueType()).isEqualTo(IntegerType.get());
    assertThat(sv.getValue().getLength()).isEqualTo(3);
    ValueSequence sequence = sv.getValue().asSequence();

    for(int i = 1; i <= 3; i++) {
      Value value = sequence.get(i - 1);
      assertThat((IntegerType) value.getValueType()).isEqualTo(IntegerType.get());
      assertThat((Long) value.getValue()).isEqualTo((long) i);
    }
  }

 @Test
 @Ignore
  public void test_newSequence_String() throws Exception {
    Object tmp = MagmaContextFactory.getEngine().evaluate("['1', '2', '3']");
    ScriptableValue sv = GlobalMethods.newSequence(MagmaContextFactory.createContext(),
        new Object[] { tmp });
    assertThat(sv.getValue().isSequence()).isTrue();
    assertThat((TextType) sv.getValueType()).isEqualTo(TextType.get());
    assertThat(sv.getValue().getLength()).isEqualTo(3l);
    ValueSequence sequence = sv.getValue().asSequence();
    for(int i = 1; i <= 3; i++) {
      Value value = sequence.get(i - 1);
      assertThat((TextType) value.getValueType()).isEqualTo(TextType.get());
      assertThat((String) value.getValue()).isEqualTo(String.valueOf(i));
    }
  }

  @Test
  @Ignore
  public void test_newSequence_with_int_type() throws Exception {
    Object tmp = MagmaContextFactory.getEngine().evaluate("['1', '2', '3']");
    ScriptableValue sv = GlobalMethods
        .newSequence(MagmaContextFactory.createContext(), new Object[] { tmp, "integer" });
    assertThat(sv.getValue().isSequence()).isTrue();
    assertThat((IntegerType) sv.getValueType()).isEqualTo(IntegerType.get());
    assertThat(sv.getValue().getLength()).isEqualTo(3l);
    ValueSequence sequence = sv.getValue().asSequence();
    for(int i = 1; i <= 3; i++) {
      Value value = sequence.get(i - 1);
      assertThat((IntegerType) value.getValueType()).isEqualTo(IntegerType.get());
      assertThat((Long) value.getValue()).isEqualTo((long) i);
    }
  }
}
