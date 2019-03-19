package mwgrid.middleware.kernel;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import mwgrid.common.FilePrint;
import mwgrid.middleware.distributedobject.DistributedObject;
import mwgrid.middleware.distributedobject.Location;
import mwgrid.middleware.distributedobject.Value;
import mwgrid.middleware.distributedobject.Variable;
import mwgrid.middleware.exception.InvalidTypeException;
import mwgrid.middleware.exception.RollbackException;
import mwgrid.middleware.exception.SSVAlreadyExistsException;
import mwgrid.middleware.exception.SSVNotFoundException;
import mwgrid.middleware.kernel.pdesmas.PDESMASInterface;

/**
 * @author Dr B.G.W. Craenen <b.g.w.craenen@cs.bham.ac.uk>
 */
public class DistributedSSVHandler implements SSVHandler {
    private static final Logger LOG = Logger
            .getLogger(DistributedSSVHandler.class.getPackage().getName());
    private static final Map<Integer, Integer> PARENTCLPMAP = Collections
            .unmodifiableMap(new HashMap<Integer, Integer>() {
                private static final long serialVersionUID = 1L;
                {
                    put(new Integer(1), new Integer(1));
                    put(new Integer(3), new Integer(2));
                    put(new Integer(7), new Integer(4));
                    put(new Integer(15), new Integer(8));
                    put(new Integer(31), new Integer(16));
                    put(new Integer(63), new Integer(32));
                    put(new Integer(127), new Integer(64));
                    put(new Integer(255), new Integer(128));
                    put(new Integer(511), new Integer(256));
                    put(new Integer(1023), new Integer(512));
                }
            });
    private int fNumberOfAlps;
    private int fNumberOfClps;
    // private int fStartTime;
    private int fEndTime;
    private final Map<Identifier, SavedStateVariable> fSavedStateVariableMap;
    private final Map<Long, Integer> fRollbackMap;
    private final PDESMASInterface fPDESMASInterface;
    
    /**
     * Constructor of DistributedSSVHandler
     * 
     * @param pNumberOfAlps
     *            - number of Alps
     * @param pNumberOfClps
     *            - number of Clps
     * @param pStartTime
     *            - start time
     * @param pEndTime
     *            - end time
     */
    public DistributedSSVHandler(final int pNumberOfAlps,
            final int pNumberOfClps, final int pStartTime, final int pEndTime) {
        LOG.finest("Construct DistributedSSVHandler");
        this.fNumberOfAlps = pNumberOfAlps;
        this.fNumberOfClps = pNumberOfClps;
        // this.fStartTime = pStartTime;
        this.fEndTime = pEndTime;
        this.fPDESMASInterface = new PDESMASInterface();
        this.fPDESMASInterface.construct(pNumberOfAlps, pNumberOfClps,
            pStartTime, pEndTime);
        this.fSavedStateVariableMap =
                new HashMap<Identifier, SavedStateVariable>();
        this.fRollbackMap = new HashMap<Long, Integer>();
    }
    
