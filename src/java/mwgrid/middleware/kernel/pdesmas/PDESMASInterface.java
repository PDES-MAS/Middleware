package mwgrid.middleware.kernel.pdesmas;

import java.util.Map;

import mwgrid.middleware.distributedobject.Location;
import mwgrid.middleware.distributedobject.Value;
import mwgrid.middleware.exception.RollbackException;
import mwgrid.middleware.exception.SSVAlreadyExistsException;
import mwgrid.middleware.exception.SSVNotFoundException;
import mwgrid.middleware.kernel.Kernel;

public class PDESMASInterface {
    static {
        Runtime.getRuntime().load(System.getProperty("lib.mpi.path") + "/" + "libmpi_cxx.so.0");
        Runtime.getRuntime().load(System.getProperty("lib.pdesmas.path") + "/" + "libPDESMAS.so");
    }
    
    /**
     * Constructor
     */
    public PDESMASInterface() {
        // constructor
    }
    
    /**
     * Construct the interface
     * 
     * @param pNumberOfAlps
     *            - number of ALPs
     * @param pNumberOfClps
     *            - number of CLPs
     * @param pStartTime
     *            - start time
     * @param pEndTime
     *            - end time
     */
    public native void construct(final long pNumberOfAlps,
            final long pNumberOfClps, final long pStartTime,
            final long pEndTime);
    
    /**
     * Initialise the CLPs and the Agents/ALPs
     * 
     * @param pDataLocation
     *            - location of the data files
     */
    public native void initialise(final String pDataLocation);
    
    /**
     * Finalise interface and PDESMAS
     */
    public native void finalise();
    
    /**
     * Native method for getting the rank
     * 
     * @return (int) rank
     */
    public native int getRank();
    
    /**
     * Native method for getting the size
     * 
     * @return (int) size
     */
    public native int getSize();
    
    /**
     * Native method for getting the GVT
     * 
     * @return (int) Global Virtual Time
     */
    public native int getGVT();
    
    /**
     * Send a GVT Message to update GVT
     */
    public native void sendGVTMessage();
    
    /**
     * Native method for adding SSVs
     * 
     * @param pAgentId
     *            - agent ID
     * @param pObjectId
     *            - object ID
     * @param pVariable
     *            - variable
     * @param pInitValue
     *            - initial value
     * @param pTime
     *            - time
     * @return true if addtion successful, false otherwise
     * @throws SSVAlreadyExistsException
     *             - exception thrown when SSV already exists
     * @throws RollbackException
     *             - exception thrown when Rollback occurred
     */
    public native boolean add(final long pAgentId, final long pObjectId,
            final int pVariable, final Value<?> pInitValue,
            final int pTime) throws RollbackException,
            SSVAlreadyExistsException;
    
    /**
     * Native method for reading SSVs.
     * 
     * @param pAgentId
     *            - agent ID
     * @param pObjectId
     *            - object ID
     * @param pVariable
     *            - variable
     * @param pTime
     *            - time
     * @return (Value<?>) value of variable
     * @throws SSVNotFoundException
     *             - exception thrown when SSV is not found
     * @throws RollbackException
     *             - exception thrown when Rollback occurred
     */
    public native Value<?> read(final long pAgentId, final long pObjectId,
            final int pVariable, final int pTime)
            throws RollbackException, SSVNotFoundException;
    
    /**
     * Native method for writing SSVs.
     * 
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
     * @return true if write is successful, false otherwise
     * @throws SSVNotFoundException
     *             - exception thrown when SSV was not found
     * @throws RollbackException
     *             - exception thrown when rollback occurred
     */
    public native boolean write(final long pAgentId, final long pObjectId,
            final int pVariable, final int pValue, final int pTime)
            throws RollbackException, SSVNotFoundException;
    
    /**
     * Native method for writing SSVs.
     * 
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
     * @return true if write is successful, false otherwise
     * @throws SSVNotFoundException
     *             - exception thrown when SSV was not found
     * @throws RollbackException
     *             - exception thrown when a rollback occurred
     */
    public native boolean write(final long pAgentId, final long pObjectId,
            final int pVariable, final double pValue, final int pTime)
            throws RollbackException, SSVNotFoundException;
    
    /**
     * Native method for writing SSVs.
     * 
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
     * @return true if write is successful, false otherwise
     * @throws SSVNotFoundException
     *             - exception thrown when SSV was not found
     * @throws RollbackException
     *             - exception thrown when a rollback occurred
     */
    public native boolean write(final long pAgentId, final long pObjectId,
            final int pVariable, final Location pValue, final int pTime)
            throws RollbackException, SSVNotFoundException;
    
    /**
     * Native method for writing SSVs.
     * 
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
     * @return true if write is successful, false otherwise
     * @throws SSVNotFoundException
     *             - exception thrown when SSV was not found
     * @throws RollbackException
     *             - exception thrown when rollback occurred
     */
    public native boolean write(final long pAgentId, final long pObjectId,
            final int pVariable, final String pValue, final int pTime)
            throws RollbackException, SSVNotFoundException;
    
    /**
     * Native method for range-queries.
     * 
     * @param pAgentId
     *            - agent ID
     * @param pTime
     *            - time
     * @param pStartValue
     *            - start value
     * @param pEndValue
     *            - end value
     * @return (Map<ObjectID, Value<?>>) map of object IDs with values
     * @throws RollbackException
     *             - expception thrown when rollback occurred
     */
    public native Map<Long, Location> rangeQuery(final long pAgentId,
            final int pTime, final Location pStartValue,
            final Location pEndValue) throws RollbackException;
    
    /**
     * @param pAgentId
     *            - agent ID
     * @param pTime
     *            - time
     */
    public void handleRollback(final long pAgentId, final int pTime) {
        Kernel.getSSVHandler().handleRollback(pAgentId, pTime);
    }
}
