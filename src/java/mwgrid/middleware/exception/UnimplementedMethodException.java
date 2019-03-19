/**
 * 
 */
package mwgrid.middleware.exception;

/**
 * @author Dr B.G.W. Craenen <b.g.w.craenen@cs.bham.ac.uk>
 *
 */
public class UnimplementedMethodException extends RuntimeException {
    private static final long serialVersionUID = 1L;
    
    /**
     * @param pMessage - message
     */
    public UnimplementedMethodException(final String pMessage) {
        super(pMessage);
    }
    
    /**
     * Constructor
     */
    public UnimplementedMethodException() {
        super();
    }
}
