package testbed;

/**
 * Exception thrown during algorithm tracing operation
 */
public final class AlgorithmException extends RuntimeException {

  public void setError() {
    mError = true;
  }

  public boolean error() {
    return mError;
  }

  public AlgorithmException(Object... message) {
    mMessages = message;
  }

  private Object[] mMessages;
  private boolean mError;

}
