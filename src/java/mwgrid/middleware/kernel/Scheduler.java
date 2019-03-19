package mwgrid.middleware.kernel;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import mwgrid.common.FilePrint;
import mwgrid.middleware.distributedobject.DistributedObject;
import mwgrid.middleware.exception.RollbackException;
import mwgrid.middleware.exception.SchedulerException;

public class Scheduler {
    final class SchedulerThread extends Thread {
        private boolean fIsStopRequested;
        
        /**
         * Constructor
         */
        SchedulerThread() {
            LOG.finest("Constructor");
            this.fIsStopRequested = false;
        }
        
        @Override
        public void run() {
            while (!this.isStopRequested()) {
                handleBufferUpdate();
                synchronized (Scheduler.this.fDistributedObjectList) {
                    handleCallStep();
                    handleReportCollection();
                    handleGVTUpdate();
                    handleStopSimulation();
                    if (LOG.isLoggable(Level.FINE)) {
                        long totalMemory = 0;
                        long usedMemory = 0;
                        long freeMemory = 0;
                        try {
                            final Process process =
                                    Runtime.getRuntime().exec("free -b");
                            final BufferedReader inputStream =
                                    new BufferedReader(new InputStreamReader(
                                            process.getInputStream()));
                            inputStream.readLine();
                            final String[] values =
                                    inputStream.readLine().split("\\s+");
                            totalMemory = Long.parseLong(values[1]);
                            usedMemory = Long.parseLong(values[2]);
                            freeMemory = Long.parseLong(values[3]);
                            inputStream.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        FilePrint
                                .printToFile(
                                    Kernel.getSSVHandler().getRank(),
                                    FilePrint.Filename.SCHED_MEM,
                                    +(Runtime.getRuntime().totalMemory() - Runtime
                                            .getRuntime().freeMemory())
                                            + "\t"
                                            + Runtime.getRuntime()
                                                    .maxMemory()
                                            + "\t"
                                            + totalMemory
                                            + "\t"
                                            + usedMemory
                                            + "\t"
                                            + freeMemory);
                    }
                }
            }
        }
        
        /**
         * Request stop
         */
        public synchronized void requestStop() {
            LOG.finest("Stop requested");
            this.fIsStopRequested = true;
        }
        
        /**
         * @return (boolean) is stop requested?
         */
        private synchronized boolean isStopRequested() {
            LOG.finest("Is stop requested?");
            return this.fIsStopRequested;
        }
        
        /**
         * Handle buffer update
         */
        private void handleBufferUpdate() {
            LOG.finest("Scheduler buffer update on: "
                    + Kernel.getSSVHandler().getRank());
            try {
                synchronized (Scheduler.this.fDistributedObjectList) {
                    synchronized (Scheduler.this.fDistributedObjectAddBuffer) {
                        Scheduler.this.fDistributedObjectList
                                .addAll(Scheduler.this.fDistributedObjectAddBuffer);
                        Scheduler.this.fDistributedObjectAddBuffer.clear();
                    }
                    synchronized (Scheduler.this.fDistributedObjectRemoveBuffer) {
                        Scheduler.this.fDistributedObjectList
                                .removeAll(Scheduler.this.fDistributedObjectRemoveBuffer);
                        Scheduler.this.fDistributedObjectRemoveBuffer.clear();
                    }
                }
            } finally {
                LOG.finest("Finish scheduler buffer update on: "
                        + Kernel.getSSVHandler().getRank());
            }
        }
        
        /**
         * Handle call to step
         */
        private void handleCallStep() {
            LOG.finest("Scheduler call step on: "
                    + Kernel.getSSVHandler().getRank());
            if (LOG.isLoggable(Level.FINEST)) {
                FilePrint.printToFile(Kernel.getSSVHandler().getRank(),
                    FilePrint.Filename.TRACE, "Scheduler; handle call step");
            }
            try {
                for (final DistributedObject distributedObject : Scheduler.this.fDistributedObjectList) {
                    handleRollbackCallback(distributedObject);
                    distributedObject.resetRolledBackFlag();
                    distributedObject.increaseTime();
                    if (Scheduler.this.fEndTime + 1 > distributedObject
                            .getTime()) {
                        long startTime = 0;
                        if (LOG.isLoggable(Level.FINE)) {
                            startTime = System.currentTimeMillis();
                            LOG.finest("Call step on agent: "
                                    + distributedObject.getObjectId()
                                    + ", at time: "
                                    + distributedObject.getTime());
                        }
                        try {
                            distributedObject.step();
                        } catch (final RollbackException e) {
                            if (LOG.isLoggable(Level.FINEST)) {
                                FilePrint
                                        .printToFile(Kernel.getSSVHandler()
                                                .getRank(),
                                            FilePrint.Filename.TRACE,
                                            "Scheduler; Rollback exception during step!");
                            }
                            handleRollbackException(e, distributedObject);
                            continue;
                        } finally {
                            LOG.finest("Finish call step on agent: "
                                    + distributedObject.getObjectId()
                                    + ", at time: "
                                    + distributedObject.getTime());
                            if (LOG.isLoggable(Level.FINE)) {
                                final long duration =
                                        System.currentTimeMillis()
                                                - startTime;
                                FilePrint.printToFile(
                                    Kernel.getSSVHandler().getRank(),
                                    FilePrint.Filename.SCHED_STEP,
                                    duration + "\t"
                                            + distributedObject.getObjectId()
                                            + "\t"
                                            + distributedObject.getTime());
                            }
                        }
                    } else {
                        try {
                            LOG.finest("Ping agent: "
                                    + distributedObject.getObjectId()
                                    + ", at time: "
                                    + distributedObject.getTime()
                                    + ", end time: "
                                    + Scheduler.this.fEndTime);
                            distributedObject.getClassTypeId();
                        } catch (final RollbackException e) {
                            if (LOG.isLoggable(Level.FINEST)) {
                                FilePrint
                                        .printToFile(Kernel.getSSVHandler()
                                                .getRank(),
                                            FilePrint.Filename.TRACE,
                                            "Scheduler; Rollback exception during ping!");
                            }
                            handleRollbackException(e, distributedObject);
                            continue;
                        }
                    }
                    Kernel.getSSVHandler().setGVTTime(
                        distributedObject.getObjectId(),
                        distributedObject.getTime());
                }
            } finally {
                LOG.finest("Finish Scheduler call step on: "
                        + Kernel.getSSVHandler().getRank());
                if (LOG.isLoggable(Level.FINEST)) {
                    FilePrint.printToFile(Kernel.getSSVHandler().getRank(),
                        FilePrint.Filename.TRACE,
                        "Scheduler; Finished call step");
                }
            }
        }
        
        /**
         * Handle report collection
         */
        private void handleReportCollection() {
            LOG.finest("Report collection on: "
                    + Kernel.getSSVHandler().getRank());
            if (LOG.isLoggable(Level.FINEST)) {
                FilePrint.printToFile(Kernel.getSSVHandler().getRank(),
                    FilePrint.Filename.TRACE,
                    "Scheduler; handle report collection");
            }
            try {
                synchronized (Scheduler.this.fSchedulerListenerList) {
                    for (final SchedulerListener listener : Scheduler.this.fSchedulerListenerList) {
                        for (final DistributedObject distributedObject : Scheduler.this.fDistributedObjectList) {
                            handleRollbackCallback(distributedObject);
                            final boolean hasRolledBack =
                                    distributedObject.resetRolledBackFlag();
                            if (!hasRolledBack
                                    && Scheduler.this.fEndTime + 1 > distributedObject
                                            .getTime()) {
                                LOG.finest("Call report on agent: "
                                        + distributedObject.getObjectId()
                                        + ", at time: "
                                        + distributedObject.getTime());
                                final long objectId =
                                        distributedObject.getObjectId();
                                final int time = distributedObject.getTime();
                                try {
                                    final String report =
                                            distributedObject.report();
                                    LOG.finest("Call collectReport for agent: "
                                            + objectId + ", at time: " + time);
                                    listener.collectReport(objectId, time,
                                        report);
                                    LOG.finest("Finish call report on agent: "
                                            + distributedObject.getObjectId()
                                            + ", at time: "
                                            + distributedObject.getTime()
                                            + ", endtime: "
                                            + Scheduler.this.fEndTime);
                                } catch (RollbackException e) {
                                    if (LOG.isLoggable(Level.FINEST)) {
                                        FilePrint
                                                .printToFile(Kernel
                                                        .getSSVHandler()
                                                        .getRank(),
                                                    FilePrint.Filename.TRACE,
                                                    "Scheduler; Rollback exception during report!");
                                    }
                                    handleRollbackException(e,
                                        distributedObject);
                                    // Reset rolled back flag so it isn't
                                    // ignored
                                    // Next report cycle
                                    distributedObject.resetRolledBackFlag();
                                    // Break loop for next cycle
                                    continue;
                                }
                            } else {
                                LOG.finest("Ignoring agent: "
                                        + distributedObject.getObjectId()
                                        + ", at time: "
                                        + distributedObject.getTime());
                            }
                            Kernel.getSSVHandler().setGVTTime(
                                distributedObject.getObjectId(),
                                distributedObject.getTime());
                        }
                    }
                }
            } finally {
                LOG.finest("Finished report collection on: "
                        + Kernel.getSSVHandler().getRank());
            }
        }
        
        /**
         * Handle GVT update
         */
        private void handleGVTUpdate() {
            if (LOG.isLoggable(Level.FINE)) {
                LOG.finer("Scheduler clean to GVT on: "
                        + Kernel.getSSVHandler().getRank());
                if (LOG.isLoggable(Level.FINEST)) {
                    FilePrint.printToFile(Kernel.getSSVHandler().getRank(),
                        FilePrint.Filename.TRACE,
                        "Scheduler; handle GVT update");
                }
            }
            try {
                for (final DistributedObject distributedObject : Scheduler.this.fDistributedObjectList) {
                    final int globalVariableTime =
                            Kernel.getSSVHandler().getGVTTime();
                    if (globalVariableTime != Scheduler.this.fGVT) {
                        LOG.finest("Clean to GVT: " + globalVariableTime
                                + ", on agent: "
                                + distributedObject.getObjectId()
                                + ", at time: " + distributedObject.getTime()
                                + ", on: " + Kernel.getSSVHandler().getRank());
                        distributedObject.cleanToGVT(globalVariableTime - 1);
                    }
                }
            } finally {
                LOG.finest("Finish scheduler clean to GVT on: "
                        + Kernel.getSSVHandler().getRank());
            }
        }
        
        /**
         * Handle stopping the simulation
         */
        private void handleStopSimulation() {
            if (LOG.isLoggable(Level.FINEST)) {
                FilePrint.printToFile(Kernel.getSSVHandler().getRank(),
                    FilePrint.Filename.TRACE,
                    "Scheduler; handle stop simulation");
            }
            Scheduler.this.fGVTCounter++;
            LOG.finest("GVT counter: " + Scheduler.this.fGVTCounter
                    + ", on: " + Kernel.getSSVHandler().getRank());
            if (Scheduler.this.fGVTCounter == Scheduler.GVT_INTERVAL) {
                Scheduler.this.fGVTCounter = 0;
                LOG.finest("Sending GVT message because of counter on: "
                        + Kernel.getSSVHandler().getRank());
                Kernel.getSSVHandler().sendGVTMessage();
                LOG.finest("Send GVT message on: "
                        + Kernel.getSSVHandler().getRank());
            }
            int lvt = Integer.MAX_VALUE;
            for (final DistributedObject distributedObject : Scheduler.this.fDistributedObjectList) {
                if (lvt > distributedObject.getTime())
                    lvt = distributedObject.getTime();
            }
            if (lvt > Scheduler.this.fEndTime) {
                LOG.finest("Sending GVT message because of LVT on: "
                        + Kernel.getSSVHandler().getRank());
                Kernel.getSSVHandler().sendGVTMessage();
                LOG.finest("Send GVT message on: "
                        + Kernel.getSSVHandler().getRank());
                try {
                    LOG.finest("Wait of one second");
                    // Do nothing for 1000 miliseconds
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            Scheduler.this.fGVT = Kernel.getSSVHandler().getGVTTime();
            LOG.finest("GVT on: " + Kernel.getSSVHandler().getRank()
                    + ", is now: " + Scheduler.this.fGVT);
            if (Scheduler.this.fGVT >= Scheduler.this.fEndTime) {
                LOG.finest("GVT: " + Scheduler.this.fGVT + ", end time: "
                        + Scheduler.this.fEndTime + ", now requesting stop");
                this.requestStop();
            }
        }
        
        /**
         * @param pDistributedObject
         *            - distributed object
         */
        private void handleRollbackCallback(
                final DistributedObject pDistributedObject) {
            if (LOG.isLoggable(Level.FINEST)) {
                FilePrint.printToFile(
                    Kernel.getSSVHandler().getRank(),
                    FilePrint.Filename.TRACE,
                    "Handle rollback callback; agent: "
                            + pDistributedObject.getObjectId() + ", time: "
                            + pDistributedObject.getTime());
            }
            final long objectId = pDistributedObject.getObjectId();
            if (Kernel.getSSVHandler().isInRollbackMap(objectId)) {
                final int rollbackTime =
                        Kernel.getSSVHandler().getRollbackMapTime(objectId);
                handleRollback(pDistributedObject, rollbackTime);
            }
        }
        
        /**
         * @param pException
         *            - roll-back exception
         * @param pRollbackObject
         *            - roll-back object
         */
        private void handleRollbackException(
                final RollbackException pException,
                final DistributedObject pRollbackObject) {
            if (LOG.isLoggable(Level.FINE)) {
                if (LOG.isLoggable(Level.FINEST)) {
                    FilePrint.printToFile(Kernel.getSSVHandler().getRank(),
                        FilePrint.Filename.TRACE,
                        "Handle rollback exception; agent: "
                                + pRollbackObject.getObjectId() + ", time: "
                                + pRollbackObject.getTime());
                }
                LOG.finest("Caught rollback exception on agent: "
                        + pException.getAgentId() + ", at time: "
                        + pRollbackObject.getTime() + ", to time: "
                        + pException.getRollbackTime());
            }
            assert pRollbackObject.getObjectId() == pException.getAgentId() : "Roll-back exception called not on current agent";
            handleRollback(pRollbackObject, pException.getRollbackTime());
            LOG.finest("Finished caught rollback exception on agent: "
                    + pException.getAgentId() + ", at time: "
                    + pRollbackObject.getTime() + ", to time: "
                    + pException.getRollbackTime());
        }
        
        /**
         * @param pRollbackObject
         *            - distributed object to roll-back
         * @param pRollbackTime
         *            - time to roll-back to
         */
        private void handleRollback(final DistributedObject pRollbackObject,
                final int pRollbackTime) {
            if (pRollbackTime <= pRollbackObject.getTime()) {
                long startTime = 0;
                if (LOG.isLoggable(Level.FINE)) {
                    startTime = System.currentTimeMillis();
                    LOG.finest("Rollback agent: "
                            + pRollbackObject.getObjectId() + ", from time: "
                            + pRollbackObject.getTime() + ", to time: "
                            + (pRollbackTime - 1));
                }
                try {
                    pRollbackObject.rollBack(pRollbackTime - 1);
                } finally {
                    if (LOG.isLoggable(Level.FINE)) {
                        final long duration =
                                System.currentTimeMillis() - startTime;
                        LOG.finest("Finish rollback agent: "
                                + pRollbackObject.getObjectId() + ", time: "
                                + pRollbackObject.getTime());
                        if (LOG.isLoggable(Level.FINEST)) {
                            FilePrint.printToFile(
                                Kernel.getSSVHandler().getRank(),
                                FilePrint.Filename.TRACE,
                                "Rollback handled; agent: "
                                        + pRollbackObject.getObjectId()
                                        + ", time: "
                                        + pRollbackObject.getTime()
                                        + ", duration: " + duration);
                        }
                    }
                }
            } else {
                if (LOG.isLoggable(Level.FINE)) {
                    LOG.finest("Rollback time greater than agent time, rollback ignored on agent: "
                            + pRollbackObject.getObjectId()
                            + ", from time: "
                            + pRollbackObject.getTime()
                            + ", to time: "
                            + pRollbackTime);
                    if (LOG.isLoggable(Level.FINEST)) {
                        FilePrint.printToFile(
                            Kernel.getSSVHandler().getRank(),
                            FilePrint.Filename.TRACE,
                            "Rollback ignored; agent: "
                                    + pRollbackObject.getObjectId()
                                    + ", time: " + pRollbackObject.getTime());
                    }
                }
            }
        }
    }
    
    static final Logger LOG = Logger.getLogger(Scheduler.class.getPackage()
            .getName());
    static final int GVT_INTERVAL = 10;
    final int fEndTime;
    List<DistributedObject> fDistributedObjectList;
    List<DistributedObject> fDistributedObjectAddBuffer;
    List<DistributedObject> fDistributedObjectRemoveBuffer;
    List<SchedulerListener> fSchedulerListenerList;
    int fGVTCounter;
    int fGVT;
    private final SchedulerThread fSchedulerThread;
    
    /**
     * Constructor
     * 
     * @param pEndTime
     *            - end time
     */
    public Scheduler(final int pEndTime) {
        this.fGVTCounter = 0;
        this.fGVT = 0;
        this.fEndTime = pEndTime;
        this.fDistributedObjectList =
                Collections
                        .synchronizedList(new ArrayList<DistributedObject>());
        this.fDistributedObjectAddBuffer =
                Collections
                        .synchronizedList(new ArrayList<DistributedObject>());
        this.fDistributedObjectRemoveBuffer =
                Collections
                        .synchronizedList(new ArrayList<DistributedObject>());
        this.fSchedulerListenerList =
                Collections
                        .synchronizedList(new ArrayList<SchedulerListener>());
        this.fSchedulerThread = new SchedulerThread();
    }
    
    /**
     * Start the simulation
     */
    public void startSimulation() {
        LOG.finest("Start simulation");
        synchronized (this.fSchedulerListenerList) {
            if (this.fSchedulerListenerList.isEmpty())
                throw new SchedulerException();
        }
        synchronized (this.fDistributedObjectList) {
            synchronized (this.fDistributedObjectAddBuffer) {
                if (this.fDistributedObjectList.isEmpty()
                        && this.fDistributedObjectAddBuffer.isEmpty())
                    throw new SchedulerException();
            }
        }
        this.fSchedulerThread.start();
    }
    
    /**
     * Stop the simulation
     */
    public void stopSimulation() {
        this.fSchedulerThread.requestStop();
    }
    
    /**
     * @return true if the simulation is alive, false otherwise
     */
    public boolean isSimulationAlive() {
        return this.fSchedulerThread.isAlive();
    }
    
    /**
     * @return list of registered distributed objects
     */
    public List<DistributedObject> getRegisteredDistributedObjects() {
        return Collections.unmodifiableList(this.fDistributedObjectList);
    }
    
    /**
     * Update the registered distributed objects
     */
    public void updateRegisteredDistributedObjects() {
        LOG.finest("Update registered distributed objects");
        this.fDistributedObjectList.addAll(this.fDistributedObjectAddBuffer);
        this.fDistributedObjectAddBuffer.clear();
        this.fDistributedObjectList
                .removeAll(this.fDistributedObjectRemoveBuffer);
        this.fDistributedObjectRemoveBuffer.clear();
    }
    
    /**
     * @param pDistributedObject
     *            - distributed object
     */
    public void registerDistributedObject(
            final DistributedObject pDistributedObject) {
        synchronized (this.fDistributedObjectAddBuffer) {
            this.fDistributedObjectAddBuffer.add(pDistributedObject);
        }
    }
    
    /**
     * @param pDistributedObject
     *            - distributed object
     */
    public void deregisterDistributedObject(
            final DistributedObject pDistributedObject) {
        synchronized (this.fDistributedObjectRemoveBuffer) {
            this.fDistributedObjectRemoveBuffer.add(pDistributedObject);
        }
    }
    
    /**
     * @param pSchedulerListener
     *            - scheduler listener
     */
    public void registerSchedulerListener(
            final SchedulerListener pSchedulerListener) {
        synchronized (this.fSchedulerListenerList) {
            this.fSchedulerListenerList.add(pSchedulerListener);
        }
    }
    
    /**
     * @param pSchedulerListener
     *            - scheduler listener
     */
    public void deregisterSchedulerListener(
            final SchedulerListener pSchedulerListener) {
        synchronized (this.fSchedulerListenerList) {
            this.fSchedulerListenerList.remove(pSchedulerListener);
        }
    }
}
