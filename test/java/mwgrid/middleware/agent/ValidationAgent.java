/**
 * 
 */
package mwgrid.middleware.agent;

import java.util.Map;
import java.util.logging.Logger;

import mwgrid.common.RandomNumberGenerator;
import mwgrid.middleware.distributedobject.DistributedObject;
import mwgrid.middleware.distributedobject.KernelVariables;
import mwgrid.middleware.distributedobject.Location;
import mwgrid.middleware.distributedobject.Value;
import mwgrid.middleware.exception.RollbackException;

/**
 * @author Dr B.G.W. Craenen (b.g.w.craenen@cs.bham.ac.uk)
 */
public class ValidationAgent extends DistributedObject {
    private static final long serialVersionUID = 1L;
    private static final Logger LOG = Logger.getLogger(ValidationAgent.class
            .getPackage().getName());
    
    /**
     * @param pLocation
     *            - location
     */
    public ValidationAgent(final Location pLocation) {
        super(1);
        LOG.finest("Constructor");
        this.addVariable(KernelVariables.LOCATION, new Value<Location>(
                pLocation));
        this.addVariable(KernelVariables.PREVIOUS, new Value<Location>(pLocation));
    }
    
    @Override
    public void step() throws RollbackException {
        LOG.finest("Step function");
        final Location currentLocation =
                (Location) this.getVariable(KernelVariables.LOCATION).get();
        assert this.getTime() - 1 == currentLocation.getX() : "X is: "
                + currentLocation.getX() + ", should be: "
                + (this.getTime() - 1);
        assert this.getObjectId() - 1 == currentLocation.getY() : "Y is: "
                + currentLocation.getY() + ", should be: "
                + (this.getObjectId() - 1);
        final Map<Long, Location> agentMap =
                this.rangeQuery(new Location(currentLocation.getX() - 5,
                        currentLocation.getY() - 5), new Location(
                        currentLocation.getX() + 5,
                        currentLocation.getY() + 5));
        for (Long agentID : agentMap.keySet()) {
            LOG.finest("Agent: " + this.getObjectId() + ", at time: "
                    + this.getTime() + ", checks agent: "
                    + agentID.longValue() + ", for location: "
                    + agentMap.get(agentID).toString());
            final Location agentLocation = agentMap.get(agentID);
            /*
            assert this.getTime() - 1 == agentLocation.getX() : "X for agent is: "
                    + agentLocation.getX()
                    + ", should be: "
                    + (this.getTime() - 1);
            */
            assert agentID.longValue() - 1 == agentLocation.getY() : "Y for agent is: "
                    + agentLocation.getY()
                    + ", should be: "
                    + (agentID.longValue() - 1);
        }
        // Set previous location
        if (RandomNumberGenerator.nextInt(5) == 0) {
            this.setVariable(KernelVariables.PREVIOUS, new Value<Location>(new Location(currentLocation)));
        }
        // Move one to the right
        this.setVariable(KernelVariables.LOCATION,
            new Value<Location>(new Location(currentLocation.getX() + 1,
                    currentLocation.getY())));
        LOG.finest("Finished step, agent: " + this.getObjectId() + ", time: " + this.getTime());
    }
    
    @Override
    public String report() throws RollbackException {
        final StringBuilder report = new StringBuilder();
        final int time = this.getTime();
        report.append(time);
        report.append(" ");
        final long objectId = this.getObjectId();
        report.append(objectId);
        report.append(" ");
        final int classTypeId = this.getClassTypeId();
        report.append(classTypeId);
        report.append(" ");
        final Location location =
                (Location) this.getVariable(KernelVariables.LOCATION).get();
        report.append(location.getX());
        report.append(" ");
        report.append(location.getY());
        report.append(" ");
        return report.toString();
    }
}
