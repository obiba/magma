package org.obiba.magma.datasource.limesurvey;

public class LimeAnswer extends LimeLocalizableEntity implements Comparable<LimeAnswer> {

  private int sortorder;

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

  @Override
  public int compareTo(LimeAnswer o) {
    return new Integer(this.sortorder).compareTo(o.sortorder);
  }

}