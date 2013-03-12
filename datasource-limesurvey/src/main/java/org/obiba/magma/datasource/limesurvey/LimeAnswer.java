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
    return Integer.valueOf(this.sortorder).compareTo(o.sortorder);
  }

  @Override
  public boolean equals(Object obj) {
    if(this == obj) return true;
    if(obj instanceof LimeAnswer == false) return false;

    if(this.scaleId != ((LimeAnswer) obj).scaleId) return false;
    return compareTo((LimeAnswer) obj) == 0;
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash = 31 * hash + scaleId;
    hash = 31 * hash + sortorder;
    return hash;
  }

  @Override
  public Map<String, LimeAttributes> getImplicitLabel() {
    return implicitLabels;
  }

  public static final Map<String, LimeAttributes> implicitLabels = new HashMap<String, LimeAttributes>() {

    private static final long serialVersionUID = -4789211495142871372L;

    {
      put("-oth-", LimeAttributes.create().attribute("label:en", "Other").attribute("label:fr", "Autre"));
      put("Y", LimeAttributes.create().attribute("label:en", "Yes").attribute("label:fr", "Oui"));
      put("N", LimeAttributes.create().attribute("label:en", "No").attribute("label:fr", "Non"));
      put("U", LimeAttributes.create().attribute("label:en", "Uncertain").attribute("label:fr", "Incertain"));
      put("I", LimeAttributes.create().attribute("label:en", "Increase").attribute("label:fr", "Augmenter"));
      put("S", LimeAttributes.create().attribute("label:en", "Same").attribute("label:fr", "Sans changement"));
      put("D", LimeAttributes.create().attribute("label:en", "Decrease").attribute("label:fr", "Diminuer"));
      put("M", LimeAttributes.create().attribute("label:en", "Male").attribute("label:fr", "Masculin"));
      put("F", LimeAttributes.create().attribute("label:en", "Female").attribute("label:fr", "Féminin"));
    }
  };

}