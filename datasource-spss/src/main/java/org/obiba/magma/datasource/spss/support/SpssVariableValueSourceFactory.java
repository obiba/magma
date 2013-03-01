/*
 * Copyright (c) 2012 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.magma.datasource.spss.support;

import java.io.IOException;
import java.util.Set;

import org.obiba.magma.Attribute;
import org.obiba.magma.Category;
import org.obiba.magma.Variable;
import org.obiba.magma.VariableValueSource;
import org.obiba.magma.VariableValueSourceFactory;
import org.obiba.magma.datasource.spss.SpssVariableValueSource;
import org.obiba.magma.type.TextType;
import org.opendatafoundation.data.spss.SPSSFile;
import org.opendatafoundation.data.spss.SPSSFileException;
import org.opendatafoundation.data.spss.SPSSNumericVariable;
import org.opendatafoundation.data.spss.SPSSStringVariable;
import org.opendatafoundation.data.spss.SPSSVariable;
import org.opendatafoundation.data.spss.SPSSVariableCategory;

import com.google.common.collect.Sets;

public class SpssVariableValueSourceFactory implements VariableValueSourceFactory {

  //
  // Data members
  //
  private final SPSSFile spssFile;

  /**
   * @param spssFile
   * @throws IOException
   * @throws SPSSFileException
   */
  public SpssVariableValueSourceFactory(SPSSFile spssFile) {
    this.spssFile = spssFile;
  }

  @Override
  public Set<VariableValueSource> createSources() {
    Set<VariableValueSource> sources = Sets.newLinkedHashSet();
    SpssVariableTypeMapper typeMapper = new SpssVariableTypeMapper();

    for(int i = 1; i < spssFile.getVariableCount(); i++) {
      SPSSVariable variable = spssFile.getVariable(i);
      Variable.Builder builder = createVariableBuilder(variable);
      builder.type(typeMapper.map(variable));

      if(variable instanceof SPSSNumericVariable) {
        initializeNumericCategories(variable, builder);
      } else if(variable instanceof SPSSStringVariable) {
        initializeStringCategories(variable, builder);
      }

      sources.add(new SpssVariableValueSource(builder.build(), variable));
    }

    return sources;
  }

  //
  // Private methods
  //

  private void initializeStringCategories(SPSSVariable variable, Variable.Builder builder) {
    if(variable.categoryMap != null) {
      for(String category : variable.categoryMap.keySet()) {
        SPSSVariableCategory spssCategory = variable.categoryMap.get(category);
        builder.addCategory(Category.Builder.newCategory(category).addAttribute("label", spssCategory.label)
            .missing(variable.isMissingValueCode(spssCategory.strValue)).build());
      }
    }
  }

  private void initializeNumericCategories(SPSSVariable variable, Variable.Builder builder) {
    if(variable.categoryMap != null) {
      for(String category : variable.categoryMap.keySet()) {
        SPSSVariableCategory spssCategory = variable.categoryMap.get(category);
        builder.addCategory(Category.Builder.newCategory(category).addAttribute("label", spssCategory.label)
            .missing(variable.isMissingValueCode(spssCategory.value)).build());
      }
    }
  }

  private Variable.Builder createVariableBuilder(SPSSVariable variable) {
    Variable.Builder builder = Variable.Builder.newVariable(variable.getName(), TextType.get(), "Participant")//
        .addAttribute(createAttribute("measure", variable.getMeasureLabel()))
        .addAttribute(createAttribute("width", variable.getLength()))
        .addAttribute(createAttribute("decimals", variable.getDecimals()))
        .addAttribute(createAttribute("shortName", variable.getShortName()))
        .addAttribute(createAttribute("format", variable.getSPSSFormat()));

    String label = variable.getLabel();

    if (label != null && !label.isEmpty()) {
      builder.addAttribute("label", label);
    }

    return builder;
  }

  private Attribute createAttribute(String attributeName, String value) {
    return Attribute.Builder.newAttribute(attributeName).withNamespace("spss").withValue(value).build();
  }

  private Attribute createAttribute(String attributeName, int value) {
    return createAttribute(attributeName, String.valueOf(value));
  }
}
