package mwgrid.middleware.exception;

public class InvalidVariableException extends RuntimeException {
    private static final long serialVersionUID = 1L;
    
    /**
     * @param pFieldID
     *            - field ID
     */
    public InvalidVariableException(final int pFieldID) {
        super(String.format(
            "An error occured when accessing the requested field: %d",
            new Integer(pFieldID)));
    }
    
    /**
     * @param pException
     *            - SSV not found exception
     */
    public InvalidVariableException(final SSVNotFoundException pException) {
        super("The given field does not exist", pException);
    }
    
    /**
     * @param pError
     *            - error
     */
    public InvalidVariableException(final String pError) {
        super(pError);
    }
    
    /**
     * Exception without message
     */
    public InvalidVariableException() {
        super();
    }
}
