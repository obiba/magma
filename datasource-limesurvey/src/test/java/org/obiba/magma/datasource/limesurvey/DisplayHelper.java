package org.obiba.magma.datasource.limesurvey;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.obiba.magma.Attribute;
import org.obiba.magma.Category;
import org.obiba.magma.Datasource;
import org.obiba.magma.ValueSet;
import org.obiba.magma.ValueTable;
import org.obiba.magma.Variable;
import org.obiba.magma.VariableEntity;
import org.obiba.magma.datasource.limesurvey.LimesurveyValueTable.LimesurveyVariableValueSource;
import org.obiba.magma.support.VariableEntityBean;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableSortedSet;
import com.google.common.collect.Lists;

public class DisplayHelper {
  public static void display(Datasource datasource) {
    int nbVariable = 0;
    for(final ValueTable table : datasource.getValueTables()) {
      System.out.println(table.getTimestamps().getLastUpdate());
      System.out.println(table.getTimestamps().getCreated());
      List<LimesurveyVariableValueSource> variables = Lists.newArrayList(Lists.transform(Lists.newArrayList(table.getVariables()), new Function<Variable, LimesurveyVariableValueSource>() {

        @Override
        public LimesurveyVariableValueSource apply(Variable input) {
          return (LimesurveyVariableValueSource) table.getVariableValueSource(input.getName());
        }
      }));
      Collections.sort(variables, new Comparator<LimesurveyVariableValueSource>() {

        @Override
        public int compare(LimesurveyVariableValueSource o1, LimesurveyVariableValueSource o2) {
          return o1.getVariable().getName().compareTo(o2.getVariable().getName());
        }
      });
      VariableEntity entity = new VariableEntityBean(table.getEntityType(), "1234");
      VariableEntity entity2 = new VariableEntityBean(table.getEntityType(), "5678");
      ValueSet valueSet = table.getValueSet(entity);
      for(LimesurveyVariableValueSource lvv : variables) {
        Variable v = lvv.getVariable();
        System.out.print("Var '" + v.getName() + "' " + v.getValueType().getName() + " ");
        for(Attribute attr : v.getAttributes()) {
          System.out.print(attr.getName() + "=" + attr.getValue() + ", ");
        }
        System.out.println(lvv.getLimesurveyVariableField());
        for(Category c : v.getCategories()) {
          System.out.print("    Cat '" + c.getName() + "' ");
          for(Attribute attr : c.getAttributes()) {
            System.out.print(" " + attr.getName() + "=" + attr.getValue() + ", ");
          }
          System.out.println();
        }

        ImmutableSortedSet<VariableEntity> sortedSet = ImmutableSortedSet.of(entity, entity2);
        // Iterable<Value> values = lvv.getValues(sortedSet);

        System.out.println(lvv.getValue(valueSet));
        // for(Value value : values) {
        // System.out.println(value);
        // }
      }
      nbVariable += variables.size();
    }
    System.out.println(nbVariable);
    System.out.println(datasource.getValueTables().size());
  }
}
