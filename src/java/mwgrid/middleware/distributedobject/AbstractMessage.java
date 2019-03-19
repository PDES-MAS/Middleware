package mwgrid.middleware.distributedobject;

import java.util.Comparator;

/**
 * @author Dr B.G.W. Craenen <b.g.w.craenen@cs.bham.ac.uk>
 */
public abstract class AbstractMessage implements Message {
    public static class MessageComparator implements Comparator<Message> {
        /**
         * Constructor
         */
        public MessageComparator() {
            // Do Nothing
        }
        
        @Override
        public int compare(final Message pFirst, final Message pSecond) {
            return pFirst.getTime() - pSecond.getTime();
        }
    }
    
    protected static final String SEPERATOR = "#";
    protected static int numberOfElements = 3; 
    private long fDestination;
    private long fSource;
    private int fTime;
    
    /**
     * Constructor
     */
    public AbstractMessage() {
        this.fDestination = -1;
        this.fSource = -1;
        this.fTime = 0;
    }
    
    /**
     * Constructor
     * 
     * @param pDestination
     *            - destination
     * @param pSource
     *            - source
     * @param pTime
     *            - time
     */
    public AbstractMessage(final long pDestination,
            final long pSource, final int pTime) {
        this.fDestination = pDestination;
        this.fSource = pSource;
        this.fTime = pTime;
    }
    
    @Override
    public void convertFromString(final String pString) {
        final String[] string = pString.split(SEPERATOR);
        assert string.length == numberOfElements;
        this.fDestination = Long.parseLong(string[0]);
        this.fSource = Long.parseLong(string[1]);
        this.fTime = Integer.parseInt(string[2]);
    }
    
    @Override
    public String convertToString() {
        final StringBuilder result = new StringBuilder();
        result.append(this.fDestination);
        result.append(SEPERATOR);
        result.append(this.fSource);
        result.append(SEPERATOR);
        result.append(this.fTime);
        return result.toString();
    }
    
    @Override
    public long getDestination() {
        return this.fDestination;
    }
    
    @Override
    public long getSource() {
        return this.fSource;
    }
    
    @Override
    public int getTime() {
        return this.fTime;
    }
    
    @Override
    public Class<?> getType() {
        throw new IllegalAccessError();
    }
}
