package mwgrid.middleware.exception;

public class SchedulerException extends RuntimeException {
    private static final long serialVersionUID = 1L;
    
    /**
     * @param pMessage
     *            - message
     */
    public SchedulerException(final String pMessage) {
        super(pMessage);
    }
    
    /**
     * Constructor
     */
    public SchedulerException() {
        super();
    }
}
