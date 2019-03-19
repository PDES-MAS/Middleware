package mwgrid.middleware.exception;

public class InvalidTypeException extends RuntimeException {
    private static final long serialVersionUID = 1L;
    
    /**
     * @param pMessage
     *            - message
     */
    public InvalidTypeException(final String pMessage) {
        super(pMessage);
    }
    
    /**
     * Constructor
     */
    public InvalidTypeException() {
        super();
    }
}
