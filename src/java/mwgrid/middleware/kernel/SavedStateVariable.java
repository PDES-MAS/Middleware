package mwgrid.middleware.kernel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import mwgrid.common.EqualsUtil;
import mwgrid.common.HashCodeUtil;
import mwgrid.middleware.distributedobject.Value;

/**
 * @author Dr B.G.W. Craenen
 */
public class SavedStateVariable {
    private class WritePeriod implements Comparable<WritePeriod> {
        private final int fTime;
        private final Value<?> fValue;
        
        /**
         * Constructor
         * 
         * @param pTime
         *            - time
         * @param pValue
         *            - value
         */
        public WritePeriod(final int pTime, final Value<?> pValue) {
            // Constructor
            this.fTime = pTime;
            this.fValue = pValue;
        }
        
        @Override
        public int compareTo(final WritePeriod pWritePeriod) {
            if (this.fTime < pWritePeriod.fTime) return -1;
            if (this.fTime > pWritePeriod.fTime) return 1;
            return 0;
        }
        
        @Override
        public boolean equals(final Object pObject) {
            if (this == pObject) return true;
            if (!(pObject instanceof WritePeriod)) return false;
            final WritePeriod that = (WritePeriod) pObject;
            return EqualsUtil.areEqual(this.fTime, that.fTime)
                    && EqualsUtil.areEqual(this.fValue, that.fValue);
        }
        
        /**
         * @return (int) time
         */
        public int getTime() {
            return this.fTime;
        }
        
        /**
         * @return (Value<?>) value
         */
        public Value<?> getValue() {
            return this.fValue;
        }
        
        @Override
        public int hashCode() {
            int result = HashCodeUtil.SEED;
            result = HashCodeUtil.hash(result, this.fTime);
            result = HashCodeUtil.hash(result, this.fValue);
            return result;
        }
    }
    
    private final Identifier fIdentifier;
    private final List<WritePeriod> fWritePeriodList;
    
    /**
     * Constructor
     * 
     * @param pIdentifier
     *            - identifier
     * @param pInitialValue
     *            - initial value
     * @param pTime
     *            - time
     */
    public SavedStateVariable(final Identifier pIdentifier,
            final Value<?> pInitialValue, final int pTime) {
        this.fIdentifier = pIdentifier;
        this.fWritePeriodList = new ArrayList<WritePeriod>();
        this.fWritePeriodList.add(new WritePeriod(pTime, pInitialValue));
    }
    
    /**
     * @return (Identifier) identifier
     */
    public Identifier getIdentifier() {
        return this.fIdentifier;
    }
    
    /**
     * @param pTime
     *            - time
     * @return (Value<?>) value
     */
    public Value<?> read(final int pTime) {
        // Sort the write period list
        Collections.sort(this.fWritePeriodList);
        // If time is before write period list initial time
        // throw IllegalArgumentException
        if (this.fWritePeriodList.get(0).getTime() > pTime)
            throw new IllegalArgumentException();
        Value<?> result = null;
        for (final WritePeriod writePeriod : this.fWritePeriodList) {
            result = writePeriod.getValue();
            // Test what happens when time = writePeriod.getTime()
            if (writePeriod.getTime() > pTime) break;
        }
        return result;
    }
    
    /**
     * @param pTime
     *            - time
     * @param pValue
     *            - value
     * @return (boolean) success?
     */
    public boolean write(final int pTime, final Value<?> pValue) {
        // Sort the write periods
        Collections.sort(this.fWritePeriodList);
        // If before initial time throw Illegal argument
        if (this.fWritePeriodList.get(0).getTime() > pTime)
            throw new IllegalArgumentException();
        // Check if there is a write for the same time,if so return false
        for (final WritePeriod writePeriod : this.fWritePeriodList)
            if (writePeriod.getTime() == pTime) return false;
        // Create new write period and add write period to list
        this.fWritePeriodList.add(new WritePeriod(pTime, pValue));
        // return true
        return true;
    }
}