    /**
     * @param pDataLocation
     *            - data location
     * @see mwgrid.middleware.kernel.SSVHandler#initialise(java.lang.String)<p>
     *      <p>
     *      Initialisation is tricky. Only the root node, a CLP, will create the
     *      initial state file, the SOM file, and the AlpToParentFile. The
     *      Scheduler will then update the distributed objects created. On CLPs
     *      the distributed objects will be destroyed (no longer necessary). On
     *      ALPs, the non-required distributed objects are destroyed. This evens
     *      out the number of distributed objects per ALP. After these changes
     *      the scheduler is updated again so that it will be working with the
     *      right distributed objects. The PDESMAS is initialised through the
     *      interface. For an ALP this initialisation will return, so the
     *      simulation can be started. For a CLP it will not, control will be
     *      handed over to PDESMAS and this method will not end.
     */
    @Override
    public void initialise(final String pDataLocation) {
        FilePrint.setDataLocation(pDataLocation);
        LOG.finest("Generate initialisation file at rank: " + this.getRank());
        this.generateInitialisationFile(pDataLocation);
        LOG.finest("Removed unnecessary agents at rank: " + this.getRank());
        Kernel.getScheduler().updateRegisteredDistributedObjects();
        final List<DistributedObject> distributedObjects =
                Kernel.getScheduler().getRegisteredDistributedObjects();
        for (DistributedObject distributedObject : distributedObjects) {
            if (this.isClp()) distributedObject.destroyDistributedObject();
            else if (!(distributedObject.getObjectId()
                    % this.getNumberOfAlps() == (this.getRank()
                    - this.getNumberOfAlps() + 1)))
                distributedObject.destroyDistributedObject();
        }
        Kernel.getScheduler().updateRegisteredDistributedObjects();
        LOG.finest("Number of distributed objects at rank: "
                + this.getRank()
                + " is: "
                + Kernel.getScheduler().getRegisteredDistributedObjects()
                        .size());
        LOG.finest("Initialise PDESMAS interface at rank: " + this.getRank());
        // CLPs don't return from initialisation!
        this.fPDESMASInterface.initialise(pDataLocation);
        LOG.finest("Finish initialise PDESMAS interface at rank: "
                + this.getRank());
    }
    
    @Override
    public boolean add(final long pObjectId, final Variable pVariable,
            final Value<?> pInitValue, final int pTime)
            throws SSVAlreadyExistsException {
        long startTime = 0;
        if (LOG.isLoggable(Level.FINEST)) {
            startTime = System.currentTimeMillis();
            LOG.finest("Add, agent: " + pObjectId + ", variable: "
                    + pVariable + ", value: " + pInitValue.get() + ", time: "
                    + pTime);
        }
        try {
            // Create Identifier for SSV
            final Identifier identifier =
                    new Identifier(pObjectId, pVariable);
            // If SSV already in SSVMap, throw SSVAlreadyExistsException
            if (this.fSavedStateVariableMap.containsKey(identifier))
                throw new SSVAlreadyExistsException();
            // Create SSV
            final SavedStateVariable savedStateVariable =
                    new SavedStateVariable(identifier, pInitValue, pTime);
            // Add SSV to Map, return true if didn't exist before
            return null == this.fSavedStateVariableMap.put(identifier,
                savedStateVariable);
        } finally {
            if (LOG.isLoggable(Level.FINEST)) {
                final long duration = System.currentTimeMillis() - startTime;
                LOG.finest("Finish, Add, agent: " + pObjectId
                        + ", variable: " + pVariable + ", value: "
                        + pInitValue.get() + ", time: " + pTime + ", in: "
                        + duration);
                FilePrint.printToFile(this.getRank(),
                    FilePrint.Filename.SSVH_ADD, duration + "\t" + pObjectId
                            + "\t" + pVariable + "\t" + pInitValue.get()
                            + "\t" + pTime);
            }
        }
    }
    
    @Override
    public void removeVariables(final long pObjectId) {
        LOG.finest("RemoveVariables, agent: " + pObjectId);
        try {
            for (Identifier identifier : this.fSavedStateVariableMap.keySet()) {
                if (identifier.getObjectId() == pObjectId) {
                    this.fSavedStateVariableMap.remove(identifier);
                }
            }
        } finally {
            LOG.finest("Finish removeVariables, agent: " + pObjectId);
        }
    }
    
    @Override
    public boolean isClp() {
        return this.fPDESMASInterface.getRank() < this.fNumberOfClps;
    }
    
    @Override
    public boolean isAlp() {
        return this.fPDESMASInterface.getRank() >= this.fNumberOfClps
                && this.fPDESMASInterface.getRank() < (this.fNumberOfClps + this.fNumberOfAlps);
    }
    
    @Override
    public int getNumberOfAlps() {
        return this.fNumberOfAlps;
    }
    
    @Override
    public int getRank() {
        return this.fPDESMASInterface.getRank();
    }
    
    @Override
    public void setGVTTime(final long pObjectId, final int pTime) {
        // Skip
    }
    
    @Override
    public int getGVTTime() {
        return this.fPDESMASInterface.getGVT();
    }
    
