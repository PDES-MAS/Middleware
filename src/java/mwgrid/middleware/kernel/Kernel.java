package mwgrid.middleware.kernel;

import java.util.logging.Logger;

public final class Kernel {
    public static final String SEPARATOR = "#";
    private static final Logger LOG = Logger.getLogger(Kernel.class
        .getPackage().getName());
    private static Scheduler fScheduler;
    private static SSVHandler fSSVHandler;
    
    /**
     * Private constructor
     */
    private Kernel() {
        LOG.finest("Private Constructor");
    }
    
    /**
     * @param pSchedulerListener
     *            - scheduler listener
     * @param pEndTime
     *            - end time
     */
    public static void getSequentionalInstance(final SchedulerListener pSchedulerListener,
            final int pEndTime) {
        LOG.finest("Create scheduler");
        Kernel.fScheduler = new Scheduler(pEndTime);
        LOG.finest("Create sequential SSV handler");
        Kernel.fSSVHandler = new SequentialSSVHandler();
        LOG.finest("Register scheduler listener");
        Kernel.fScheduler.registerSchedulerListener(pSchedulerListener);
    }
    
    /**
     * @param pSchedulerListener
     *            - scheduler listener
     * @param pNumberOfAlps
     *            - number of ALPs
     * @param pNumberOfClps
     *            - number of CLPs
     * @param pStartTime
     *            - start time
     * @param pEndTime
     *            - end time
     */
    public static void getDistributedInstance(final SchedulerListener pSchedulerListener,
            final int pNumberOfAlps, final int pNumberOfClps,
            final int pStartTime, final int pEndTime) {
        LOG.finest("Create scheduler");
        Kernel.fScheduler = new Scheduler(pEndTime);
        LOG.finest("Create distributed SSV handler");
        fSSVHandler =
                new DistributedSSVHandler(pNumberOfAlps,
                        pNumberOfClps, pStartTime, pEndTime);
        LOG.finest("Register scheduler listener");
        fScheduler.registerSchedulerListener(pSchedulerListener);
    }
    
    /**
     * Starts simulation
     */
    public static void startSimulation() {
        if (Kernel.fSSVHandler.isAlp()) Kernel.fScheduler.startSimulation();
    }
    
    /**
     * Stops simulation
     */
    public static void stopSimulation() {
        Kernel.fScheduler.stopSimulation();
    }
    
    /**
     * @return (boolean) is simulation alive?
     */
    public static boolean isSimulationAlive() {
        return Kernel.fScheduler.isSimulationAlive();
    }
    
    /**
     * @return (fSSVHandler) SSV Handler
     */
    public static SSVHandler getSSVHandler() {
        return Kernel.fSSVHandler;
    }
    
    /**
     * @return (Scheduler) scheduler
     */
    public static Scheduler getScheduler() {
        return Kernel.fScheduler;
    }
}
