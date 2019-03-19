package mwgrid.common;

import java.util.Random;

/**
 * @author Dr. B.G.W. Craenen
 */
public final class RandomNumberGenerator {
    private static final long DEFAULT_RANDOM_SEED = 1L;
    private static Random fRandom =
            new Random(RandomNumberGenerator.DEFAULT_RANDOM_SEED);
    
    /**
     * Private constructor
     */
    private RandomNumberGenerator() {
        // Private constructor
    }
    
    /**
     * @return (boolean) random boolean
     */
    public static boolean nextBoolean() {
        return RandomNumberGenerator.fRandom.nextBoolean();
    }
    
    /**
     * @return (double) random double [0.0d,1.0d>
     */
    public static double nextDouble() {
        return RandomNumberGenerator.fRandom.nextDouble();
    }
    
    /**
     * @return (float) random float [0.0f,1.0f>
     */
    public static float nextFloat() {
        return RandomNumberGenerator.fRandom.nextFloat();
    }
    
    /**
     * @return (double) random gaussian distributed double <-1.0d,1.0d>
     */
    public static double nextGaussian() {
        return RandomNumberGenerator.fRandom.nextGaussian();
    }
    
    /**
     * @return (int) random integer [Integer.MIN_VALUE,Integer.MAX_VALUE]
     */
    public static int nextInt() {
        return RandomNumberGenerator.fRandom.nextInt();
    }
    
    /**
     * @param pMaxValueExclusive
     *            - maximum value
     * @return (int) random integer [0,pMaxValueExclusive>
     */
    public static int nextInt(final int pMaxValueExclusive) {
        return RandomNumberGenerator.fRandom.nextInt(pMaxValueExclusive);
    }
    
    /**
     * @return (long) random long [Long.MIN_VALUE,Long.MAX_VALUE]
     */
    public static long nextLong() {
        return RandomNumberGenerator.fRandom.nextLong();
    }
    
    /**
     * @param pSeed
     *            - random number generator seed
     */
    public static void setSeed(final long pSeed) {
        RandomNumberGenerator.fRandom.setSeed(pSeed);
    }
}
