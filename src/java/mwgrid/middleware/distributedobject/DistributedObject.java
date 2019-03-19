package mwgrid.middleware.distributedobject;

import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.logging.Level;
import java.util.logging.Logger;

import mwgrid.common.FilePrint;
import mwgrid.middleware.distributedobject.AbstractMessage.MessageComparator;
import mwgrid.middleware.exception.RollbackException;
import mwgrid.middleware.kernel.Kernel;
import mwgrid.middleware.kernel.Statebase;

public abstract class DistributedObject {
    private static final Logger LOG = Logger
            .getLogger(DistributedObject.class.getPackage().getName());
    private final Statebase fStatebase;
    
    /**
     * @param pClassId
     *            - class ID
     */
    protected DistributedObject(final int pClassId) {
        DistributedObject.LOG.finest("Constructor");
        this.fStatebase = new Statebase();
        this.addVariable(KernelVariables.CLASS, new Value<Integer>(
                new Integer(pClassId)));
        this.addVariable(KernelVariables.MESSAGES, new Value<String>(
                new String("|")));
        // Register DistributedObject with Scheduler
        Kernel.getScheduler().registerDistributedObject(this);
    }
    
    /**
     * @return (int) time
     */
    public int getTime() {
        return this.fStatebase.getTime();
    }
    
    /**
     * Increase time by one
     */
    public void increaseTime() {
        this.fStatebase.increaseTime();
    }
    
    /**
     * @return (boolean) reset rolled back flag
     */
    public boolean resetRolledBackFlag() {
        return this.fStatebase.resetRolledBackFlag();
    }
    
    /**
     * Remove object from simulation reference lists, but keep statebases
     * intact.
     */
    public void destroyDistributedObject() {
        LOG.finest("Destroy distributed object");
        try {
            Kernel.getScheduler().deregisterDistributedObject(this);
            this.fStatebase.destroyStatebase();
        } finally {
            LOG.finest("Finished destroy distributed object");
        }
    }
    
    /**
     * @return (ClassType) class fType
     * @throws RollbackException
     *             - roll-back exception
     */
    public final int getClassTypeId() throws RollbackException {
        return ((Integer) this.getVariable(KernelVariables.CLASS).get())
                .intValue();
    }
    
    /**
     * @param pVariable
     *            - variable
     * @param pValue
     *            - value
     */
    protected final void addVariable(final Variable pVariable,
            final Value<?> pValue) {
        long startTime = 0;
        if (LOG.isLoggable(Level.FINEST)) {
            startTime = System.currentTimeMillis();
            LOG.finest("\tAdd variable on agent: " + this.getObjectId()
                    + ", at time: " + this.getTime());
        }
        try {
            this.fStatebase.addVariable(pVariable, pValue);
        } finally {
            if (LOG.isLoggable(Level.FINEST)) {
                final long duration = System.currentTimeMillis() - startTime;
                LOG.finest("\tFinished adding variable on agent: "
                        + this.getObjectId() + ", at time: " + this.getTime());
                FilePrint.printToFile(
                    Kernel.getSSVHandler().getRank(),
                    FilePrint.Filename.DO_ADD,
                    duration + "\t" + this.getObjectId() + "\t"
                            + this.getTime());
            }
        }
    }
    
    /**
     * @return (ObjectID) fObject ID
     */
    public final long getObjectId() {
        return this.fStatebase.getObjectID();
    }
    
    /**
     * @see mwgrid.middleware.distributedobject.VariableObject#getVariable(mwgrid.middleware.distributedobject.Variable)
     * @param pVariable
     *            - variable
     * @return (Value<?>) fValue
     * @throws RollbackException
     *             - roll-back exception
     */
    public Value<?> getVariable(final Variable pVariable)
            throws RollbackException {
        long startTime = 0;
        if (LOG.isLoggable(Level.FINE)) {
            startTime = System.currentTimeMillis();
            LOG.finest("\tGet variable: " + pVariable + ", on agent: "
                    + this.getObjectId() + ", at time: " + this.getTime());
        }
        try {
            return this.fStatebase.getVariable(pVariable);
        } finally {
            if (LOG.isLoggable(Level.FINE)) {
                final long duration = System.currentTimeMillis() - startTime;
                LOG.finest("\tFinished getting variable: " + pVariable
                        + ", on agent: " + this.getObjectId() + ", at time: "
                        + this.getTime());
                FilePrint
                        .printToFile(Kernel.getSSVHandler().getRank(),
                            FilePrint.Filename.DO_GET, duration + "\t"
                                    + pVariable + "\t" + this.getObjectId()
                                    + "\t" + this.getTime());
            }
        }
    }
    
