package org.obiba.magma.datasource.generated;

import java.io.File;
import java.io.IOException;
import java.util.NoSuchElementException;
import java.util.Set;

import javax.annotation.Nullable;

import org.junit.Test;
import org.obiba.core.util.FileUtil;
import org.obiba.magma.Category;
import org.obiba.magma.Datasource;
import org.obiba.magma.MagmaEngine;
import org.obiba.magma.ValueTable;
import org.obiba.magma.Variable;
import org.obiba.magma.datasource.excel.ExcelDatasource;
import org.obiba.magma.datasource.fs.FsDatasource;
import org.obiba.magma.js.MagmaJsExtension;
import org.obiba.magma.support.Disposables;
import org.obiba.magma.support.Initialisables;
import org.obiba.magma.support.MultithreadedDatasourceCopier;
import org.obiba.magma.xstream.MagmaXStreamExtension;

import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;

public class FeedbackServerDataGeneratorTest {

  @SuppressWarnings("ResultOfMethodCallIgnored")
  @Test
  public void generateTestData() throws IOException {

    new MagmaEngine().extend(new MagmaJsExtension()).extend(new MagmaXStreamExtension());

    File targetFile = new File("target/generated.zip");
    targetFile.delete();

    ExcelDatasource eds = new ExcelDatasource("patate", FileUtil.getFileFromResource("clsa-opal.xls"));
    Datasource target = new FsDatasource("target", targetFile);

    Initialisables.initialise(eds, target);

    ValueTable table = eds.getValueTables().iterator().next();

    ValueTable generated = new GeneratedValueTable(null, fixConditions(table), 3000);

    MultithreadedDatasourceCopier.Builder.newCopier().from(generated).to(target).as(table.getName()).build().copy();

    Disposables.dispose(eds, target);

  }

  private Set<Variable> fixConditions(ValueTable table) {
    ImmutableSet.Builder<Variable> builder = ImmutableSet.builder();
    for(Variable v : table.getVariables()) {
      Variable.Builder copy = Variable.Builder.sameAs(v);
      if(v.hasAttribute("Universe and Conditions")) {
        String conditionString = v.getAttributeStringValue("Universe and Conditions");
        if(conditionString.contains("=")) {
          String script = patate(table, conditionString);
          if(script != null) {
            copy.addAttribute("condition", script);
          }
        }
      }
      builder.add(copy.build());
    }
    return builder.build();
  }

  private Category findCategory(Variable variable, final String categoryStr) {
    return Iterables.find(variable.getCategories(), new Predicate<Category>() {

      @Override
      public boolean apply(Category input) {
        return input.getName().equalsIgnoreCase(categoryStr.replaceAll(" ", "_"));
      }
    });
  }

  @Nullable
  private String patate(ValueTable table, String conditionString) {
    String s = or(table, conditionString);
    if(s != null && s.length() > 0) {
      return s;
    }
    return null;
  }

  private String varCond(ValueTable table, String conditionString) {
    if(conditionString.contains("=")) {
      String[] parts = conditionString.split("=");
      if(parts.length == 2) {
        String otherVarStr = parts[0].trim();
        String categoryStr = parts[1].trim();
        if(table.hasVariable(otherVarStr)) {
          Variable otherVar = table.getVariable(otherVarStr);
          try {
            Category category = findCategory(otherVar, categoryStr);
            return String.format("$('%s').any('%s')", otherVar.getName(), category.getName());
          } catch(NoSuchElementException e) {
            // ignore
          }

        }
      }
    }
    return null;
  }

  private String and(ValueTable table, String condition) {
    StringBuilder builder = new StringBuilder();

    String[] parts = condition.split("and", 2);

    String s = varCond(table, parts[0]);
    if(s != null) {
      builder.append(s);
      if(parts.length > 1) {
        String rhs = and(table, parts[1]);
        if(!rhs.isEmpty()) builder.append(".and(").append(and(table, parts[1])).append(')');
      }
      return builder.toString();
    }
    if(parts.length > 1) {
      return builder.append(and(table, parts[1])).toString();
    }
    return "";
  }

  private String or(ValueTable table, String condition) {
    StringBuilder builder = new StringBuilder();

    String[] parts = condition.split("and/or", 2);

    if(parts.length == 1) {
      return builder.append(and(table, parts[0])).toString();
    }
    builder.append(and(table, parts[0]));
    String rhs = or(table, parts[1]);
    if(!rhs.isEmpty()) builder.append(".or(").append(or(table, parts[1])).append(')');
    return builder.toString();
  }
}