    @Override
    public void sendGVTMessage() {
        this.fPDESMASInterface.sendGVTMessage();
    }
    
    @Override
    public Map<Long, Location> rangeQuery(final long pAgentId,
            final int pTime, final Location pStartLocation,
            final Location pEndLocation) throws RollbackException {
        long startTime = 0;
        if (LOG.isLoggable(Level.FINEST)) {
            startTime = System.currentTimeMillis();
            LOG.finest("RQ: " + pAgentId + ":" + pTime + ":"
                    + pStartLocation.getX() + ":" + pStartLocation.getY()
                    + ":" + pEndLocation.getX() + ":" + pEndLocation.getY());
            /*
             * FilePrint.printToFile( this.getRank(), FilePrint.Filename.TRACE,
             * "RangeQuery; agent: " + pAgentId + ", time: " + pTime +
             * ", start: (" + pStartLocation.getX() + " ," +
             * pStartLocation.getY() + "), end: (" + pEndLocation.getX() + " ,"
             * + pEndLocation.getY() + ")");
             */
        }
        try {
            return this.fPDESMASInterface.rangeQuery(pAgentId, pTime,
                pStartLocation, pEndLocation);
        } catch (RollbackException rollbackException) {
            if (LOG.isLoggable(Level.FINEST)) {
                FilePrint.printToFile(
                    this.getRank(),
                    FilePrint.Filename.TRACE,
                    "RangeQuery RollbackException; agent: "
                            + rollbackException.getAgentId()
                            + ", rollback time: "
                            + rollbackException.getRollbackTime());
            }
            throw rollbackException;
        } finally {
            if (LOG.isLoggable(Level.FINEST)) {
                final long duration = System.currentTimeMillis() - startTime;
                LOG.finest("Finished RQ: " + pAgentId + ":" + pTime + ":"
                        + pStartLocation.getX() + ":" + pStartLocation.getY()
                        + ":" + pEndLocation.getX() + ":"
                        + pEndLocation.getY() + ", in: " + duration);
                /*
                 * FilePrint.printToFile( this.getRank(),
                 * FilePrint.Filename.TRACE, "RangeQuery; agent: " + pAgentId +
                 * ", time: " + pTime + ", start: (" + pStartLocation.getX() +
                 * " ," + pStartLocation.getY() + "), end: (" +
                 * pEndLocation.getX() + " ," + pEndLocation.getY() +
                 * "), duration: " + duration);
                 */
                if (pTime <= this.fEndTime)
                    FilePrint.printToFile(
                        this.getRank(),
                        FilePrint.Filename.SSVH_RQ,
                        duration + "\t" + pAgentId + "\t" + pTime + "\t"
                                + pStartLocation.getX() + "\t"
                                + pStartLocation.getY() + "\t"
                                + pEndLocation.getX() + "\t"
                                + pEndLocation.getY());
            }
        }
    }
    
