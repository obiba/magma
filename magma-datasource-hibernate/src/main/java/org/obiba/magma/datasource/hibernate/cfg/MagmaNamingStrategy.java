package org.obiba.magma.datasource.hibernate.cfg;

import org.hibernate.AssertionFailure;
import org.hibernate.cfg.ImprovedNamingStrategy;
import org.hibernate.internal.util.StringHelper;

public class MagmaNamingStrategy extends ImprovedNamingStrategy {

  private static final long serialVersionUID = -7910076102639471779L;

  /**
   * Overridden to generate the column name: &lt;tableName&gt;_&lt;columnName&gt;
   */
  @Override
  public String foreignKeyColumnName(String propertyName, String propertyEntityName, String propertyTableName,
      String referencedColumnName) {
    String header = propertyName == null ? propertyTableName : StringHelper.unqualify(propertyName);
    if(header == null) throw new AssertionFailure("NamingStrategy not properly filled");
    return columnName(header) + "_" + referencedColumnName; // not used for backward compatibility
  }

}
