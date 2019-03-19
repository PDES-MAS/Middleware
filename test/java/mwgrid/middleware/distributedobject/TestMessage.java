/**
 * 
 */
package mwgrid.middleware.distributedobject;

import mwgrid.common.EqualsUtil;
import mwgrid.common.HashCodeUtil;

/**
 * @author Dr B.G.W. Craenen <b.g.w.craenen@cs.bham.ac.uk>
 */
public class TestMessage extends AbstractMessage {
    private Location fLocation;
    
    /**
     * Constructor
     */
    public TestMessage() {
        super();
        this.fLocation = null;
    }
    
    /**
     * @param pDestination
     *            - destination
     * @param pSource
     *            - source
     * @param pTime
     *            - time
     * @param pLocation
     *            - location
     */
    public TestMessage(final long pDestination,
            final long pSource, final int pTime,
            final Location pLocation) {
        super(pDestination, pSource, pTime);
        this.fLocation = pLocation;
    }
    
    @Override
    public void convertFromString(final String pString) {
        final String[] string = pString.split("#");
        assert string.length == 5;
        final StringBuilder tempString = new StringBuilder();
        for (int counter = 0; counter < 3; counter++) {
            tempString.append(string[counter]);
            if (counter != 2) tempString.append("#");
        }
        super.convertFromString(tempString.toString());
        this.fLocation =
                new Location(Integer.parseInt(string[3]), Integer
                        .parseInt(string[4]));
    }
    
    @Override
    public String convertToString() {
        final StringBuilder result = new StringBuilder();
        result.append(super.convertToString());
        result.append("#");
        result.append(this.fLocation.getX());
        result.append("#");
        result.append(this.fLocation.getY());
        return result.toString();
    }
    
    /**
     * @return (Location) location
     */
    public Location getLocation() {
        return this.fLocation;
    }
    
    @Override
    public Class<?> getType() {
        return TestMessage.class;
    }
    
    @Override
    public boolean equals(final Object pObject) {
        if (this == pObject) return true;
        if (!(pObject instanceof TestMessage)) return false;
        final TestMessage that = (TestMessage) pObject;
        return EqualsUtil.areEqual(this.getDestination(), that
                .getDestination())
                && EqualsUtil
                        .areEqual(this.getSource(), that.getSource())
                && EqualsUtil.areEqual(this.getTime(), that.getTime())
                && EqualsUtil.areEqual(this.getLocation(), that
                        .getLocation());
    }
    
    @Override
    public int hashCode() {
        int result = HashCodeUtil.SEED;
        result =
                HashCodeUtil.hash(result, this.getDestination());
        result = HashCodeUtil.hash(result, this.getSource());
        result = HashCodeUtil.hash(result, this.getTime());
        result = HashCodeUtil.hash(result, this.getLocation().hashCode());
        return result;
    }
}