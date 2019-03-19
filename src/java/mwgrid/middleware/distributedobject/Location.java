package mwgrid.middleware.distributedobject;

import java.util.ArrayList;
import java.util.List;

import mwgrid.common.EqualsUtil;
import mwgrid.common.HashCodeUtil;

/**
 * @author bcraenen
 *
 */
public final class Location {
    public enum Neighbours {
        DOWN(-1, 0),
        DOWN_LEFT(-1, -1),
        DOWN_RIGHT(-1, 1),
        LEFT(0, -1),
        RIGHT(0, 1),
        UP(1, 0),
        UP_LEFT(1, -1),
        UP_RIGHT(1, 1);
        private int fXModifier;
        private int fYModifier;
        
        /**
         * @param pXModifier
         *            - fX modifier
         * @param pYModifier
         *            - fY modifier
         */
        private Neighbours(final int pXModifier, final int pYModifier) {
            this.fXModifier = pXModifier;
            this.fYModifier = pYModifier;
        }
        

            
        /**
         * @param pLocation
         *            - location
         * @return (Location) location
         */
        public Location getLocation(final Location pLocation) {
            return new Location(pLocation.getX() + this.fXModifier, pLocation
                    .getY()
                    + this.fYModifier);
        }
    }
    
    
    public enum MacroNeighbours {
        DOWN(-10, 0),
        DOWN_LEFT(-10, -10),
        DOWN_RIGHT(-10, 10),
        LEFT(0, -10),
        RIGHT(0, 10),
        UP(10, 0),
        UP_LEFT(10, -10),
        UP_RIGHT(10, 10);
        private int fXModifier;
        private int fYModifier;
        
        /**
         * @param pXModifier
         *            - fX modifier
         * @param pYModifier
         *            - fY modifier
         */
        private MacroNeighbours(final int pXModifier, final int pYModifier) {
            this.fXModifier = pXModifier;
            this.fYModifier = pYModifier;
        }
        

            
        /**
         * @param pLocation
         *            - location
         * @return (Location) location
         */
        public Location getLocation(final Location pLocation) {
            return new Location(pLocation.getX() + this.fXModifier, pLocation
                    .getY()
                    + this.fYModifier);
        }
    }
    
    
    public static final Location NULL_LOCATION = new Location(0, 0);
    private static final long serialVersionUID = 1L;
    private final int fX;
    private final int fY;
    
    /**
     * @param pX
     *            - fX
     * @param pY
     *            - fY
     */
    public Location(final int pX, final int pY) {
        this.fX = pX;
        this.fY = pY;
    }
    
    /**
     * @param pLocation - location as a string
     */
    public Location(final String pLocation) {
        final String[] vals = pLocation.split(":");
        this.fX = Integer.parseInt(vals[0].trim());
        this.fY = Integer.parseInt(vals[1].trim());
    }
    
    /**
     * @param pLocation
     *            - location
     */
    public Location(final Location pLocation) {
        this.fX = pLocation.fX;
        this.fY = pLocation.fY;
    }
    
    /**
     * @return (Location) copy
     */
    public Location copy() {
        return new Location(this);
    }
    
    /**
     * @return (int) fX
     */
    public int getX() {
        return this.fX;
    }
    
    /**
     * @return (int) fY
     */
    public int getY() {
        return this.fY;
    }
    
    /**
     * @param pToLocation
     *            - to location
     * @return (double) distance
     */
    public double distanceTo(final Location pToLocation) {
        return Math.sqrt(Math.pow(this.getX() - pToLocation.getX(), 2)
                + Math.pow(this.getY() - pToLocation.getY(), 2));
    }
    
    /**
     * @param pThis
     *            - this location
     * @param pThat
     *            - that location
     * @return (boolean) is in square between this and that location
     */
    public boolean in(final Location pThis, final Location pThat) {
        return this.fX >= Math.min(pThis.fX, pThat.fX)
                && this.fX <= Math.max(pThis.fX, pThat.fX)
                && this.fY >= Math.min(pThis.fY, pThat.fY)
                && this.fY <= Math.max(pThis.fY, pThat.fY);
    }
    
    /**
     * @return (List<Location>) neighbours
     */
    public List<Location> neighbours() {
        final List<Location> result = new ArrayList<Location>(8);
        for (final Neighbours neighbour : Neighbours.values())
            result.add(neighbour.getLocation(this));
        return result;
    }
   
    /**
     * @return (List<Location>) macro neighbours
     */
    public List<Location> macroNeighbours() {
        final List<Location> result = new ArrayList<Location>(8);
        for (final MacroNeighbours macroneighbour : MacroNeighbours.values())
            result.add(macroneighbour.getLocation(this));
        return result;
    }
    
    /**
     * @return List<Location> four neighbours
     */
    public List<Location> fourNeigbours() {
        final List<Location> result = new ArrayList<Location>(4);
        result.add(Neighbours.UP.getLocation(this));
        result.add(Neighbours.RIGHT.getLocation(this));
        result.add(Neighbours.DOWN.getLocation(this));
        result.add(Neighbours.LEFT.getLocation(this));
        return result;
    }
    
    @Override
    public boolean equals(final Object pObject) {
        if (this == pObject) return true;
        if (!(pObject instanceof Location)) return false;
        final Location that = (Location) pObject;
        return EqualsUtil.areEqual(this.fX, that.fX)
                && EqualsUtil.areEqual(this.fY, that.fY);
    }
    
    @Override
    public int hashCode() {
        int result = HashCodeUtil.SEED;
        result = HashCodeUtil.hash(result, this.fX);
        result = HashCodeUtil.hash(result, this.fY);
        return result;
    }
    
    @Override
    public String toString() {
        final StringBuilder result = new StringBuilder();
//        result.append("Location: (");
        result.append(this.fX + ":");
        result.append(this.fY);
//        result.append(").");
        return result.toString();
    }
}