    @Override
    public Value<?> read(final long pAgentId, final long pObjectId,
            final Variable pVariable, final int pTime)
            throws RollbackException, SSVNotFoundException {
        long startTime = 0;
        Value<?> result = null;
        if (LOG.isLoggable(Level.FINEST)) {
            startTime = System.currentTimeMillis();
            LOG.finest("READ: " + pAgentId + ":" + pObjectId + ":"
                    + pVariable.getVariableId() + ":" + pTime);
            /*
             * FilePrint.printToFile(this.getRank(), FilePrint.Filename.TRACE,
             * "Read; agent: " + pAgentId + ", object: " + pObjectId +
             * ", variable: " + pVariable + ", time: " + pTime);
             */
        }
        try {
            result =
                    this.fPDESMASInterface.read(pAgentId, pObjectId,
                        pVariable.getVariableId(), pTime);
            return result;
        } catch (RollbackException rollbackException) {
            if (LOG.isLoggable(Level.FINEST)) {
                FilePrint.printToFile(
                    this.getRank(),
                    FilePrint.Filename.TRACE,
                    "Read RollbackException; agent: "
                            + rollbackException.getAgentId()
                            + ", rollback time: "
                            + rollbackException.getRollbackTime());
            }
            throw rollbackException;
        } finally {
            if (LOG.isLoggable(Level.FINEST)) {
                final long duration = System.currentTimeMillis() - startTime;
                String resultString;
                if (result == null) resultString = "NULL";
                else resultString = result.get().toString();
                LOG.finest("Finished READ: " + pAgentId + ":" + pObjectId
                        + ":" + pVariable.getVariableId() + ":" + pTime
                        + ", in: " + duration + ", result: " + resultString);
                /*
                 * FilePrint.printToFile(this.getRank(),
                 * FilePrint.Filename.TRACE, "Read; agent: " + pAgentId +
                 * ", object: " + pObjectId + ", variable: " + pVariable +
                 * ", time: " + pTime + ", duration: " + duration + ", result: "
                 * + resultString);
                 */
                if (pTime <= this.fEndTime)
                    FilePrint.printToFile(this.getRank(),
                        FilePrint.Filename.SSVH_READ, duration + "\t"
                                + pAgentId + "\t" + pObjectId + "\t"
                                + pVariable.getVariableId() + "\t" + pTime);
            }
        }
    }
    
    @Override
    public boolean write(final long pAgentId, final long pObjectId,
            final Variable pVariable, final Value<?> pValue, final int pTime)
            throws RollbackException, SSVNotFoundException {
        long startTime = 0;
        if (LOG.isLoggable(Level.FINEST)) {
            startTime = System.currentTimeMillis();
            LOG.finest("WRITE: " + pAgentId + ":" + pObjectId + ":"
                    + pVariable + ":" + pValue.get() + ":" + pTime);
            /*
             * FilePrint.printToFile(this.getRank(), FilePrint.Filename.TRACE,
             * "Write; agent: " + pAgentId + ", object: " + pObjectId +
             * ", variable: " + pVariable + ", value: " + pValue + ", time: " +
             * pTime);
             */
        }
        try {
            if (pValue.getType() == Integer.class)
                return this.fPDESMASInterface.write(pAgentId, pObjectId,
                    pVariable.getVariableId(), ((Integer) pValue.get()).intValue(), pTime);
            if (pValue.getType() == Double.class)
                return this.fPDESMASInterface.write(pAgentId, pObjectId,
                    pVariable.getVariableId(), ((Double) pValue.get()).doubleValue(), pTime);
            if (pValue.getType() == Location.class)
                return this.fPDESMASInterface.write(pAgentId, pObjectId,
                    pVariable.getVariableId(), (Location) pValue.get(), pTime);
            if (pValue.getType() == String.class)
                return this.fPDESMASInterface.write(pAgentId, pObjectId,
                    pVariable.getVariableId(), (String) pValue.get(), pTime);
            throw new InvalidTypeException();
        } catch (RollbackException rollbackException) {
            if (LOG.isLoggable(Level.FINEST)) {
                FilePrint.printToFile(
                    this.getRank(),
                    FilePrint.Filename.TRACE,
                    "Write RollbackException; agent: "
                            + rollbackException.getAgentId()
                            + ", rollback time: "
                            + rollbackException.getRollbackTime());
            }
            throw rollbackException;
        } finally {
            if (LOG.isLoggable(Level.FINEST)) {
                final long duration = System.currentTimeMillis() - startTime;
                LOG.finest("Finished WRITE: " + pAgentId + ":" + pObjectId
                        + ":" + pVariable + ":" + pValue.get() + ":" + pTime
                        + ", in: " + duration);
                /*
                 * FilePrint.printToFile(this.getRank(),
                 * FilePrint.Filename.TRACE, "Write; agent: " + pAgentId +
                 * ", object: " + pObjectId + ", variable: " + pVariable +
                 * ", value: " + pValue + ", time: " + pTime + ", duration: " +
                 * duration);
                 */
                if (pTime <= this.fEndTime)
                    FilePrint.printToFile(this.getRank(),
                        FilePrint.Filename.SSVH_WRITE, duration + "\t"
                                + pAgentId + "\t" + pObjectId + "\t"
                                + pVariable.getVariableId() + "\t" + pTime
                                + "\t" + pValue.get());
            }
        }
    }
    
