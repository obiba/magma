package org.obiba.magma.datasource.hibernate.converter;

import java.util.ArrayList;
import java.util.Collection;

import javax.annotation.Nullable;

import org.obiba.magma.Category;
import org.obiba.magma.MagmaRuntimeException;
import org.obiba.magma.Variable;
import org.obiba.magma.Variable.Builder;
import org.obiba.magma.datasource.hibernate.domain.CategoryState;
import org.obiba.magma.datasource.hibernate.domain.VariableState;

public class VariableConverter extends AttributeAwareConverter implements HibernateConverter<VariableState, Variable> {

  public static VariableConverter getInstance() {
    return new VariableConverter();
  }

  private VariableConverter() {
  }

  @Nullable
  public VariableState getStateForVariable(Variable variable, HibernateMarshallingContext context) {
    for(VariableState state : context.getValueTable().getVariables()) {
      if(state.getName().equals(variable.getName())) return state;
    }
    return null;
  }

  @Override
  public VariableState marshal(Variable magmaObject, HibernateMarshallingContext context) {
    VariableState variableState = getStateForVariable(magmaObject, context);
    if(variableState == null) {
      variableState = new VariableState(context.getValueTable(), magmaObject);
      context.getValueTable().getVariables().add(variableState);
    } else {
      variableState.copyVariableFields(magmaObject);
    }

    if(variableState.getValueType() != magmaObject.getValueType()) {
      throw new MagmaRuntimeException(
          "Changing the value type of a variable is not supported. Cannot modify variable '" + magmaObject.getName() +
              "' in table '" + context.getValueTable().getName() + "'");
    }

    setAttributes(magmaObject, variableState);
    marshalCategories(magmaObject, variableState);

    return variableState;
  }

  private void marshalCategories(Variable variable, VariableState variableState) {
    // Make sure the ordering of the categories is correct
    int categoryIndex = 0;
    for(Category c : variable.getCategories()) {
      CategoryState categoryState;
      int currentCategoryIndex = variableState.getCategoryIndex(c.getName());
      if(currentCategoryIndex == -1) {
        // Category does not exist
        categoryState = new CategoryState(c.getName(), c.getCode(), c.isMissing());
        variableState.getCategories().add(categoryIndex, categoryState);
      } else {
        categoryState = variableState.getCategories().get(currentCategoryIndex);
        if(categoryIndex != currentCategoryIndex) {
          // Swap their positions
          CategoryState previousCategory = variableState.getCategories().set(categoryIndex, categoryState);
          variableState.getCategories().set(currentCategoryIndex, previousCategory);
        }
      }
      setAttributes(c, categoryState);
      categoryIndex++;
    }
    // Remaining categories needs to be deleted
    if(categoryIndex < variableState.getCategories().size()) {
      Collection<CategoryState> toDelete = new ArrayList<CategoryState>();
      for(int i = categoryIndex; i < variableState.getCategories().size(); i++) {
        toDelete.add(variableState.getCategories().get(i));
      }
      variableState.getCategories().removeAll(toDelete);
    }
  }

  @Override
  public Variable unmarshal(VariableState jpaObject, @Nullable HibernateMarshallingContext context) {
    Variable.Builder builder = Variable.Builder
        .newVariable(jpaObject.getName(), jpaObject.getValueType(), jpaObject.getEntityType());
    builder.mimeType(jpaObject.getMimeType()).occurrenceGroup(jpaObject.getOccurrenceGroup())
        .referencedEntityType(jpaObject.getReferencedEntityType()).unit(jpaObject.getUnit());
    if(jpaObject.isRepeatable()) {
      builder.repeatable();
    }

    buildAttributeAware(builder, jpaObject);
    unmarshalCategories(builder, jpaObject);
    return builder.build();
  }

  private void unmarshalCategories(Builder builder, VariableState variableState) {
    for(CategoryState categoryState : variableState.getCategories()) {
      Category.Builder categoryBuilder = Category.Builder.newCategory(categoryState.getName())
          .withCode(categoryState.getCode()).missing(categoryState.isMissing());
      buildAttributeAware(categoryBuilder, categoryState);
      builder.addCategory(categoryBuilder.build());
    }
  }

}
