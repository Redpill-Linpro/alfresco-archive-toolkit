package org.redpill.alfresco.archive.repo.action.executor;

public class VeraPdfValidationException extends RuntimeException {

  /**
   * Constructs a {@code VeraPdfValidationException} with no detail message.
   */
  public VeraPdfValidationException() {
    super();
  }

  /**
   * Constructs a {@code VeraPdfValidationException} with the specified
   * detail message.
   *
   * @param s the detail message.
   */
  public VeraPdfValidationException(String s) {
    super(s);
  }

  /**
   * Constructs a {@code VeraPdfValidationException} with the specified
   * detail message.
   *
   * @param s the detail message.
   * @param e the exception causing this error.
   */
  public VeraPdfValidationException(String s, Exception e) {
    super(s, e);
  }
}
