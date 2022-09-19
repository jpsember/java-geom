package testbed;

/**
 * Exception thrown during algorithm tracing operation
 */
public final class AlgorithmException extends RuntimeException {

  public AlgorithmException(String message, Throwable cause) {
    super(message, cause);
  }

  public AlgorithmException(String message, boolean error) {
    this(message, null);
    mError = error;
  }

  public boolean error() {
    return mError;
  }

  private boolean mError;

}
