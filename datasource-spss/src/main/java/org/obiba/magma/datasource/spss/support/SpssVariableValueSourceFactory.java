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

import java.io.File;
import java.io.IOException;
import java.util.Locale;
import java.util.Set;

import org.obiba.magma.Attribute;
import org.obiba.magma.Category;
import org.obiba.magma.Variable;
import org.obiba.magma.VariableValueSource;
import org.obiba.magma.VariableValueSourceFactory;
import org.obiba.magma.datasource.spss.SpssVariableValueSource;
import org.obiba.magma.type.DateTimeType;
import org.obiba.magma.type.IntegerType;
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
  private SPSSFile spssFile;

  /**
   * @param spssFile
   * @throws IOException
   * @throws SPSSFileException
   */
  public SpssVariableValueSourceFactory(File file) throws IOException, SPSSFileException {
    this.spssFile = new SPSSFile(file);
    spssFile.loadMetadata();
  }

  @Override
  public Set<VariableValueSource> createSources() {
    Set<VariableValueSource> sources = Sets.newLinkedHashSet();

    for(int i = 0; i < spssFile.getVariableCount(); i++) {
      SPSSVariable variable = spssFile.getVariable(i);
      Variable.Builder builder = createVariableBuilder(variable);

      if(variable instanceof SPSSNumericVariable) {
        initializeNumericValueType(variable, builder);
        initializeNumericCategories(variable, builder);
      } else if(variable instanceof SPSSStringVariable) {
        initializeStringCategories(variable, builder);
      }

      sources.add(new SpssVariableValueSource(builder.build()));
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

  private void initializeNumericValueType(SPSSVariable variable, Variable.Builder builder) {
    if(variable.getSPSSFormat().toLowerCase().contains("date")) {
      builder.type(DateTimeType.get());
    } else {
      builder.type(IntegerType.get());
    }
  }

  private Variable.Builder createVariableBuilder(SPSSVariable variable) {
    return Variable.Builder.newVariable(variable.getName(), TextType.get(), "Participant")//
        .addAttribute("label", variable.getLabel())//
        .addAttribute(
            Attribute.Builder.newAttribute("measure").withNamespace("spss").withValue(variable.getMeasureLabel())
                .build()).addAttribute(
            Attribute.Builder.newAttribute("shortName").withNamespace("spss").withValue(variable.getShortName())
                .build()).addAttribute(
            Attribute.Builder.newAttribute("format").withNamespace("spss").withValue(variable.getSPSSFormat()).build());
  }
}
