/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.magma.datasource.hibernate.converter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

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
      if (state == null) continue; //MAGMA-255
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
    variableState.setUpdated(new Date());

    return variableState;
  }

  private void marshalCategories(Variable variable, VariableState variableState) {
    // Make sure the ordering of the categories is correct
    int categoryIndex = 0;
    for(Category category : variable.getCategories()) {
      CategoryState categoryState;
      int currentCategoryIndex = variableState.getCategoryIndex(category.getName());
      if(currentCategoryIndex == -1) {
        // Category does not exist
        categoryState = new CategoryState(category.getName(), category.getCode(), category.isMissing());
        variableState.addCategory(categoryIndex, categoryState);
      } else {
        categoryState = variableState.getCategories().get(currentCategoryIndex);
        if(categoryIndex != currentCategoryIndex) {
          // Swap their positions
          CategoryState previousCategory = variableState.getCategories().set(categoryIndex, categoryState);
          variableState.getCategories().set(currentCategoryIndex, previousCategory);
        }
      }
      categoryState.setMissing(category.isMissing());
      categoryState.setUpdated(new Date());
      setAttributes(category, categoryState);
      categoryIndex++;
    }
    // Remaining categories needs to be deleted
    deleteRemainingCategories(variableState, categoryIndex);
  }

  private void deleteRemainingCategories(VariableState variableState, int categoryIndex) {
    int nbCategories = variableState.getCategories().size();
    if(categoryIndex < nbCategories) {
      Collection<CategoryState> toDelete = new ArrayList<>();
      for(int i = categoryIndex; i < nbCategories; i++) {
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
        .referencedEntityType(jpaObject.getReferencedEntityType()).unit(jpaObject.getUnit())
        .index(jpaObject.getIndex());

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
