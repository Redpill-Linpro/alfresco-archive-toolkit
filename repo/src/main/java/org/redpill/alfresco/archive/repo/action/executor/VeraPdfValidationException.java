package org.redpill.alfresco.archive.repo.action.executor;

public class VeraPdfValidationException extends RuntimeException {

  private String validationResult;

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

  /**
   * Constructs a {@code VeraPdfValidationException} with the specified
   * detail message.
   *
   * @param s      the detail message.
   * @param result The validation result detail
   */
  public VeraPdfValidationException(String s, String result) {
    super(s);
    this.validationResult = result;
  }

  /**
   * Constructs a {@code VeraPdfValidationException} with the specified
   * detail message.
   *
   * @param s      the detail message.
   * @param result The validation result detail
   * @param e      the exception causing this error.
   */
  public VeraPdfValidationException(String s, String result, Exception e) {
    super(s, e);
    this.validationResult = result;
  }

  public String getValidationResult() {
    return validationResult;
  }
}
