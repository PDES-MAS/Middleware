package mwgrid.middleware.exception;

public class RollbackException extends Exception {
    private static final long serialVersionUID = 1L;
    private final int fRollbackTime;
    private final long fAgentId;
    
    /**
     * Constructor
     * 
     * @param pAgentId
     *            - agent ID
     * @param pRollbackTime
     *            - roll-back time
     */
    public RollbackException(final long pAgentId, final int pRollbackTime) {
        super();
        this.fRollbackTime = pRollbackTime;
        this.fAgentId = pAgentId;
    }
    
    /**
     * @return (int) roll-back time
     */
    public int getRollbackTime() {
        return this.fRollbackTime;
    }
    
    /**
     * @return (int) agent ID
     */
    public long getAgentId() {
        return this.fAgentId;
    }
}
