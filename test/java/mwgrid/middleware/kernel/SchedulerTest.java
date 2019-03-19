package mwgrid.middleware.kernel;

import mwgrid.middleware.distributedobject.DistributedObject;
import mwgrid.middleware.exception.RollbackException;
import mwgrid.middleware.exception.SchedulerException;

import org.junit.Assert;
import org.junit.Test;

public final class SchedulerTest {
    private static final int WAIT_PERIOD = 1000;
    private static final int CLASS_ID = 0;
    
    public class StubSchedulerListener implements SchedulerListener {
        private int fSchedulerListenerCalls;
        
        /**
         * Constructor
         */
        public StubSchedulerListener() {
            // Constructor
        }
        
        @Override
        public void collectReport(final long pAgentId, final int pTime,
                final String pReport) {
            this.fSchedulerListenerCalls++;
        }
        
        /**
         * @return (int) scheduler listener calls
         */
        public int getSchedulerListenerCalls() {
            return this.fSchedulerListenerCalls;
        }
    }
    
    public class StubDistributedObject extends DistributedObject {
        private int fDistributedObjectCalls;
        
        /**
         * @param pClassId
         *            - class ID
         */
        protected StubDistributedObject(final int pClassId) {
            super(pClassId);
        }
        
        @Override
        public void step() throws RollbackException {
            this.fDistributedObjectCalls++;
            this.getClassTypeId();
            try {
                Thread.sleep(SchedulerTest.WAIT_PERIOD);
            } catch (final InterruptedException e) {
                e.printStackTrace();
            }
        }
        
        @Override
        public String report() {
            return "";
        }
        
        /**
         * @return (int) distributed object calls
         */
        public int getDistributedObjectCalls() {
            return this.fDistributedObjectCalls;
        }
    }
    
    public class DistributedObjectCreatesOthers extends DistributedObject {
        private int fDistributedObjectCalls;
        private final int fInterval;
        private int fDistributedObjectsCreated;
        
        /**
         * @param pClassId
         *            - class ID
         * @param pInterval
         *            - interval
         */
        protected DistributedObjectCreatesOthers(final int pClassId,
                final int pInterval) {
            super(pClassId);
            this.fInterval = pInterval;
        }
        
        @Override
        public void step() {
            this.fDistributedObjectCalls++;
            if (this.fDistributedObjectCalls % this.fInterval == 0) {
                new DistributedObjectCreatesOthers(
                        SchedulerTest.CLASS_ID, this.fInterval);
                this.fDistributedObjectsCreated++;
            }
        }
        
        @Override
        public String report() {
            return "";
        }
        
        /**
         * @return (int) distributed object calls
         */
        public int getDistributedObjectCalls() {
            return this.fDistributedObjectCalls;
        }
        
        /**
         * @return (int) distributed objects created
         */
        public int getDistributedObjectsCreated() {
            return this.fDistributedObjectsCreated;
        }
    }
    
    /**
     * Public constructor
     */
    public SchedulerTest() {
        // Do nothing
    }
    
    /**
     * Test constructor
     */
    @Test
    public void testConstruction() {
        new Scheduler(10);
    }
    
    /**
     * Test start simulation without listeners
     */
    @Test(expected = SchedulerException.class)
    public void testStartSimulationWithoutListeners() {
        final Scheduler scheduler =
                new Scheduler(10);
        scheduler.startSimulation();
    }
    
    /**
     * Test start simulation with listeners but without distributed object
     */
    @Test(expected = SchedulerException.class)
    public void testStartSimulationWithListenersWithoutDistributedObject() {
        Kernel.getSequentionalInstance(new StubSchedulerListener(), 1);
        Kernel.startSimulation();
    }
    
    /**
     * Test start simulation with listeners and with distributed object
     */
    @Test
    public void testStartSimulationWithListenersAndDistributedObject() {
        final StubSchedulerListener schedulerListener =
                new StubSchedulerListener();
        Kernel.getSequentionalInstance(schedulerListener, 1);
        final StubDistributedObject distributedObject =
                new StubDistributedObject(
                        SchedulerTest.CLASS_ID);
        Kernel.startSimulation();
        while (Kernel.isSimulationAlive()) {
            continue;
        }
        Assert.assertTrue(schedulerListener.getSchedulerListenerCalls() == 1);
        Assert.assertTrue(distributedObject.getDistributedObjectCalls() == 1);
    }
    