    /**
     * @param pAgentId
     *            - agent ID
     * @param pTime
     *            - time
     */
    @Override
    public void handleRollback(final long pAgentId, final int pTime) {
        long startTime = 0;
        if (LOG.isLoggable(Level.FINEST)) {
            startTime = System.currentTimeMillis();
            LOG.finest("Store rollback on: " + pAgentId + " to: " + pTime);
            /*
             * FilePrint.printToFile(this.getRank(), FilePrint.Filename.TRACE,
             * "Rollback callback; agent: " + pAgentId + ", time: " + pTime);
             */
        }
        try {
            if (this.fRollbackMap.containsKey(new Long(pAgentId))
                    && this.fRollbackMap.get(new Long(pAgentId)).intValue() <= pTime)
                return;
            if (LOG.isLoggable(Level.FINEST)) {
                LOG.finest("Put into rollback map; agentID: " + pAgentId
                        + ", time: " + pTime);
                /*
                 * FilePrint.printToFile(this.getRank(),
                 * FilePrint.Filename.TRACE, "Added rollback callback; agent: "
                 * + pAgentId + ", time: " + pTime);
                 */
            }
            this.fRollbackMap.put(new Long(pAgentId), new Integer(pTime));
        } finally {
            if (LOG.isLoggable(Level.FINEST)) {
                final long duration = System.currentTimeMillis() - startTime;
                LOG.finest("Finished store rollback on: " + pAgentId
                        + " to: " + pTime + ", in: " + duration);
                FilePrint.printToFile(this.getRank(),
                    FilePrint.Filename.SSVH_ROLLBACK, duration + "\t"
                            + pAgentId + "\t" + pTime);
                /*
                 * FilePrint.printToFile(this.getRank(),
                 * FilePrint.Filename.TRACE, "Rollback callback; agent: " +
                 * pAgentId + ", time: " + pTime + ", duration: " + duration);
                 */
            }
        }
    }
    
    @Override
    public boolean isInRollbackMap(final long pAgentId) {
        return this.fRollbackMap.containsKey(new Long(pAgentId));
    }
    
    @Override
    public int getRollbackMapTime(final long pAgentId) {
        final int result =
                this.fRollbackMap.get(new Long(pAgentId)).intValue();
        this.fRollbackMap.remove(new Long(pAgentId));
        return result;
    }
    
