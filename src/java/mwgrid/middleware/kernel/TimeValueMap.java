/**
 * 
 */
package mwgrid.middleware.kernel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import mwgrid.common.FilePrint;
import mwgrid.middleware.distributedobject.Value;

/**
 * @author Dr B.G.W. Craenen <b.g.w.craenen@cs.bham.ac.uk>
 */
public class TimeValueMap {
    private static final Logger LOG = Logger.getLogger(TimeValueMap.class
            .getPackage().getName());
    private final Map<Integer, Value<?>> fTimeValueMap;
    
    /**
     * @param pTime
     *            - time
     * @param pValue
     *            - value
     */
    public TimeValueMap(final int pTime, final Value<?> pValue) {
        this.fTimeValueMap = new HashMap<Integer, Value<?>>();
        this.fTimeValueMap.put(new Integer(pTime), pValue);
    }
    
    /**
     * @param pTime
     *            - time
     * @return (Value<?>) value
     */
    public Value<?> get(final int pTime) {
        final int closestTime = this.getClosestTime(pTime);
        return this.fTimeValueMap.get(new Integer(closestTime));
    }
    
    /**
     * @param pTime
     *            - time
     * @param pValue
     *            - value
     */
    public void set(final int pTime, final Value<?> pValue) {
        assert !this.fTimeValueMap.containsKey(new Integer(pTime)) : "TimeValueMap already contains time!";
        this.fTimeValueMap.put(new Integer(pTime), pValue);
    }
    
    /**
     * @param pTime
     *            - time
     */
    public void removeMoreThan(final int pTime) {
        if (LOG.isLoggable(Level.FINEST)) {
            FilePrint.printToFile(Kernel.getSSVHandler().getRank(),
                FilePrint.Filename.TRACE, "removeMoreThan: at time: " + pTime
                        + ", before time-value map: " + this.toString());
        }
        final int closestTime = this.getClosestTime(pTime);
        // There should be a closest time for a rollback, so this assert should
        // still work
        assert closestTime != -1 : "Couldn't find closest time when there should be one! time: "
                + pTime + ", " + this.toString();
        final List<Integer> timesToRemove = new ArrayList<Integer>();
        for (Integer timeInMap : this.fTimeValueMap.keySet())
            if (timeInMap.intValue() > closestTime)
                timesToRemove.add(timeInMap);
        assert timesToRemove.size() < this.fTimeValueMap.keySet().size() : "Removing too many values!";
        for (Integer timeToRemove : timesToRemove) {
            assert this.fTimeValueMap.containsKey(timeToRemove) : "Time to remove is not in TimeValueMap!";
            if (LOG.isLoggable(Level.FINEST)) {
                FilePrint.printToFile(Kernel.getSSVHandler().getRank(),
                    FilePrint.Filename.TRACE,
                    "removeMoreThan: time: " + timeToRemove + ", value: "
                            + this.fTimeValueMap.get(timeToRemove));
            }
            this.fTimeValueMap.remove(timeToRemove);
        }
        assert !this.fTimeValueMap.isEmpty() : "TimeValueMap is empty!";
        assert this.fTimeValueMap.containsKey(new Integer(closestTime)) : "Closest time not contained in TimeValueMap!";
        if (LOG.isLoggable(Level.FINEST)) {
            FilePrint.printToFile(Kernel.getSSVHandler().getRank(),
                FilePrint.Filename.TRACE, "removeMoreThan: at time: " + pTime
                        + ", after time-value map: " + this.toString());
        }
    }
    
    /**
     * @param pGlobalVariableTime
     *            - time
     */
    public void removeLessThan(final int pGlobalVariableTime) {
        LOG.finest("time: " + pGlobalVariableTime);
        LOG.finest("before: " + this.toString());
        if (LOG.isLoggable(Level.FINEST)) {
            FilePrint.printToFile(Kernel.getSSVHandler().getRank(),
                FilePrint.Filename.TRACE,
                "removeLessThan: at time: " + pGlobalVariableTime
                        + ", time-value map: " + this.toString());
        }
        final int closestTime = this.getClosestTime(pGlobalVariableTime);
        // If there's no closest time it will return -1. In this case a previous
        // GVT has already removed all that needs to be removed and we can
        // continue on and do nothing.
        if (closestTime == -1) return;
        final List<Integer> timesToRemove = new ArrayList<Integer>();
        for (Integer timeInMap : this.fTimeValueMap.keySet())
            if (timeInMap.intValue() < closestTime)
                timesToRemove.add(timeInMap);
        assert timesToRemove.size() < this.fTimeValueMap.keySet().size() : "Removing too many values!";
        for (Integer timeToRemove : timesToRemove) {
            assert this.fTimeValueMap.containsKey(timeToRemove) : "Time to remove is not in TimeValueMap!";
            if (LOG.isLoggable(Level.FINEST)) {
                FilePrint.printToFile(Kernel.getSSVHandler().getRank(),
                    FilePrint.Filename.TRACE,
                    "removeLessThan: time: " + timeToRemove + ", value: "
                            + this.fTimeValueMap.get(timeToRemove));
            }
            this.fTimeValueMap.remove(timeToRemove);
        }
        assert !this.fTimeValueMap.isEmpty() : "TimeValueMap is empty!";
        assert this.fTimeValueMap.containsKey(new Integer(closestTime)) : "Closest time not contained in TimeValueMap!";
        if (closestTime != pGlobalVariableTime) {
            this.fTimeValueMap.put(new Integer(pGlobalVariableTime),
                this.fTimeValueMap.get(new Integer(closestTime)));
            if (LOG.isLoggable(Level.FINEST)) {
                FilePrint.printToFile(
                    Kernel.getSSVHandler().getRank(),
                    FilePrint.Filename.TRACE,
                    "removeLessThan: replace, time: "
                            + closestTime
                            + ", value: "
                            + this.fTimeValueMap
                                    .get(new Integer(closestTime)));
            }
            this.fTimeValueMap.remove(new Integer(closestTime));
        }
        LOG.finest("after: " + this.toString());
        if (LOG.isLoggable(Level.FINEST)) {
            FilePrint.printToFile(Kernel.getSSVHandler().getRank(),
                FilePrint.Filename.TRACE, "removeLessThan: at time: "
                        + pGlobalVariableTime + ", final time-value map: "
                        + this.toString());
        }
    }
    
    /**
     * @param pTime
     *            - time
     * @return (int) closest time
     */
    private int getClosestTime(final int pTime) {
        if (this.fTimeValueMap.containsKey(new Integer(pTime))) return pTime;
        int closestTime = -1;
        for (Integer timeInMap : this.fTimeValueMap.keySet()) {
            if (timeInMap.intValue() > closestTime
                    && timeInMap.intValue() < pTime)
                closestTime = timeInMap.intValue();
        }
        return closestTime;
    }
    
    @Override
    public String toString() {
        final StringBuilder result = new StringBuilder();
        result.append("TimeValueMap {");
        for (final Integer timeInMap : this.fTimeValueMap.keySet()) {
            result.append(" (" + timeInMap.intValue());
            result.append(", " + this.fTimeValueMap.get(timeInMap).toString());
            result.append("),");
        }
        result.deleteCharAt(result.length() - 1);
        result.append("}.");
        return result.toString();
    }
}
