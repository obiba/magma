package org.obiba.magma.support;

import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.obiba.magma.MagmaRuntimeException;

import com.google.common.base.Charsets;

/**
 * Exception to be used when parsing a datasource based on a file.
 */
@SuppressWarnings("UseOfSystemOutOrSystemErr")
public class DatasourceParsingException extends MagmaRuntimeException {

  private static final long serialVersionUID = 1L;

  private final String key;

  @Nonnull
  private final List<Object> parameters;

  private List<DatasourceParsingException> children;

  /**
   * @param message default message
   * @param messageKey message key for localization
   * @param parameters parameters to go in the localized message place holders
   */
  public DatasourceParsingException(String message, String messageKey, @Nullable Object... parameters) {
    super(message);
    key = messageKey;
    this.parameters = parameters == null ? Collections.emptyList() : new ArrayList<>(Arrays.asList(parameters));
  }

  /**
   * @param message default message
   * @param e cause exception
   * @param messageKey message key for localization
   * @param parameters parameters to go in the localized message place holders
   */
  public DatasourceParsingException(String message, Throwable e, String messageKey, @Nullable Object... parameters) {
    super(message, e);
    key = messageKey;
    this.parameters = parameters == null ? Collections.emptyList() : new ArrayList<>(Arrays.asList(parameters));
  }

  public String getKey() {
    return key;
  }

  @Nonnull
  public List<Object> getParameters() {
    return parameters;
  }

  public DatasourceParsingException addChild(DatasourceParsingException child) {
    if(child != null) {
      getChildren().add(child);
    }
    return this;
  }

  public void setChildren(Iterable<? extends DatasourceParsingException> children) {
    getChildren().clear();
    for(DatasourceParsingException child : children) {
      addChild(child);
    }
  }

  /**
   * Get the children exceptions.
   *
   * @return
   */
  public List<DatasourceParsingException> getChildren() {
    return children == null ? (children = new ArrayList<>()) : children;
  }

  /**
   * Get the exception leaves in a list.
   *
   * @return
   */
  public List<DatasourceParsingException> getChildrenAsList() {
    List<DatasourceParsingException> flat = new ArrayList<>();
    for(DatasourceParsingException child : getChildren()) {
      if(child.hasChildren()) {
        for(DatasourceParsingException subChild : child.getChildrenAsList()) {
          flat.add(subChild);
        }
      } else {
        flat.add(child);
      }
    }
    return flat;
  }

  public boolean hasChildren() {
    return getChildren().size() > 0;
  }

  public void printList() {
    printList(System.out);
  }

  public void printList(PrintStream s) {
    printList(new PrintWriter(new OutputStreamWriter(s, Charsets.UTF_8)));
  }

  public void printList(PrintWriter w) {
    if(hasChildren()) {
      for(DatasourceParsingException child : getChildrenAsList()) {
        w.println(child.getMessage());
        w.println("  key: " + child.getKey());
        w.println("  parameters (" + child.getParameters().size() + "): " + child.getParameters());
      }
    } else {
      w.println(getMessage());
    }
    w.flush();
  }

  @SuppressWarnings("UnusedDeclaration")
  public void printTree() {
    print(this, "");
  }

  private void print(DatasourceParsingException e, String indent) {
    if(e.hasChildren()) {
      System.out.println(indent + e.getMessage());
      for(DatasourceParsingException child : e.getChildren()) {
        print(child, indent + "\t");
      }
    } else {
      System.out.println(indent + e.getMessage());
    }
  }
}
