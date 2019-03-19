package mwgrid.middleware.kernel;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.logging.Level;
import java.util.logging.Logger;

import mwgrid.common.FilePrint;
import mwgrid.middleware.distributedobject.PrivateVariable;
import mwgrid.middleware.distributedobject.PublicVariable;
import mwgrid.middleware.distributedobject.Value;
import mwgrid.middleware.distributedobject.Variable;
import mwgrid.middleware.exception.InvalidTypeException;
import mwgrid.middleware.exception.InvalidVariableException;
import mwgrid.middleware.exception.RollbackException;
import mwgrid.middleware.exception.SSVAlreadyExistsException;
import mwgrid.middleware.exception.SSVNotFoundException;

public class Statebase implements Serializable {
    private static final long serialVersionUID = 1L;
    private static final Logger LOG = Logger.getLogger(Statebase.class
            .getPackage().getName());
    private static long fObjectCounter = 1;
    // private static final long MAX_AGENTS = 1000000;
    // private final Map<Variable, Map<Integer, Value<?>>> fPrivateVariableMap;
    private final PrivateVariableMap fPrivateVariableMap;
    private final long fObjectID;
    private int fTime;
    private boolean fRolledBack;
    
    /**
     * Constructor
     */
    public Statebase() {
        this.fObjectID =
        /* Kernel.getSSVHandler().getRank() * MAX_AGENTS + */fObjectCounter++;
        // this.fPrivateVariableMap = new HashMap<Variable, Map<Integer,
        // Value<?>>>();
        this.fPrivateVariableMap = new PrivateVariableMap();
        this.fTime = 0;
        this.fRolledBack = false;
    }
    
    /**
     * @return (int) time;
     */
    public int getTime() {
        LOG.finest("Time on agent: " + this.fObjectID + ", is: " + this.fTime);
        return this.fTime;
    }
    
    /**
     * Increase time by one
     */
    public void increaseTime() {
        this.fTime++;
        LOG.finest("Time increased, on agent: " + this.fObjectID + ", now: "
                + this.fTime);
    }
    
    /**
     * @param pTime
     *            - time
     */
    public void setTime(final int pTime) {
        this.fTime = pTime;
        LOG.finest("Time set, on agent: " + this.fObjectID + ", now: "
                + this.fTime);
    }
    
    /**
     * This checks the rollback flag and also resets it
     * 
     * @return (boolean) has been rolled back?
     */
    public boolean resetRolledBackFlag() {
        if (this.fRolledBack) {
            this.fRolledBack = false;
            return true;
        }
        return false;
    }
    
    /**
     * Remove Statebase from reference lists, but keep the rest intact.
     */
    public void destroyStatebase() {
        this.fPrivateVariableMap.clear();
    }
    
    /**
     * @return (ObjectID) object ID
     */
    public long getObjectID() {
        return this.fObjectID;
    }
    
    /**
     * @param pVariable
     *            - variable
     * @param pValue
     *            - value
     */
    public void addVariable(final Variable pVariable, final Value<?> pValue) {
        // Check if Variable and Value type are equal
        if (!pVariable.getType().equals(pValue.getType()))
            throw new InvalidTypeException();
        // Check if annotated as private variable
        if (isPrivateVariable(pVariable)) {
            this.fPrivateVariableMap.add(pVariable, pValue, this.getTime());
            return;
        }
        // Register the new SSV with the kernel
        try {
            Kernel.getSSVHandler().add(this.fObjectID, pVariable, pValue,
                this.getTime());
        } catch (final SSVAlreadyExistsException e) {
            throw new InvalidVariableException("Variable address "
                    + pVariable + " already allocated for object "
                    + this.fObjectID);
        }
    }
    
