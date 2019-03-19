package mwgrid.middleware.kernel;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import mwgrid.middleware.distributedobject.KernelVariables;
import mwgrid.middleware.distributedobject.Location;
import mwgrid.middleware.distributedobject.Value;
import mwgrid.middleware.distributedobject.Variable;
import mwgrid.middleware.exception.SSVAlreadyExistsException;
import mwgrid.middleware.exception.SSVNotFoundException;

/**
 * @author Dr B.G.W. Craenen
 */
public class SequentialSSVHandler implements SSVHandler {
    private static final Logger LOG = Logger
            .getLogger(SequentialSSVHandler.class.getPackage().getName());
    private int fNodeID;
    private final Map<Identifier, SavedStateVariable> fSavedStateVariableMap;
    private final Map<Long, Integer> fGlobalVariableTimeMap;
    
    /**
     * Constructor
     */
    public SequentialSSVHandler() {
        try {
            this.fNodeID = InetAddress.getLocalHost().hashCode();
            SequentialSSVHandler.LOG.finest("NodeID: " + this.fNodeID);
        } catch (final UnknownHostException e) {
            SequentialSSVHandler.LOG
                    .finest("Can't get local host, set NodeID to default (1)");
            this.fNodeID = 1;
        }
        this.fSavedStateVariableMap =
                new HashMap<Identifier, SavedStateVariable>();
        this.fGlobalVariableTimeMap = new HashMap<Long, Integer>();
    }
    
    @Override
    public void initialise(final String pDataLocation) {
        // Do nothing
    }
    
    @Override
    public boolean add(final long pObjectId, final Variable pVariable,
            final Value<?> pInitValue, final int pTime)
            throws SSVAlreadyExistsException {
        LOG.finest("Add, agent: " + pObjectId + ", variable: " + pVariable
                + ", value: " + pInitValue + ", time: " + pTime);
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
            LOG.finest("Finish, Add, agent: " + pObjectId + ", variable: "
                    + pVariable + ", value: " + pInitValue + ", time: "
                    + pTime);
        }
    }
    
    @Override
    public Value<?> read(final long pAgentId, final long pObjectId,
            final Variable pVariable, final int pTime)
            throws SSVNotFoundException {
        LOG.finest("Read, agent: " + pAgentId + ", objectID: " + pObjectId
                + ", variable: " + pVariable + ", time: " + pTime);
        try {
            // Create identifier
            final Identifier identifier =
                    new Identifier(pObjectId, pVariable);
            // If not exist, throw SSVNotFoundException
            if (!this.fSavedStateVariableMap.containsKey(identifier))
                throw new SSVNotFoundException();
            // Get SSV with correct SSVID
            final SavedStateVariable savedStateVariable =
                    this.fSavedStateVariableMap.get(identifier);
            // If exists, return value
            return savedStateVariable.read(pTime);
        } finally {
            LOG.finest("Finish read, agent: " + pAgentId + ", objectID: "
                    + pObjectId + ", variable: " + pVariable + ", time: "
                    + pTime);
        }
    }
    
    @Override
    public boolean write(final long pAgentId, final long pObjectId,
            final Variable pVariable, final Value<?> pValue, final int pTime)
            throws SSVNotFoundException {
        LOG.finest("write, agent: " + pAgentId + ", objectID: " + pObjectId
                + ", variable: " + pVariable + ", value: " + pValue.get()
                + ", time: " + pTime);
        try {
            // Create identifier
            final Identifier identifier =
                    new Identifier(pObjectId, pVariable);
            // If SSV not in map, throw SSVNotFoundException
            if (!this.fSavedStateVariableMap.containsKey(identifier))
                throw new SSVNotFoundException();
            // Remove the SSV from the map
            final SavedStateVariable savedStateVariable =
                    this.fSavedStateVariableMap.get(identifier);
            // Call write on SSV
            final boolean success = savedStateVariable.write(pTime, pValue);
            // Place SSV in map
            this.fSavedStateVariableMap.put(identifier, savedStateVariable);
            // Return success
            return success;
        } finally {
            LOG.finest("Finish write, agent: " + pAgentId + ", objectID: "
                    + pObjectId + ", variable: " + pVariable + ", value: "
                    + pValue.get() + ", time: " + pTime);
        }
    }
    
    @Override
    public Map<Long, Location> rangeQuery(final long pAgentId,
            final int pTime, final Location pStartLocation,
            final Location pEndLocation) {
        LOG.finest("rangeQuery, agent: " + pAgentId + ", time: " + pTime
                + ", start: " + pStartLocation + ", end: " + pEndLocation);
        try {
            // Instantiate result map
            final Map<Long, Location> result = new HashMap<Long, Location>();
            // For each saved state variable in the map
            for (final SavedStateVariable savedStateVariable : this.fSavedStateVariableMap
                    .values())
                // If the SSV is a LOCATION
                if (savedStateVariable.getIdentifier().getVariable()
                        .getVariableId() == KernelVariables.LOCATION
                        .getVariableId()) {
                    // Read the SSV value
                    final Location location =
                            (Location) savedStateVariable.read(pTime).get();
                    /*
                     * If the value is within range add the ObjectID|value in
                     * the map.
                     */
                    if (location.in(pStartLocation, pEndLocation))
                        result.put(new Long(savedStateVariable
                                .getIdentifier().getObjectId()), location);
                }
            return result;
        } finally {
            LOG.finest("Finish rangeQuery, agent: " + pAgentId + ", time: "
                    + pTime + ", start: " + pStartLocation + ", end: "
                    + pEndLocation);
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
        // Do nothing
    }
    
    @Override
    public boolean isInRollbackMap(final long pAgentId) {
        return false;
    }
    
    @Override
    public int getRollbackMapTime(final long pAgentId) {
        return -1;
    }
    
    @Override
    public void removeVariables(final long pObjectId) {
        LOG.finest("removeVariables, agent: " + pObjectId);
        try {
            for (Identifier identifier : this.fSavedStateVariableMap.keySet()) {
                if (identifier.getObjectId() == pObjectId) {
                    this.fSavedStateVariableMap.remove(identifier);
                }
            }
        } finally {
            LOG.finest("Finish, removeVariables, agent: " + pObjectId);
        }
    }
    
    @Override
    public boolean isClp() {
        return false;
    }
    
    @Override
    public boolean isAlp() {
        return true;
    }
    
    @Override
    public int getNumberOfAlps() {
        return 0;
    }
    
    @Override
    public int getRank() {
        return 0;
    }
    
    @Override
    public void setGVTTime(final long pObjectId, final int pTime) {
        LOG.finest("Set GVT time for agent: " + pObjectId + ", to time: "
                + pTime);
        this.fGlobalVariableTimeMap.put(new Long(pObjectId), new Integer(
                pTime));
    }
    
    @Override
    public int getGVTTime() {
        if (LOG.isLoggable(Level.FINEST)) {
            final StringBuilder result = new StringBuilder();
            result.append("{ ");
            for (Integer integer : this.fGlobalVariableTimeMap.values()) {
                result.append(integer.intValue());
                result.append(" ");
            }
            result.append("}");
            LOG.fine("Global Variable Map: " + result.toString());
        }
        return Collections.min(this.fGlobalVariableTimeMap.values())
                .intValue() - 1;
    }
    
    @Override
    public void sendGVTMessage() {
        return;
    }
}