    /**
     * @param pDataLocation
     *            - data location
     */
    private void generateInitialisationFile(final String pDataLocation) {
        BufferedWriter writer = null;
        try {
            final File file = new File(pDataLocation + "Initialisation.dat");
            writer = new BufferedWriter(new FileWriter(file));
            // Add the various types of messages used in PDESMAS
            writer.write("message: SingleReadMessage");
            writer.newLine();
            writer.write("message: SingleReadResponseMessage");
            writer.newLine();
            writer.write("message: SingleReadAntiMessage");
            writer.newLine();
            writer.write("message: RangeQueryMessage");
            writer.newLine();
            writer.write("message: RangeQueryAntiMessage");
            writer.newLine();
            writer.write("message: WriteMessage");
            writer.newLine();
            writer.write("message: WriteResponseMessage");
            writer.newLine();
            writer.write("message: WriteAntiMessage");
            writer.newLine();
            writer.write("message: GvtControlMessage");
            writer.newLine();
            writer.write("message: GvtRequestMessage");
            writer.newLine();
            writer.write("message: GvtValueMessage");
            writer.newLine();
            writer.write("message: RollbackMessage");
            writer.newLine();
            writer.write("message: StateMigrationMessage");
            writer.newLine();
            writer.write("message: RangeUpdateMessage");
            writer.newLine();
            // writer.write("message: EndMessage");
            // writer.newLine();
            // Add the ALP to CLP map
            if (!PARENTCLPMAP.containsKey(new Integer(this.fNumberOfClps)))
                throw new IllegalArgumentException(
                        "Wrong number of CLPs used");
            final int numberOfParentClps =
                    PARENTCLPMAP.get(new Integer(this.fNumberOfClps))
                            .intValue();
            LOG.finest("Number of parent CLPs: " + numberOfParentClps);
            final int numberOfAlpsPerParentClp =
                    this.fNumberOfAlps / numberOfParentClps;
            LOG.finest("Number of ALPs per parent CLP: "
                    + numberOfAlpsPerParentClp);
            int alpCounter = this.fNumberOfClps;
            for (int clpCounter = this.fNumberOfClps - numberOfParentClps; clpCounter < this.fNumberOfClps; clpCounter++) {
                for (int localAlpCounter = 0; localAlpCounter < numberOfAlpsPerParentClp; localAlpCounter++) {
                    writer.write("alp: " + alpCounter + " -> " + clpCounter);
                    writer.newLine();
                    alpCounter++;
                    if (alpCounter == this.fNumberOfClps + this.fNumberOfAlps)
                        break;
                }
                if (alpCounter == this.fNumberOfClps + this.fNumberOfAlps)
                    break;
            }
            // Add the SSVs
            Location minLocation =
                    new Location(Integer.MAX_VALUE, Integer.MAX_VALUE);
            Location maxLocation =
                    new Location(Integer.MIN_VALUE, Integer.MIN_VALUE);
            for (Identifier identifier : this.fSavedStateVariableMap.keySet()) {
                String type = "NULL";
                String value = "0";
                if (identifier.getVariable().getType() == Location.class) {
                    type = "POINT";
                    final Location location =
                            (Location) this.fSavedStateVariableMap
                                    .get(identifier).read(0).get();
                    value =
                            "<" + location.getX() + ":" + location.getY()
                                    + ">";
                    if (location.getX() < minLocation.getX())
                        minLocation =
                                new Location(location.getX(),
                                        minLocation.getY());
                    if (location.getY() < minLocation.getY())
                        minLocation =
                                new Location(minLocation.getX(),
                                        location.getY());
                    if (location.getX() > maxLocation.getX())
                        maxLocation =
                                new Location(location.getX(),
                                        maxLocation.getY());
                    if (location.getY() > maxLocation.getY())
                        maxLocation =
                                new Location(maxLocation.getX(),
                                        location.getY());
                } else if (identifier.getVariable().getType() == Double.class) {
                    type = "DOUBLE";
                    value =
                            "<"
                                    + ((Double) this.fSavedStateVariableMap
                                            .get(identifier).read(0).get())
                                            .doubleValue() + ">";
                } else if (identifier.getVariable().getType() == Integer.class) {
                    type = "INT";
                    value =
                            "<"
                                    + ((Integer) this.fSavedStateVariableMap
                                            .get(identifier).read(0).get())
                                            .intValue() + ">";
                } else if (identifier.getVariable().getType() == String.class) {
                    type = "STRING";
                    value =
                            "<"
                                    + ((String) this.fSavedStateVariableMap
                                            .get(identifier).read(0).get())
                                    + ">";
                }
                writer.write("ssv: 0, " + identifier.getObjectId() + ", "
                        + identifier.getVariable().getVariableId() + ", "
                        + type + ", " + value);
                writer.newLine();
            }
            // Add the CLP ranges
            for (int clpCounter = 0; clpCounter < this.fNumberOfClps; clpCounter++) {
                if (clpCounter == 0) {
                    writer.write("clp: 0, <" + minLocation.getX() + ","
                            + minLocation.getY() + ">, <"
                            + maxLocation.getX() + ":" + maxLocation.getY()
                            + ">");
                    writer.newLine();
                } else {
                    writer.write("clp: " + clpCounter + ", <"
                            + Integer.MAX_VALUE + ":" + Integer.MAX_VALUE
                            + ">, <" + Integer.MAX_VALUE + ":"
                            + Integer.MAX_VALUE + ">");
                    writer.newLine();
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (writer != null) {
                    writer.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
