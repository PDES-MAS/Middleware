package mwgrid.middleware.kernel;

import mwgrid.common.EqualsUtil;
import mwgrid.common.HashCodeUtil;
import mwgrid.middleware.distributedobject.Variable;

public class Identifier {
    private final long fObjectId;
    private final Variable fVariable;
    
    /**
     * Constructor
     * 
     * @param pObjectId
     *            - object ID
     * @param pVariable
     *            - variable
     */
    public Identifier(final long pObjectId, final Variable pVariable) {
        this.fObjectId = pObjectId;
        this.fVariable = pVariable;
    }
    
    /**
     * @return (ObjectID) object ID
     */
    public long getObjectId() {
        return this.fObjectId;
    }
    
    /**
     * @return (Variable) variable
     */
    public Variable getVariable() {
        return this.fVariable;
    }
    
    @Override
    public boolean equals(final Object pObject) {
        if (this == pObject) return true;
        if (!(pObject instanceof Identifier)) return false;
        final Identifier that = (Identifier) pObject;
        return EqualsUtil.areEqual(this.fObjectId, that.fObjectId)
                && EqualsUtil.areEqual(this.fVariable.getVariableId(),
                    that.fVariable.getVariableId());
    }
    
    @Override
    public int hashCode() {
        int result = HashCodeUtil.SEED;
        result = HashCodeUtil.hash(result, this.fObjectId);
        result = HashCodeUtil.hash(result, this.fVariable.getVariableId());
        return result;
    }
}
