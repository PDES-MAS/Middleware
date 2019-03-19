package mwgrid.middleware.kernel;


public interface SchedulerListener {
    /**
     * @param pAgentId - agent ID
     * @param pTime - time
     * @param pReport - report string
     */
    void collectReport(long pAgentId, int pTime, String pReport);
}