    /**
     * @param pVariable
     *            - field ID
     * @return (Value<?>) fValue
     * @throws RollbackException
     *             - roll-back exception
     */
    public Value<?> getVariable(final Variable pVariable)
            throws RollbackException {
        LOG.finest("\t\tGet variable: " + pVariable + ", on agent: "
                + this.getObjectID() + ", at time: " + this.getTime());
        try {
            final Value<?> result;
            // Check if variable is private
            LOG.finest("\t\tCheck if: " + pVariable
                    + " is private on agent: " + this.getObjectID()
                    + ", at time: " + this.getTime());
            if (this.isPrivateVariable(pVariable)) {
                result =
                        this.fPrivateVariableMap.get(pVariable,
                            this.getTime());
            } else {
                // Get variable from Kernel
                LOG.finest("\t\t" + pVariable
                        + " is public, get variable from kernel on agent: "
                        + this.getObjectID() + ", at time: " + this.getTime());
                result = this.getKernelVariable(this.fObjectID, pVariable);
            }
            // Check if result isn't null
            assert null != result : "Value stored in private variable map is NULL!";
            // Check if Variable and Value type are equal
            LOG.finest("\t\tCheck if variable type and value type are the same for: "
                    + pVariable
                    + ", on agent: "
                    + this.getObjectID()
                    + ", at time: " + this.getTime());
            if (!pVariable.getType().equals(result.getType()))
                throw new InvalidTypeException("Expected type: "
                        + pVariable.getType() + " but got: "
                        + result.getType() + " instead.");
            // Return result
            LOG.finest("\t\tReturn result for variable: " + pVariable
                    + ", on agent: " + this.getObjectID() + ", at time: "
                    + this.getTime());
            return result;
        } finally {
            LOG.finest("\t\tFinished Getting variable: " + pVariable
                    + ", on agent: " + this.getObjectID() + ", at time: "
                    + this.getTime());
        }
    }
    
    /**
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
        // Check if variable is private
        if (this.isPrivateVariable(pVariable))
            throw new InvalidVariableException();
        // Initialise result
        final Value<?> result = this.getKernelVariable(pObjectId, pVariable);
        // Check if Variable and Value type are equal
        if (!pVariable.getType().equals(result.getType()))
            throw new InvalidTypeException();
        // Return result
        return result;
    }
    
    /**
     * @param pVariable
     *            - field ID
     * @param pValue
     *            - value
     * @throws RollbackException
     *             - roll-back exception
     */
    public void setVariable(final Variable pVariable, final Value<?> pValue)
            throws RollbackException {
        // Check if Variable and Value type is equal
        if (!pVariable.getType().equals(pValue.getType()))
            throw new InvalidTypeException();
        // Check if variable is private
        if (this.isPrivateVariable(pVariable)) {
            this.fPrivateVariableMap.set(pVariable, pValue, this.getTime());
        } else {
            // Set public variable in kernel
            this.setKernelVariable(this.fObjectID, pVariable, pValue);
        }
    }
    
    /**
     * @param pObjectId
     *            - object ID
     * @param pVariable
     *            - variable
     * @param pValue
     *            - value
     * @throws RollbackException
     *             - roll-back exception
     */
    public void setVariable(final long pObjectId, final Variable pVariable,
            final Value<?> pValue) throws RollbackException {
        // Check if variable is private
        if (this.isPrivateVariable(pVariable))
            throw new InvalidVariableException();
        // Check if Variable and Value type is equal
        if (!pVariable.getType().equals(pValue.getType()))
            throw new InvalidTypeException();
        this.setKernelVariable(pObjectId, pVariable, pValue);
    }
    
    /**
     * @param pRollbackTime
     *            - roll-back time Update private variable map by removing all
     *            values from roll-back time
     */
    public void rollBack(final int pRollbackTime) {
        LOG.finest("Rolling back agent: " + this.getObjectID() + ", to: "
                + pRollbackTime + ", at time: " + this.getTime());
        // Remove all from rollback time minus one
        this.fPrivateVariableMap.removeMoreThan(pRollbackTime);
        // Set time to rollback time
        this.setTime(pRollbackTime);
        // Set rolled back flag
        this.fRolledBack = true;
        LOG.finest("Finished rolling back agent: " + this.getObjectID()
                + ", time now at: " + this.getTime());
    }
    
