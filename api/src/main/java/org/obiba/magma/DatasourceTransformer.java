package org.obiba.magma;

public interface DatasourceTransformer {

  /**
   * Transforms the specified datasource into another.
   * 
   * @param datasource datasource
   * @return datasource resulting from the transformation original (note that this could be the same datasource object
   * that was passed in, modified in some way, or it could be a new datasource object)
   */
  public Datasource transform(Datasource datasource);
}
