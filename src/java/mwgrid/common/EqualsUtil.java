/**
 * 
 */
package mwgrid.common;

/**
 * Collected methods which allow easy implementation of <code>equals</code>.
 * Example use case in a class called Car:
 * 
 * <pre>
 * public boolean equals(Object aThat) {
 *     if (this == aThat) return true;
 *     if (!(aThat instanceof Car)) return false;
 *     Car that = (Car) aThat;
 *     return EqualsUtil.areEqual(this.fName, that.fName)
 *             &amp;&amp; EqualsUtil.areEqual(this.fNumDoors, that.fNumDoors)
 *             &amp;&amp; EqualsUtil.areEqual(this.fGasMileage, that.fGasMileage)
 *             &amp;&amp; EqualsUtil.areEqual(this.fColor, that.fColor)
 *             &amp;&amp; Arrays
 *                     .equals(this.fMaintenanceChecks, that.fMaintenanceChecks); //array!
 * }
 * </pre>
 * 
 * <em>Arrays are not handled by this class</em>. This is because the
 * <code>Arrays.equals</code> methods should be used for array fields.
 * 
 * @author Dr B.G.W. Craenen
 */
public final class EqualsUtil {
    /**
     * Private constructor
     */
    private EqualsUtil() {
        // Do nothing
    }
    
    /**
     * @param pThis
     *            - this
     * @param pThat
     *            - that
     * @return (boolean) equal
     */
    public static boolean areEqual(final boolean pThis, final boolean pThat) {
        return pThis == pThat;
    }
    
    /**
     * @param pThis
     *            - this
     * @param pThat
     *            - that
     * @return (boolean) equal
     */
    public static boolean areEqual(final char pThis, final char pThat) {
        return pThis == pThat;
    }
    
    /**
     * @param pThis
     *            - this
     * @param pThat
     *            - that
     * @return (boolean) equal?
     */
    public static boolean areEqual(final double pThis, final double pThat) {
        return Double.doubleToLongBits(pThis) == Double
                .doubleToLongBits(pThat);
    }
    
    /**
     * @param pThis
     *            - this
     * @param pThat
     *            - that
     * @return (boolean) equal?
     */
    public static boolean areEqual(final float pThis, final float pThat) {
        return Float.floatToIntBits(pThis) == Float.floatToIntBits(pThat);
    }
    
    /**
     * @param pThis
     *            - this
     * @param pThat
     *            - that
     * @return (boolean) equal?
     */
    public static boolean areEqual(final long pThis, final long pThat) {
        /*
         * Implementation Note: Note that byte, short, and int are handled by
         * this method, through implicit conversion.
         */
        return pThis == pThat;
    }
    
    /**
     * Possibly-null fObject field. Includes fType-safe enumerations and
     * collections, but does not include arrays. See class comment.
     * 
     * @param pThis
     *            - this
     * @param pThat
     *            - that
     * @return (boolean) equal?
     */
    public static boolean areEqual(final Object pThis, final Object pThat) {
        if (pThis == null) return pThat == null;
        return pThis.equals(pThat);
    }
}