    /**
     * @see mwgrid.middleware.distributedobject.VariableObject#getVariable(mwgrid.middleware.distributedobject.ObjectID,
     *      mwgrid.middleware.distributedobject.Variable)
     * @param pObjectId
     *            - object ID
     * @param pVariable
     *            - variable
     * @return (Value<?>) value
     * @throws RollbackException
     *             - roll-back exception
     */
    public Value<?> getVariable(final long pObjectId, final Variable pVariable)
            throws RollbackException {
        long startTime = 0;
        if (LOG.isLoggable(Level.FINE)) {
            startTime = System.currentTimeMillis();
            LOG.finest("\tGet variable: " + pVariable + ", of agent: "
                    + pObjectId + ", on agent: " + this.getObjectId()
                    + ", at time: " + this.getTime());
        }
        try {
            if (pObjectId == this.getObjectId())
                return this.getVariable(pVariable);
            return this.fStatebase.getVariable(pObjectId, pVariable);
        } finally {
            if (LOG.isLoggable(Level.FINE)) {
                final long duration = System.currentTimeMillis() - startTime;
                LOG.finest("\tFinished getting variable: " + pVariable
                        + ", of agent: " + pObjectId + ", on agent: "
                        + this.getObjectId() + ", at time: " + this.getTime());
                FilePrint.printToFile(Kernel.getSSVHandler().getRank(),
                    FilePrint.Filename.DO_GET_ID, duration + "\t" + pVariable
                            + "\t" + pObjectId + "\t" + this.getObjectId()
                            + "\t" + this.getTime());
            }
        }
    }
    
    /**
     * @see mwgrid.middleware.distributedobject.VariableObject#setVariable(mwgrid.middleware.distributedobject.Variable,
     *      mwgrid.middleware.distributedobject.Value)
     * @param pVariable
     *            - variable
     * @param pValue
     *            - fValue
     * @throws RollbackException
     *             - roll-back exception
     */
    public void setVariable(final Variable pVariable, final Value<?> pValue)
            throws RollbackException {
        long startTime = 0;
        if (LOG.isLoggable(Level.FINE)) {
            startTime = System.currentTimeMillis();
            LOG.finest("\tSet variable: " + pVariable + ", to: "
                    + pValue.get().toString() + ", on agent: "
                    + this.getObjectId() + ", at time: " + this.getTime());
        }
        try {
            this.fStatebase.setVariable(pVariable, pValue);
        } finally {
            if (LOG.isLoggable(Level.FINE)) {
                final long duration = System.currentTimeMillis() - startTime;
                LOG.finest("\tFinished set variable: " + pVariable + ", to: "
                        + pValue.get().toString() + ", on agent: "
                        + this.getObjectId() + ", at time: " + this.getTime());
                FilePrint.printToFile(Kernel.getSSVHandler().getRank(),
                    FilePrint.Filename.DO_SET, duration + "\t" + pVariable
                            + "\t" + pValue.get() + "\t" + this.getObjectId()
                            + "\t" + this.getTime());
            }
        }
    }
    
    /**
     * @see mwgrid.middleware.distributedobject.VariableObject#setVariable(mwgrid.middleware.distributedobject.ObjectID,
     *      mwgrid.middleware.distributedobject.Variable,
     *      mwgrid.middleware.distributedobject.Value)
     * @param pObjectId
     *            - object ID
     * @param pValue
     *            - value
     * @param pVariable
     *            - variable
     * @throws RollbackException
     *             - roll-back exception
     */
    public void setVariable(final long pObjectId,
            final Variable pVariable, final Value<?> pValue)
            throws RollbackException {
        long startTime = 0;
        if (LOG.isLoggable(Level.FINE)) {
            startTime = System.currentTimeMillis();
            LOG.finest("\tSet variable: " + pVariable + ", for agent: "
                    + pObjectId + ", to: " + pValue.get().toString()
                    + ", on agent: " + this.getObjectId() + ", at time: "
                    + this.getTime());
        }
        try {
            if (pObjectId == this.getObjectId()) {
                this.setVariable(pVariable, pValue);
                return;
            }
            this.fStatebase.setVariable(pObjectId, pVariable, pValue);
        } finally {
            if (LOG.isLoggable(Level.FINE)) {
                final long duration = System.currentTimeMillis() - startTime;
                LOG.finest("\tFinished set variable: " + pVariable
                        + ", for agent: " + pObjectId + ", to: "
                        + pValue.get().toString() + ", on agent: "
                        + this.getObjectId() + ", at time: " + this.getTime());
                FilePrint.printToFile(Kernel.getSSVHandler().getRank(),
                    FilePrint.Filename.DO_SET_ID, duration + "\t" + pVariable
                            + "\t" + pObjectId + "\t" + pValue.get() + "\t"
                            + this.getObjectId() + "\t" + this.getTime());
            }
        }
    }
    