    /**
     * Update private variable map to GVT time by removing all values up to GVT
     * 
     * @param pGlobalVariableTime
     *            - global variable time
     */
    public void cleanToGVT(final int pGlobalVariableTime) {
        LOG.finest("Clean to GVT: " + pGlobalVariableTime + ", on agent: "
                + this.getObjectID() + ", at time: " + this.getTime());
        if (pGlobalVariableTime < 1) return;
        // Remove all less than GVT
        this.fPrivateVariableMap.removeLessThan(pGlobalVariableTime);
        LOG.finest("Finished clean to GVT: " + pGlobalVariableTime
                + ", on agent: " + this.getObjectID() + ", at time: "
                + this.getTime());
    }
    
    @Override
    public String toString() {
        final StringBuilder result = new StringBuilder();
        result.append(this.getClass().getName() + " Object: {");
        result.append(" fObjectId: " + this.fObjectID);
        result.append(" fPrivateVariableMap: "
                + this.fPrivateVariableMap.toString());
        return result.toString();
    }
    
    /**
     * @param pVariable
     *            - variable
     * @return (boolean) is variable annotated as PrivateVariable?
     */
    private boolean isPrivateVariable(final Variable pVariable) {
        // Check if variable is declared in enum
        if (!pVariable.getClass().isEnum()) {
            LOG.warning("Variable is not declared in Enum, "
                    + "handling as public variable");
            return false;
        }
        // Get field
        Field field;
        try {
            field = pVariable.getClass().getField(pVariable.toString());
        } catch (SecurityException e) {
            LOG.warning("SecurityException thrown, "
                    + "handling as public variable");
            return false;
        } catch (NoSuchFieldException e) {
            LOG.warning("Variable field could not be found, "
                    + "handling as public variable");
            return false;
        }
        // Check if field is enum constant
        if (!field.isEnumConstant()) {
            LOG.warning("Variable field is not an enum constant, "
                    + "handling as public variable");
            return false;
        }
        // Check if field has correct annotations
        if (!field.isAnnotationPresent(PublicVariable.class)
                && !field.isAnnotationPresent(PrivateVariable.class)) {
            LOG.warning("Variable field is not annotated, "
                    + "handling as public variable");
            return false;
        }
        // Check if field has private variable annotation
        if (field.isAnnotationPresent(PrivateVariable.class)) {
            LOG.finest("Variable field is annotated as private variable");
            return true;
        }
        return false;
    }
    
    /**
     * @param pObjectId
     *            - object ID
     * @param pVariable
     *            - variable
     * @return (Value<?>) value
     * @throws RollbackException
     *             - roll-back exception
     */
    private Value<?> getKernelVariable(final long pObjectId,
            final Variable pVariable) throws RollbackException {
        // Initialise result
        Value<?> result;
        // Get value from kernel
        try {
            result =
                    Kernel.getSSVHandler().read(this.fObjectID, pObjectId,
                        pVariable, this.getTime() - 1);
        } catch (final SSVNotFoundException e) {
            throw new InvalidVariableException(e);
        }
        // Return result
        return result;
    }
    
    /**
     * @param pObjectId
     *            - object ID
     * @param pVariable
     *            - variable
     * @param pValue
     *            - value
     * @throws RollbackException
     *             - rollback exception
     */
    private void setKernelVariable(final long pObjectId,
            final Variable pVariable, final Value<?> pValue)
            throws RollbackException {
        boolean success = false;
        try {
            Statebase.LOG.finest("Called SSVHandler write; Time: "
                    + this.getTime() + "| objectId: " + pObjectId
                    + "| Variable: " + pVariable.toString() + "| Value: "
                    + pValue.toString());
            success =
                    Kernel.getSSVHandler().write(this.fObjectID, pObjectId,
                        pVariable, pValue, this.getTime());
        } catch (final SSVNotFoundException e) {
            throw new InvalidVariableException(e);
        }
        if (!success) {
            if (LOG.isLoggable(Level.FINEST)) {
                FilePrint.printToFile(
                    Kernel.getSSVHandler().getRank(),
                    FilePrint.Filename.TRACE,
                    "Statebase; Write unsuccessful, agent: "
                            + this.getObjectID() + ", time: "
                            + this.getTime());
            }
            throw new RollbackException(this.getObjectID(), this.getTime());
        }
    }
}
