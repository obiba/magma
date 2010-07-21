package org.obiba.magma.js.support;

import org.obiba.magma.Attribute;
import org.obiba.magma.Category;
import org.obiba.magma.Variable;
import org.obiba.magma.support.DatasourceCopier.VariableTransformer;

/**
 * Rename the categories of the given variable with their code if this one exists.
 * 
 */
public class VariableCategoryTransformer implements VariableTransformer {

  @Override
  public Variable transform(Variable variable) {
    Variable.Builder builder = Variable.Builder.sameAs(variable, false);
    for(Category category : variable.getCategories()) {
      Category.Builder cBuilder = Category.Builder.sameAs(category);
      // would be better to push the code as an 'alias' attribute and use this attribute to rename the category
      String alias = null;
      // if (category.hasAttribute("alias")) {
      // alias = category.getAttribute("alias").getValue().toString();
      // }
      alias = category.getCode();
      if(alias != null) {
        cBuilder.name(alias);
        // back up the old name as an alias
        cBuilder.clearAttributes();
        for(Attribute attr : category.getAttributes()) {
          if(attr.getName().equals("alias")) {
            cBuilder.addAttribute("alias", category.getName());
          } else {
            cBuilder.addAttribute(attr);
          }
        }
      }
      builder.addCategory(cBuilder.build());
    }
    return builder.build();
  }

}