    /**
     * @param pStartLocation
     *            - start location
     * @param pEndLocation
     *            - end location
     * @return map of agent IDs and locations
     * @throws RollbackException
     *             - thrown when rollback occurs
     */
    protected Map<Long, Location> rangeQuery(final Location pStartLocation,
            final Location pEndLocation) throws RollbackException {
        long startTime = 0;
        if (LOG.isLoggable(Level.FINE)) {
            startTime = System.currentTimeMillis();
            LOG.finest("Range query, with start: (" + pStartLocation.getX()
                    + ", " + pStartLocation.getY() + "), and end: ("
                    + pEndLocation.getX() + ", " + pEndLocation.getY()
                    + "), on agent: " + this.getObjectId() + ", at time: "
                    + this.getTime());
        }
        try {
            return Kernel.getSSVHandler().rangeQuery(this.getObjectId(),
                this.getTime() - 1, pStartLocation, pEndLocation);
        } finally {
            if (LOG.isLoggable(Level.FINE)) {
                final long duration = System.currentTimeMillis() - startTime;
                LOG.finest("Finished range query, with start: ("
                        + pStartLocation.getX() + ", "
                        + pStartLocation.getY() + "), and end: ("
                        + pEndLocation.getX() + ", " + pEndLocation.getY()
                        + "), on agent: " + this.getObjectId()
                        + ", at time: " + this.getTime());
                FilePrint
                        .printToFile(
                            Kernel.getSSVHandler().getRank(),
                            FilePrint.Filename.DO_RQ,
                            duration + "\t" + pStartLocation.getX() + "\t"
                                    + pStartLocation.getY() + "\t"
                                    + pEndLocation.getX() + "\t"
                                    + pEndLocation.getY() + "\t"
                                    + this.getObjectId() + "\t"
                                    + this.getTime());
            }
        }
    }
    
    /**
     * @param pClassId
     *            - class ID
     * @param pExcludeObjectId
     *            - exclude object ID
     * @param pStartLocation
     *            - start location
     * @param pEndLocation
     *            - fEnd location
     * @return result
     * @throws RollbackException
     *             - roll-back exception
     */
    protected Map<Long, Location> rangeQuery(final int pClassId,
            final long pExcludeObjectId, final Location pStartLocation,
            final Location pEndLocation) throws RollbackException {
        long startTime = 0;
        if (LOG.isLoggable(Level.FINE)) {
            startTime = System.currentTimeMillis();
            LOG.finest("Range query, with class ID: " + pClassId
                    + ", exclude: " + pExcludeObjectId + ", with start: ("
                    + pStartLocation.getX() + ", " + pStartLocation.getY()
                    + "), and end: (" + pEndLocation.getX() + ", "
                    + pEndLocation.getY() + "), on agent: "
                    + this.getObjectId() + ", at time: " + this.getTime());
        }
        try {
            final Map<Long, Location> objectMap =
                    this.rangeQuery(pStartLocation, pEndLocation);
            final Map<Long, Location> result = new HashMap<Long, Location>();
            for (final long objectId : objectMap.keySet())
                if (pExcludeObjectId != objectId
                        && ((Integer) this.getVariable(objectId,
                            KernelVariables.CLASS).get()).intValue() == pClassId) {
                    result.put(new Long(objectId),
                        objectMap.get(new Long(objectId)));
                }
            return result;
        } finally {
            if (LOG.isLoggable(Level.FINE)) {
                final long duration = System.currentTimeMillis() - startTime;
                LOG.finest("Finished range query, with class ID: " + pClassId
                        + ", exclude: " + pExcludeObjectId
                        + ", with start: (" + pStartLocation.getX() + ", "
                        + pStartLocation.getY() + "), and end: ("
                        + pEndLocation.getX() + ", " + pEndLocation.getY()
                        + "), on agent: " + this.getObjectId()
                        + ", at time: " + this.getTime());
                FilePrint
                        .printToFile(
                            Kernel.getSSVHandler().getRank(),
                            FilePrint.Filename.DO_RQ,
                            duration + "\t" + pClassId + "\t"
                                    + pExcludeObjectId + "\t"
                                    + pStartLocation.getX() + "\t"
                                    + pStartLocation.getY() + "\t"
                                    + pEndLocation.getX() + "\t"
                                    + pEndLocation.getY() + "\t"
                                    + this.getObjectId() + "\t"
                                    + this.getTime());
            }
        }
    }
    
