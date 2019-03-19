package mwgrid.middleware.distributedobject;

/**
 * @author Dr B.G.W. Craenen (b.g.w.craenen@cs.bham.ac.uk>
 */
public interface Message {
    /**
     * @return (Class<?>) class;
     */
    Class<?> getType();
    
    /**
     * @return (ObjectID) source object ID
     */
    long getSource();
    
    /**
     * @return (ObjectID) destination object ID
     */
    long getDestination();
    
    /**
     * @return (int) time
     */
    int getTime();
    
    /**
     * @return (String) message converted into String
     */
    String convertToString();
    
    /**
     * @param pString
     *            - string to convert message from
     */
    void convertFromString(String pString);
}