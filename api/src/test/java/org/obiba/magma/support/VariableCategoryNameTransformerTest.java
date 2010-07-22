package org.obiba.magma.support;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.obiba.magma.Category;
import org.obiba.magma.MagmaEngine;
import org.obiba.magma.Variable;
import org.obiba.magma.support.DatasourceCopier.VariableCategoryNameTransformer;
import org.obiba.magma.support.DatasourceCopier.VariableTransformer;
import org.obiba.magma.type.TextType;

public class VariableCategoryNameTransformerTest {

  @Before
  public void startYourEngine() {
    new MagmaEngine();
  }

  @After
  public void stopYourEngine() {
    MagmaEngine.get().shutdown();
  }

  @Test
  public void testTransform() {
    Variable.Builder builder = Variable.Builder.newVariable("VAR", TextType.get(), "Participant");
    builder.addCategory("YES", "1");
    builder.addCategory("NO", "0");
    Variable variable = builder.build();

    VariableTransformer transformer = new VariableCategoryNameTransformer();
    Variable transformed = transformer.transform(variable);

    Assert.assertEquals(variable.getName(), transformed.getName());
    Assert.assertEquals(2, transformed.getCategories().size());
    int count = 0;
    for(Category category : transformed.getCategories()) {
      if(category.getName().equals("1") || category.getName().equals("0")) {
        count++;
      }
    }
    Assert.assertEquals(2, count);

  }

}
