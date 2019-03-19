package mwgrid.middleware.kernel;

import java.util.Map;

import mwgrid.middleware.distributedobject.Location;
import mwgrid.middleware.distributedobject.Value;
import mwgrid.middleware.distributedobject.Variable;
import mwgrid.middleware.exception.RollbackException;
import mwgrid.middleware.exception.SSVAlreadyExistsException;
import mwgrid.middleware.exception.SSVNotFoundException;

/**
 * @author Dr B.G.W. Craenen
 */
public interface SSVHandler {
    /**
     * Initialise SSVHandler
     * 
     * @param pDataLocation
     *            - location of the data
     */
    void initialise(final String pDataLocation);
    
    /**
     * @param pObjectId
     *            - object ID
     * @param pVariable
     *            - variable
     * @param pInitValue
     *            - initial value
     * @param pTime
     *            - time
     * @return (boolean) success
     * @throws SSVAlreadyExistsException
     *             - thrown if SSV already exists
     */
    boolean add(long pObjectId, Variable pVariable, Value<?> pInitValue,
            int pTime) throws SSVAlreadyExistsException;
    
    /**
     * @param pAgentId
     *            - agent ID
     * @param pObjectId
     *            - object ID
     * @param pVariable
     *            - variable
     * @param pTime
     *            - time
     * @return (Value<?>) value
     * @throws SSVNotFoundException
     *             - thrown if SSV is not found
     * @throws RollbackException
     *             - thrown if rollback occurs
     */
    Value<?> read(long pAgentId, long pObjectId, Variable pVariable, int pTime)
            throws RollbackException, SSVNotFoundException;
    
    /**
     * @param pAgentId
     *            - agent ID
     * @param pObjectId
     *            - object ID
     * @param pVariable
     *            - variable
     * @param pValue
     *            - value
     * @param pTime
     *            - time
     * @return (boolean) success?
     * @throws SSVNotFoundException
     *             - thrown if SSV not found
     * @throws RollbackException
     *             - thrown if rollback occurs
     */
    boolean write(long pAgentId, long pObjectId, Variable pVariable,
            Value<?> pValue, int pTime) throws RollbackException,
            SSVNotFoundException;
    
    /**
     * @param pAgentId
     *            - agent ID
     * @param pTime
     *            - time
     * @param pStartLocation
     *            - start location
     * @param pEndLocation
     *            - end location
     * @return (Map<Integer, Value<?>>) map of values
     * @throws RollbackException
     *             - exception thrown when rollback occurs
     */
    Map<Long, Location> rangeQuery(long pAgentId, int pTime,
            Location pStartLocation, Location pEndLocation)
            throws RollbackException;
    
    /**
     * @param pAgentId
     *            - agent ID
     * @param pTime
     *            - time
     */
    void handleRollback(final long pAgentId, final int pTime);
    
    /**
     * @param pAgentId
     *            - agent ID
     * @return (boolean) is in roll-back map?
     */
    boolean isInRollbackMap(final long pAgentId);
    
    /**
     * @param pAgentId
     *            - agent ID
     * @return (int) time in roll-back map
     */
    int getRollbackMapTime(final long pAgentId);
    
    /**
     * @param pObjectId
     *            - object ID
     */
    void removeVariables(long pObjectId);
    
    /**
     * @return (boolean) is CLP?
     */
    boolean isClp();
    
    /**
     * @return (boolean) is ALP?
     */
    boolean isAlp();
    
    /**
     * @return (int) number of ALPs
     */
    int getNumberOfAlps();
    
    /**
     * @return (int) rank
     */
    int getRank();
    
    /**
     * @param pObjectId
     *            - object ID
     * @param pTime
     *            - time
     */
    void setGVTTime(long pObjectId, int pTime);
    
    /**
     * @return (int) GVT time
     */
    int getGVTTime();
    
    /**
     * Send GVT message
     */
    void sendGVTMessage();
}
