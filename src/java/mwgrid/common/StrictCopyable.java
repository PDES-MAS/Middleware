/**
 * 
 */
package mwgrid.common;

/**
 * <p>
 * Note:
 * <p>
 * This copyable interface is strict, in that it can only copy to the original
 * type.
 * 
 * @author Dr B.G.W. Craenen (b.g.w.craenen@cs.bham.ac.uk)
 */
public interface StrictCopyable<T extends StrictCopyable<T>> {
    /**
     * @return (Object) deep-copy
     */
    T copy();
}