    /**
     * @return (Message) either message, or null if there are none
     * @throws RollbackException
     *             - roll-back exception
     */
    protected Message getMessage() throws RollbackException {
        long startTime = 0;
        if (LOG.isLoggable(Level.FINE)) {
            startTime = System.currentTimeMillis();
            LOG.finest("Get message on agent: " + this.getObjectId()
                    + ", at time: " + this.getTime());
        }
        try {
            final String messages =
                    (String) this.getVariable(KernelVariables.MESSAGES).get();
            if ("|".equals(messages)) return null;
            final Queue<Message> messageQueue =
                    this.convertStringToMessageQueue(messages);
            if (messageQueue.isEmpty()) return null;
            if (messageQueue.peek().getTime() >= this.getTime()) return null;
            final Message result = messageQueue.poll();
            LOG.fine("Received message on agent: " + this.getObjectId()
                    + ", at time: " + this.getTime() + ", message source: "
                    + result.getSource() + ", message destination: "
                    + result.getDestination() + ", message time: "
                    + result.getTime());
            final String newMessages =
                    this.convertMessageQueueToString(messageQueue);
            this.setVariable(KernelVariables.MESSAGES, new Value<String>(
                    new String(newMessages)));
            return result;
        } catch (RollbackException e) {
            LOG.fine("Caught RollbackException in getMessage on agent: "
                    + this.getObjectId() + ", at time: " + this.getTime());
            throw e;
        } finally {
            if (LOG.isLoggable(Level.FINE)) {
                final long duration = System.currentTimeMillis() - startTime;
                LOG.finest("Finished getting message on agent: "
                        + this.getObjectId() + ", at time: " + this.getTime());
                FilePrint.printToFile(
                    Kernel.getSSVHandler().getRank(),
                    FilePrint.Filename.DO_GETMESSAGE,
                    duration + "\t" + this.getObjectId() + "\t"
                            + this.getTime());
            }
        }
    }
    
    /**
     * <p>
     * IMPORTANT NOTE: If you send a message at time t, the recipiant will only
     * receive that message at t+1, no earlier! This because sending a message
     * means a write at time t. The recipiant, for receiving the message, would
     * have to do a write at time t as well. This is not allowed. As such, all
     * messages send will take at least 1 timestep to arrive.
     * <p>
     * FURTHER NOTE: This will do for now. If something further is required then
     * an additional private variable will have to be added. This variable will
     * store the last written time of the mailbox. It can then be checked
     * against for writes. That is, the variable is set to the timestep the
     * MESSAGES variable is written when it is written. If that variable
     * indicates that the MESSAGES variable has already been written, no more
     * writing is allowed for that timestep, and the agents will have to act
     * accordingly because of that (resend the message later, or wait until the
     * message can be received). For now, this is not necessary, so I've not
     * implemented this now.
     * 
     * @param pMessage
     *            - message
     * @throws RollbackException
     *             - roll-back exception
     */
    public void sendMessage(final Message pMessage) throws RollbackException {
        long startTime = 0;
        if (LOG.isLoggable(Level.FINE)) {
            startTime = System.currentTimeMillis();
            LOG.finest("Send message on agent: " + this.getObjectId()
                    + ", at time: " + this.getTime() + ", message source: "
                    + pMessage.getSource() + ", message destination: "
                    + pMessage.getDestination() + ", message time: "
                    + pMessage.getTime());
        }
        try {
            final String messages =
                    (String) this.getVariable(pMessage.getDestination(),
                        KernelVariables.MESSAGES).get();
            final String message =
                    this.addMessageToString(messages, pMessage);
            this.setVariable(pMessage.getDestination(),
                KernelVariables.MESSAGES, new Value<String>(new String(
                        message)));
        } catch (RollbackException e) {
            LOG.fine("Caught RollbackException in getMessage on agent: "
                    + this.getObjectId() + ", at time: " + this.getTime());
            throw e;
        } finally {
            if (LOG.isLoggable(Level.FINE)) {
                final long duration = System.currentTimeMillis() - startTime;
                LOG.finest("Finished send message on agent: "
                        + this.getObjectId() + ", at time: " + this.getTime()
                        + ", message source: " + pMessage.getSource()
                        + ", message destination: "
                        + pMessage.getDestination() + ", message time: "
                        + pMessage.getTime());
                FilePrint.printToFile(
                    Kernel.getSSVHandler().getRank(),
                    FilePrint.Filename.DO_SENDMESSAGE,
                    duration + "\t" + this.getObjectId() + "\t"
                            + this.getTime() + "\t" + pMessage.getSource()
                            + "\t" + pMessage.getDestination() + "\t"
                            + pMessage.getTime());
            }
        }
    }
    
