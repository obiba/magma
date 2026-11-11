package org.obiba.magma.lang;

import java.io.*;

import org.junit.Test;
import org.obiba.magma.VariableEntity;
import org.obiba.magma.support.VariableEntityBean;

import static org.fest.assertions.api.Assertions.assertThat;

public class VariableEntityListTest {

  @Test
  public void test_lookupSurvivesSerialization() throws Exception {
    VariableEntityList list = new VariableEntityList();
    list.add(new VariableEntityBean("Participant", "1"));
    list.add(new VariableEntityBean("Participant", "1"));

    ByteArrayOutputStream bytes = new ByteArrayOutputStream();
    try (ObjectOutputStream out = new ObjectOutputStream(bytes)) {
      out.writeObject(list);
    }
    VariableEntityList read;
    try (ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray()))) {
      read = (VariableEntityList) in.readObject();
    }

    assertThat(read).hasSize(1);
    assertThat(read.contains(new VariableEntityBean("Participant", "1"))).isTrue();
    assertThat(read.add(new VariableEntityBean("Participant", "1"))).isFalse();
  }
}
