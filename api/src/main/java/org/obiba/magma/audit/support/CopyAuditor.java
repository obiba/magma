package org.obiba.magma.audit.support;

import org.obiba.magma.support.DatasourceCopier.DatasourceCopyValueSetEventListener;

public interface CopyAuditor extends DatasourceCopyValueSetEventListener {

  void startAuditing();

  void completeAuditing();

}
