package org.androiddaisyreader.testutilities;

/**
 * Custom exception to help us ensure we don't leave incomplete code undetected.
 * 
 * TODO(jharty) Check with Antony that there isn't a better exception.
 * 
 */
public class NotImplementedException extends Exception {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public NotImplementedException() {
        super();
    }

    public NotImplementedException(String detailMessage) {
        super(detailMessage);
    }

    public NotImplementedException(Throwable throwable) {
        super(throwable);
    }

    public NotImplementedException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }

}
