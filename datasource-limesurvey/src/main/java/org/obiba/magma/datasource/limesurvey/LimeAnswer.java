package org.obiba.magma.datasource.limesurvey;

import java.util.HashMap;
import java.util.Map;

public class LimeAnswer extends LimeLocalizableEntity implements Comparable<LimeAnswer> {

  private int sortorder;

  private int scaleId;

  private LimeAnswer() {
    super();
  }

  private LimeAnswer(String name) {
    super(name);
  }

  public static LimeAnswer create() {
    return new LimeAnswer();
  }

  public static LimeAnswer create(String name) {
    return new LimeAnswer(name);
  }

  public int getSortorder() {
    return sortorder;
  }

  public void setSortorder(int sortorder) {
    this.sortorder = sortorder;
  }

  public int getScaleId() {
    return scaleId;
  }

  public void setScaleId(int scaleId) {
    this.scaleId = scaleId;
  }

  @Override
  public int compareTo(LimeAnswer o) {
    return new Integer(this.sortorder).compareTo(o.sortorder);
  }

  @Override
  public Map<String, LimeLocalizableAttributes> getImplicitLabel() {
    return implicitLabels;
  }

  public static Map<String, LimeLocalizableAttributes> implicitLabels = new HashMap<String, LimeLocalizableAttributes>() {

    private static final long serialVersionUID = -4789211495142871372L;

    {
      put("Y", LimeLocalizableAttributes.create().localizableAttribute("en", "Yes").localizableAttribute("fr", "Oui"));
      put("N", LimeLocalizableAttributes.create().localizableAttribute("en", "No").localizableAttribute("fr", "Non"));
      put("I", LimeLocalizableAttributes.create().localizableAttribute("en", "Increase").localizableAttribute("fr", "Augmenter"));
      put("S", LimeLocalizableAttributes.create().localizableAttribute("en", "Same").localizableAttribute("fr", "Sans changement"));
      put("D", LimeLocalizableAttributes.create().localizableAttribute("en", "Decrease").localizableAttribute("fr", "Diminuer"));
      put("M", LimeLocalizableAttributes.create().localizableAttribute("en", "Male").localizableAttribute("fr", "Masculin"));
      put("F", LimeLocalizableAttributes.create().localizableAttribute("en", "Female").localizableAttribute("fr", "Féminin"));
    }
  };

}