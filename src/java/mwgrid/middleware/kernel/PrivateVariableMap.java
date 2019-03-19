/**
 * 
 */
package mwgrid.middleware.kernel;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import mwgrid.middleware.distributedobject.Value;
import mwgrid.middleware.distributedobject.Variable;
import mwgrid.middleware.exception.InvalidVariableException;

/**
 * @author Dr B.G.W. Craenen <b.g.w.craenen@cs.bham.ac.uk>
 */
public class PrivateVariableMap {
    private static final Logger LOG = Logger.getLogger(PrivateVariableMap.class
        .getPackage().getName());
    private final Map<Variable, TimeValueMap> fPrivateVariableMap;
    
    /**
     * Constructor
     */
    public PrivateVariableMap() {
        this.fPrivateVariableMap = new HashMap<Variable, TimeValueMap>();
    }
    
    /**
     * Clear the private variable map
     */
    public void clear() {
        this.fPrivateVariableMap.clear();
    }
    
    /**
     * @param pVariable - variable
     * @return true if private variable map contains variable, false otherwise
     */
    public boolean containsVariable(final Variable pVariable) {
        return this.fPrivateVariableMap.containsKey(pVariable);
    }
    
    /**
     * @param pVariable - variable
     * @param pValue - value
     * @param pTime - time
     */
    public void add(final Variable pVariable, final Value<?> pValue, final int pTime) {
        if (this.fPrivateVariableMap.containsKey(pVariable))
            throw new InvalidVariableException();
        final TimeValueMap newTimeValueMap = new TimeValueMap(pTime, pValue);
        this.fPrivateVariableMap.put(pVariable, newTimeValueMap);
    }
    
    /**
     * @param pVariable - variable
     * @param pTime - time
     * @return (Value<?>) value
     */
    public Value<?> get(final Variable pVariable, final int pTime) {
        if (!this.fPrivateVariableMap.containsKey(pVariable))
            throw new InvalidVariableException();
        return this.fPrivateVariableMap.get(pVariable).get(pTime);
    }
    
    /**
     * @param pVariable - variable
     * @param pValue - value
     * @param pTime - time
     */
    public void set(final Variable pVariable, final Value<?> pValue, final int pTime) {
        if (!this.fPrivateVariableMap.containsKey(pVariable))
            throw new InvalidVariableException();
        assert null != this.fPrivateVariableMap.get(pVariable) : "TimeValueMap is null!";
        this.fPrivateVariableMap.get(pVariable).set(pTime, pValue);
    }
    
    /**
     * @param pGlobalVariableTime - time
     */
    public void removeLessThan(final int pGlobalVariableTime) {
        LOG.finest("RemoveLessThan with time: " + pGlobalVariableTime);
        for (TimeValueMap timeValueMap : this.fPrivateVariableMap.values()) {
            timeValueMap.removeLessThan(pGlobalVariableTime);
        }
        LOG.finest("Finished RemoveLessThan with time: " + pGlobalVariableTime);
    }
    
    /**
     * @param pTime - time
     */
    public void removeMoreThan(final int pTime) {
        for (TimeValueMap timeValueMap : this.fPrivateVariableMap.values())
            timeValueMap.removeMoreThan(pTime);
    }
    
    @Override
    public String toString() {
        final StringBuilder result = new StringBuilder();
        result.append("PrivateVariableMap {");
        for (final Variable variable : this.fPrivateVariableMap.keySet()) {
            result.append(" (" + variable.getVariableId());
            result.append(", " + this.fPrivateVariableMap.get(variable).toString());
            result.append("),");
        }
        result.deleteCharAt(result.length() - 1);
        result.append("}.");
        return result.toString();
    }
}
