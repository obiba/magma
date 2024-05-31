/*
 * Copyright (c) 2020 OBiBa. All rights reserved.
 *  
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *  
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.magma.support;

import org.obiba.magma.*;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Locale;
import java.util.Objects;

public class VariableHelper {

  public static boolean isModified(Variable compared, Variable with) {
    return
        isModified(compared.getValueType(), with.getValueType()) ||
        isModified(compared.getMimeType(), with.getMimeType()) ||
        isModified(compared.getOccurrenceGroup(), with.getOccurrenceGroup()) ||
        isModified(compared.getReferencedEntityType(), with.getReferencedEntityType()) ||
        isModified(compared.getUnit(), with.getUnit()) ||
        compared.isRepeatable() != with.isRepeatable() ||
        compared.getIndex() != with.getIndex() ||
        areCategoriesModified(compared.getCategories(), with.getCategories()) ||
        areAttributesModified(compared.getAttributes(), with.getAttributes());
  }

  public static boolean isModified(ValueType compared, ValueType with) {
    return !compared.equals(with);
  }

  public static boolean isModified(@Nullable String compared, @Nullable String with) {
    if(compared == null && with == null) return false;
    if((compared == null || compared.isEmpty()) && (with == null || with.isEmpty())) return false;
    if(compared != null && with == null) return true;
    return !(compared != null && compared.equals(with.replace("\r", "")));
  }

  public static boolean areCategoriesModified(Collection<Category> compared, Collection<Category> with) {
    if(compared == null && with == null) return false;
    if((compared == null || compared.isEmpty()) && (with == null || with.isEmpty())) return false;
    if(compared == null && with != null || compared != null && with == null) return true;
    if(compared != null && with != null && compared.size() != with.size()) return true;

    if(compared != null && with != null) {
      int comparedPos = 0;
      for(Category comparedCat : compared) {
        boolean found = false;
        int withPos = 0;
        for(Category withCat : with) {
          if(comparedCat.getName().equals(withCat.getName())) {
            if(isModified(comparedCat, withCat)) return true;
            found = true;
            break;
          }
          withPos++;
        }
        if(!found) return true;
        // check position
        if (comparedPos != withPos) return true;
        comparedPos++;
      }
    }
    return false;
  }

  public static boolean isModified(Category compared, Category with) {
    return compared.isMissing() != with.isMissing() ||
        areAttributesModified(compared.getAttributes(), with.getAttributes());
  }

  public static boolean areAttributesModified(Collection<Attribute> compared, Collection<Attribute> with) {
    if(compared == null && with == null) return false;
    if((compared == null || compared.isEmpty()) && (with == null || with.isEmpty())) return false;
    if(compared == null || with == null) return true;
    if(compared.size() != with.size()) return true;

    for(Attribute comparedAttr : compared) {
      boolean found = false;
      for(Attribute withAttr : with) {
        if(isSameAttribute(comparedAttr, withAttr)) {
          if(isStringValueModified(comparedAttr.getValue(), withAttr.getValue())) return true;
          found = true;
        }
      }
      if(!found) return true;
    }
    return false;
  }

  public static boolean isStringValueModified(Value compared, Value with) {
    if(compared == null && with == null) return false;
    String comparedStr = compared == null || compared.isNull() ? null : compared.toString();
    String withStr = with == null || with.isNull() ? null : with.toString();
    return isModified(comparedStr, withStr);
  }

  public static boolean isSameAttribute(Attribute compared, Attribute with) {
    if(!compared.getName().equals(with.getName())) return false;
    Locale comparedLocale = compared.isLocalised() ? compared.getLocale() : null;
    Locale withLocale = with.isLocalised() ? with.getLocale() : null;
    return Objects.equals(comparedLocale, withLocale);
  }
  
}