    /**
     * Test stop simulation without running simulation
     */
    @Test
    public void testStopSimulationWithoutRunningSimulation() {
        final Scheduler scheduler =
                new Scheduler(10);
        scheduler.stopSimulation();
    }
    
    /**
     * Test start and stop simulation
     */
    @Test
    public void testStartAndStopSimulation() {
        final StubSchedulerListener schedulerListener =
                new StubSchedulerListener();
        Kernel.getSequentionalInstance(schedulerListener, 1);
        final StubDistributedObject distributedObject =
                new StubDistributedObject(
                        SchedulerTest.CLASS_ID);
        Kernel.startSimulation();
        while (Kernel.isSimulationAlive()) 
            continue;
        Assert.assertTrue(schedulerListener.getSchedulerListenerCalls() > 0);
        Assert.assertTrue(distributedObject.getDistributedObjectCalls() > 0);
    }
    
//    /**
//     * Test start simulation and registering distributed object
//     */
//    @Test
//    public void testStartAndRegisterDistributedObject() {
//        final StubSchedulerListener schedulerListener =
//                new StubSchedulerListener();
//        new Kernel(10, schedulerListener);
//        final StubDistributedObject distributedObject =
//                new StubDistributedObject(
//                        SchedulerImplementationTest.CLASS_ID);
//        Kernel.startSimulation();
//        int distributedObjectCalls = 0;
//        while (distributedObjectCalls < 5) {
//            distributedObjectCalls = distributedObject.getDistributedObjectCalls();
//            continue;
//        }
//        Kernel.stopSimulation();
//        final StubDistributedObject secondDistributedObject =
//                new StubDistributedObject(
//                        SchedulerImplementationTest.CLASS_ID);
//        Kernel.startSimulation();
//        while (Kernel.isSimulationAlive())
//            continue;
//        Assert.assertTrue(schedulerListener.getSchedulerListenerCalls() > 0);
//        Assert.assertTrue(distributedObject.getDistributedObjectCalls() > 0);
//        Assert.assertTrue(secondDistributedObject.getDistributedObjectCalls() > 0);
//        Assert.assertTrue(distributedObject.getDistributedObjectCalls() == secondDistributedObject
//                .getDistributedObjectCalls());
//    }
//    
//    /**
//     * Test start and register distributed object
//     */
//    @Test
//    public void testStartAndDeregisterDistributedObject() {
//        final StubSchedulerListener schedulerListener =
//                new StubSchedulerListener();
//        new Kernel(10, schedulerListener);
//        final StubDistributedObject distributedObject =
//                new StubDistributedObject(
//                        SchedulerImplementationTest.CLASS_ID);
//        final StubDistributedObject secondDistributedObject =
//                new StubDistributedObject(
//                        SchedulerImplementationTest.CLASS_ID);
//        Kernel.startSimulation();
//        Assert.assertTrue(Kernel.isSimulationAlive());
//        try {
//            Thread.sleep(SchedulerImplementationTest.WAIT_PERIOD);
//        } catch (final InterruptedException e) {
//            e.printStackTrace();
//        }
//        Kernel.deregisterDistributedObject(secondDistributedObject);
//        try {
//            Thread.sleep(SchedulerImplementationTest.WAIT_PERIOD);
//        } catch (final InterruptedException e) {
//            e.printStackTrace();
//        }
//        Kernel.stopSimulation();
//        Assert.assertTrue(schedulerListener.getSchedulerListenerCalls() > 0);
//        Assert.assertTrue(distributedObject.getDistributedObjectCalls() > 0);
//        Assert.assertTrue(secondDistributedObject.getDistributedObjectCalls() > 0);
//        Assert.assertTrue(distributedObject.getDistributedObjectCalls() > secondDistributedObject
//                .getDistributedObjectCalls());
//    }
    
//    /**
//     * Test start simulation and register scheduler listener
//     */
//    @Test
//    public void testStartAndRegisterSchedulerListener() {
//        final StubSchedulerListener schedulerListener =
//                new StubSchedulerListener();
//        new Kernel(10, schedulerListener);
//        final StubDistributedObject distributedObject =
//                new StubDistributedObject(
//                        SchedulerImplementationTest.CLASS_ID);
//        Kernel.startSimulation();
//        Assert.assertTrue(Kernel.isSimulationAlive());
//        try {
//            Thread.sleep(SchedulerImplementationTest.WAIT_PERIOD);
//        } catch (final InterruptedException e) {
//            e.printStackTrace();
//        }
//        final StubSchedulerListener secondSchedulerListener =
//                new StubSchedulerListener();
//        Kernel.registerSchedulerListener(secondSchedulerListener);
//        try {
//            Thread.sleep(SchedulerImplementationTest.WAIT_PERIOD);
//        } catch (final InterruptedException e) {
//            e.printStackTrace();
//        }
//        Kernel.stopSimulation();
//        Assert.assertTrue(schedulerListener.getSchedulerListenerCalls() > 0);
//        Assert.assertTrue(secondSchedulerListener.getSchedulerListenerCalls() > 0);
//        Assert.assertTrue(schedulerListener.getSchedulerListenerCalls() > secondSchedulerListener
//                .getSchedulerListenerCalls());
//        Assert.assertTrue(distributedObject.getDistributedObjectCalls() > 0);
//    }
    
//    /**
//     * Test start simulation and reregister scheduler listener
//     */
//    @Test
//    public void testStartAndDeregisterSchedulerListener() {
//        final StubSchedulerListener schedulerListener =
//                new StubSchedulerListener();
//        new Kernel(10, schedulerListener);
//        final StubSchedulerListener secondSchedulerListener =
//                new StubSchedulerListener();
//        Kernel.registerSchedulerListener(secondSchedulerListener);
//        final StubDistributedObject distributedObject =
//                new StubDistributedObject(
//                        SchedulerImplementationTest.CLASS_ID);
//        Kernel.startSimulation();
//        Assert.assertTrue(Kernel.isSimulationAlive());
//        try {
//            Thread.sleep(SchedulerImplementationTest.WAIT_PERIOD);
//        } catch (final InterruptedException e) {
//            e.printStackTrace();
//        }
//        Kernel.deregisterSchedulerListener(secondSchedulerListener);
//        try {
//            Thread.sleep(SchedulerImplementationTest.WAIT_PERIOD);
//        } catch (final InterruptedException e) {
//            e.printStackTrace();
//        }
//        Kernel.stopSimulation();
//        Assert.assertTrue(schedulerListener.getSchedulerListenerCalls() > 0);
//        Assert.assertTrue(secondSchedulerListener.getSchedulerListenerCalls() > 0);
//        Assert.assertTrue(schedulerListener.getSchedulerListenerCalls() > secondSchedulerListener
//                .getSchedulerListenerCalls());
//        Assert.assertTrue(distributedObject.getDistributedObjectCalls() > 0);
//    }
    
    /**
     * Test start simulation and have one distributed object create others
     */
    @Test
    public void testDistributedObjectCreatesAnother() {
        final StubSchedulerListener schedulerListener =
                new StubSchedulerListener();
        Kernel.getSequentionalInstance(schedulerListener, 10);
        final DistributedObjectCreatesOthers distributedObject =
                new DistributedObjectCreatesOthers(
                        SchedulerTest.CLASS_ID, 10);
        Kernel.startSimulation();
        Assert.assertTrue(Kernel.isSimulationAlive());
        try {
            Thread.sleep(SchedulerTest.WAIT_PERIOD);
        } catch (final InterruptedException e) {
            e.printStackTrace();
        }
        Kernel.stopSimulation();
        Assert.assertTrue(schedulerListener.getSchedulerListenerCalls() > 0);
        Assert.assertTrue(distributedObject.getDistributedObjectCalls() > 0);
        Assert.assertTrue(distributedObject.getDistributedObjectsCreated() > 0);
    }
}
