package mwgrid.common;

import java.lang.reflect.Array;

/**
 * Collected methods which allow easy implementation of <code>hashCode</code>.
 * Example use case:
 * 
 * <pre>
 * public int hashCode() {
 *     int result = HashCodeUtil.SEED;
 *     //collect the contributions of various fields
 *     result = HashCodeUtil.hash(result, fPrimitive);
 *     result = HashCodeUtil.hash(result, fObject);
 *     result = HashCodeUtil.hash(result, fArray);
 *     return result;
 * }
 * </pre>
 * 
 * @author Dr B.G.W. Craenen
 */
public final class HashCodeUtil {
    /**
     * An initial fValue for a <code>hashCode</code>, to which is added
     * contributions from fields. Using a non-zero fValue decreases collisons of
     * <code>hashCode</code> values.
     */
    public static final int SEED = 23;
    private static final int MAX = 32;
    private static final int ODD_PRIME_NUMBER = 37;
    
    /**
     * Private constructor
     */
    private HashCodeUtil() {
        // Do nothing
    }
    
    /**
     * @param pSeed
     *            - seed
     * @param pBoolean
     *            - boolean
     * @return (int) hash
     */
    public static int hash(final int pSeed, final boolean pBoolean) {
        if (pBoolean) return HashCodeUtil.firstTerm(pSeed) + 1;
        return HashCodeUtil.firstTerm(pSeed) + 0;
    }
    
    /**
     * @param pSeed
     *            - seed
     * @param pChar
     *            - char
     * @return (int) hash
     */
    @SuppressWarnings("cast")
    public static int hash(final int pSeed, final char pChar) {
        return HashCodeUtil.firstTerm(pSeed) + (int) pChar;
    }
    
    /**
     * @param pSeed
     *            - seed
     * @param pDouble
     *            - double
     * @return (int) hash
     */
    public static int hash(final int pSeed, final double pDouble) {
        return HashCodeUtil.hash(pSeed, Double.doubleToLongBits(pDouble));
    }
    
    /**
     * @param pSeed
     *            - seed
     * @param pFloat
     *            - float
     * @return (int) hash
     */
    public static int hash(final int pSeed, final float pFloat) {
        return HashCodeUtil.hash(pSeed, Float.floatToIntBits(pFloat));
    }
    
    /**
     * @param pSeed
     *            - seed
     * @param pInt
     *            - int
     * @return (int) hash
     */
    public static int hash(final int pSeed, final int pInt) {
        /*
         * Implementation Note: Note that byte and short are handled by this
         * method, through implicit conversion.
         */
        return HashCodeUtil.firstTerm(pSeed) + pInt;
    }
    
    /**
     * @param pSeed
     *            - seed
     * @param pLong
     *            - long
     * @return (int) hash
     */
    public static int hash(final int pSeed, final long pLong) {
        return HashCodeUtil.firstTerm(pSeed)
                + (int) (pLong ^ pLong >>> HashCodeUtil.MAX);
    }
    
    /**
     * <code>aObject</code> is a possibly-null fObject field, and possibly an
     * array. If <code>aObject</code> is an array, then each element may be a
     * primitive or a possibly-null fObject.
     * 
     * @param pSeed
     *            - seed
     * @param pObject
     *            - object
     * @return (int) hash
     */
    public static int hash(final int pSeed, final Object pObject) {
        int result = pSeed;
        if (pObject == null) result = HashCodeUtil.hash(result, 0);
        else if (!HashCodeUtil.isArray(pObject)) result =
                HashCodeUtil.hash(result, pObject.hashCode());
        else {
            final int length = Array.getLength(pObject);
            for (int idx = 0; idx < length; ++idx) {
                final Object item = Array.get(pObject, idx);
                // recursive call!
                result = HashCodeUtil.hash(result, item);
            }
        }
        return result;
    }
    
    /**
     * @param pSeed
     *            - seed
     * @return (int) first term
     */
    private static int firstTerm(final int pSeed) {
        return HashCodeUtil.ODD_PRIME_NUMBER * pSeed;
    }
    
    /**
     * @param pObject
     *            - object
     * @return (boolean) array?
     */
    private static boolean isArray(final Object pObject) {
        return pObject.getClass().isArray();
    }
}