    /**
     * @param pRollbackTime
     *            - roll-back time
     */
    public void rollBack(final int pRollbackTime) {
        LOG.finest("Rollback to: " + pRollbackTime + ", on agent: "
                + this.getObjectId() + ", at time: " + this.getTime());
        try {
            this.fStatebase.rollBack(pRollbackTime);
        } finally {
            LOG.finest("Finished rollback to: " + pRollbackTime
                    + ", on agent: " + this.getObjectId() + ", at time: "
                    + this.getTime());
        }
    }
    
    /**
     * Update private variable map to GVT
     * 
     * @param pGlobalVariableTime
     *            - global variable time
     */
    public void cleanToGVT(final int pGlobalVariableTime) {
        LOG.finest("clean to GVT: " + pGlobalVariableTime + ", on agent: "
                + this.getObjectId() + ", at time: " + this.getTime());
        try {
            this.fStatebase.cleanToGVT(pGlobalVariableTime);
        } finally {
            LOG.finest("Finished clean to GVT: " + pGlobalVariableTime
                    + ", on agent: " + this.getObjectId() + ", at time: "
                    + this.getTime());
        }
    }
    
    /**
     * @throws RollbackException
     *             - interrupted simulation exception
     */
    public abstract void step() throws RollbackException;
    
    /**
     * @return (String) report on agent
     * @throws RollbackException
     *             - roll-back exception
     */
    public abstract String report() throws RollbackException;
    
    /**
     * Converts a message string into a queue of agent messages
     * 
     * @param pMessageString
     *            - message string
     * @return (Queue<AgentMessage>) a queue of agent messages
     */
    protected Queue<Message> convertStringToMessageQueue(
            final String pMessageString) {
        final String[] stringArray = pMessageString.split("\\|");
        Queue<Message> result =
                new PriorityQueue<Message>(stringArray.length - 1,
                        new MessageComparator());
        for (String message : stringArray) {
            result = this.addStringToQueue(result, message);
        }
        return result;
    }
    
    /**
     * @param pMessageQueue
     *            - queue of messages
     * @return (String) string of messages
     */
    protected String convertMessageQueueToString(
            final Queue<Message> pMessageQueue) {
        final Queue<Message> queue =
                new PriorityQueue<Message>(pMessageQueue);
        String result = "|";
        for (Message message : queue) {
            result = this.addMessageToString(result, message);
        }
        return result;
    }
    
    /**
     * @param pString
     *            - string
     * @param pMessage
     *            - message
     * @return (String) string representation of message
     */
    protected String addMessageToString(final String pString,
            final Message pMessage) {
        final StringBuilder result = new StringBuilder();
        result.append(pString);
        result.append(pMessage.getClass().getName());
        result.append("=");
        result.append(pMessage.convertToString());
        result.append("|");
        return result.toString();
    }
    
    /**
     * @param pMessageQueue
     *            - message queue
     * @param pString
     *            - string
     * @return (Queue<Message>) message queue with message added
     */
    protected Queue<Message> addStringToQueue(
            final Queue<Message> pMessageQueue, final String pString) {
        // This method does not handle messages declared as inner classes!
        final Queue<Message> result =
                new PriorityQueue<Message>(pMessageQueue);
        if (pString.isEmpty()) return result;
        final String[] stringArray = pString.split("=");
        assert stringArray.length == 2;
        Class<?> cls = null;
        try {
            cls = Class.forName(stringArray[0]);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        Message message = null;
        if (cls != null) {
            try {
                message = (Message) cls.newInstance();
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        if (message != null) {
            message.convertFromString(stringArray[1]);
        }
        result.add(message);
        return result;
    }
}
