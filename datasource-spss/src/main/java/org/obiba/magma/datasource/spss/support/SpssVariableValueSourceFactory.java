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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.obiba.magma.Attribute;
import org.obiba.magma.Category;
import org.obiba.magma.Value;
import org.obiba.magma.ValueType;
import org.obiba.magma.Variable;
import org.obiba.magma.VariableValueSource;
import org.obiba.magma.VariableValueSourceFactory;
import org.obiba.magma.datasource.spss.SpssVariableValueSource;
import org.obiba.magma.type.TextType;
import org.opendatafoundation.data.spss.SPSSFile;
import org.opendatafoundation.data.spss.SPSSFileException;
import org.opendatafoundation.data.spss.SPSSNumericVariable;
import org.opendatafoundation.data.spss.SPSSVariable;
import org.opendatafoundation.data.spss.SPSSVariableCategory;

import com.google.common.base.Strings;
import com.google.common.collect.Sets;

import static org.obiba.magma.datasource.spss.support.CharacterSetValidator.validate;

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

    for(int i = 1; i < spssFile.getVariableCount(); i++) {
      SPSSVariable spssVariable = spssFile.getVariable(i);
      try {
        sources.add(new SpssVariableValueSource(createVariableBuilder(i, spssVariable), spssVariable));
      } catch(SpssInvalidCharacterException e) {
        throw new SpssDatasourceParsingException("Failed to create variable", spssVariable.getName(), i + 1,
            "InvalidCharsetCharacter", i + 1);
      }
    }

    return sources;
  }

  //
  // Private methods
  //

  private void initializeCategories(int variableIndex, SPSSVariable variable, ValueType valueType,
      Variable.Builder builder) throws SpssInvalidCharacterException {
    if(variable.categoryMap != null) {
      for(String category : variable.categoryMap.keySet()) {
        Value categoryName = new SpssCategoryNameValueFactory(category, variableIndex, variable, valueType).create();
        SPSSVariableCategory spssCategory = variable.categoryMap.get(category);
        validate(spssCategory.label);
        builder.addCategory(
            Category.Builder.newCategory(categoryName.getValue().toString()).addAttribute("label", spssCategory.label)
                .missing(isCategoryValueMissingCode(variable, spssCategory)).build());
      }
    }
  }

  private boolean isCategoryValueMissingCode(SPSSVariable spssVariable, SPSSVariableCategory spssCategory) {
    if(spssVariable instanceof SPSSNumericVariable) {
      return spssVariable.isMissingValueCode(spssCategory.value);
    }

    return spssVariable.isMissingValueCode(spssCategory.strValue);
  }

  @SuppressWarnings("ChainOfInstanceofChecks")
  private Variable createVariableBuilder(int variableIndex, @Nonnull SPSSVariable spssVariable)
      throws SpssInvalidCharacterException {
    String variableName = spssVariable.getName();
    validate(variableName);
    Variable.Builder builder = Variable.Builder.newVariable(variableName, TextType.get(), "Participant");
    addAttributes(builder, spssVariable);
    addLabel(builder, spssVariable);
    ValueType valueType = SpssVariableTypeMapper.map(spssVariable);
    builder.type(valueType);
    initializeCategories(variableIndex, spssVariable, valueType, builder);

    return builder.build();
  }

  private void addLabel(@Nonnull Variable.Builder builder, @Nonnull SPSSVariable spssVariable)
      throws SpssInvalidCharacterException {
    String label = spssVariable.getLabel();

    if(!Strings.isNullOrEmpty(label)) {
      validate(label);
      builder.addAttribute("label", label);
    }
  }

  private void addAttributes(Variable.Builder builder, @Nonnull SPSSVariable spssVariable)
      throws SpssInvalidCharacterException {
    builder.addAttribute(createAttribute("measure", spssVariable.getMeasureLabel()))
        .addAttribute(createAttribute("width", spssVariable.getLength()))
        .addAttribute(createAttribute("decimals", spssVariable.getDecimals()))
        .addAttribute(createAttribute("shortName", spssVariable.getShortName()))
        .addAttribute(createAttribute("format", spssVariable.getSPSSFormat()));
  }

  private Attribute createAttribute(String attributeName, @Nullable String value) throws SpssInvalidCharacterException {
    validate(value);
    return Attribute.Builder.newAttribute(attributeName).withNamespace("spss").withValue(value).build();
  }

  private Attribute createAttribute(String attributeName, int value) throws SpssInvalidCharacterException {
    return createAttribute(attributeName, String.valueOf(value));
  }
}
