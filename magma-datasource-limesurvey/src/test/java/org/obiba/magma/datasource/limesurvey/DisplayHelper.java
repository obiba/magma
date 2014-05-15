package org.obiba.magma.datasource.limesurvey;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.SortedSet;

import org.obiba.magma.Attribute;
import org.obiba.magma.Category;
import org.obiba.magma.Datasource;
import org.obiba.magma.Value;
import org.obiba.magma.ValueTable;
import org.obiba.magma.Variable;
import org.obiba.magma.VariableEntity;
import org.obiba.magma.VectorSource;
import org.obiba.magma.datasource.limesurvey.LimesurveyValueTable.LimesurveyQuestionVariableValueSource;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

@SuppressWarnings("UseOfSystemOutOrSystemErr")
public class DisplayHelper {

  private DisplayHelper() {

  }

  public static void display(Datasource datasource) {
    int nbVariable = 0;
    for(ValueTable table : datasource.getValueTables()) {
      nbVariable += display((LimesurveyValueTable) table);
    }
    System.out.println(nbVariable);
    System.out.println(datasource.getValueTables().size());
  }

  public static int display(final LimesurveyValueTable table) {
    List<LimesurveyQuestionVariableValueSource> variables = Lists.newArrayList(Lists
        .transform(Lists.newArrayList(table.getVariables()),
            new Function<Variable, LimesurveyQuestionVariableValueSource>() {
              @Override
              @edu.umd.cs.findbugs.annotations.SuppressWarnings("NP_PARAMETER_MUST_BE_NONNULL_BUT_MARKED_AS_NULLABLE")
              public LimesurveyQuestionVariableValueSource apply(Variable input) {
                return (LimesurveyQuestionVariableValueSource) table.getVariableValueSource(input.getName());
              }
            }));
    Collections.sort(variables, new Comparator<LimesurveyQuestionVariableValueSource>() {

      @Override
      public int compare(LimesurveyQuestionVariableValueSource o1, LimesurveyQuestionVariableValueSource o2) {
        return o1.getVariable().getName().compareTo(o2.getVariable().getName());
      }
    });
    SortedSet<VariableEntity> variableEntities = Sets.newTreeSet(table.getVariableEntities());
    for(LimesurveyQuestionVariableValueSource lvv : variables) {
      Variable v = lvv.getVariable();
      displayMetadata(v);
      displayValues(variableEntities, lvv);
    }
    return variables.size();
  }

  private static void displayValues(SortedSet<VariableEntity> variableEntities, VectorSource vectorSource) {
    for(Value value : vectorSource.getValues(variableEntities)) {
      System.out.println(value);
    }
  }

  private static void displayMetadata(Variable variable) {
    System.out.print("Var '" + variable.getName() + "' " + variable.getValueType().getName() + " ");
    for(Attribute attr : variable.getAttributes()) {
      System.out.print(attr.getName() + (attr.isLocalised() ? attr.getLocale() : "") + "=" + attr.getValue() +
          ", ");
    }
    System.out.println();
    for(Category c : variable.getCategories()) {
      System.out.print("    Cat '" + c.getName() + "' ");
      for(Attribute attr : c.getAttributes()) {
        System.out.print(" " + attr.getName() + (attr.isLocalised() ? attr.getLocale() : "") + "=" +
            attr.getValue() + ", ");
      }
      System.out.println();
    }
  }
}
