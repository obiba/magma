package org.obiba.magma;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

public class NoSuchViewException extends MagmaRuntimeException {

  private static final long serialVersionUID = -4146817951140665348L;

  @NotNull
  private final String view;

  public NoSuchViewException(@Nullable String view) {
    super("No such view exists with the specified name '" + view + "'");
    this.view = view;
  }

  public String getView() {
    return view;
  }
}
