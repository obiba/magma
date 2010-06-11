package org.obiba.magma.audit.support;

import org.obiba.magma.support.DatasourceCopier.DatasourceCopyValueSetEventListener;

public interface CopyAuditor extends DatasourceCopyValueSetEventListener {

  public void startAuditing();

  public void completeAuditing();

}
