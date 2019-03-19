/**
 * 
 */
package mwgrid.middleware.agent;

import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import mwgrid.common.RandomNumberGenerator;
import mwgrid.middleware.distributedobject.DistributedObject;
import mwgrid.middleware.distributedobject.KernelVariables;
import mwgrid.middleware.distributedobject.Location;
import mwgrid.middleware.distributedobject.Value;
import mwgrid.middleware.exception.RollbackException;

/**
 * @author Dr B.G.W. Craenen <b.g.w.craenen@cs.bham.ac.uk>
 */
public class TestAgent extends DistributedObject {
    private static final long serialVersionUID = 1L;
    private static final Logger LOG = Logger.getLogger(TestAgent.class
            .getPackage().getName());
    
    /**
     * Constructor
     * 
     * @param pLocation
     *            - location
     */
    public TestAgent(final Location pLocation) {
        super(1);
        LOG.finest("Constructor");
        this.addVariable(KernelVariables.LOCATION, new Value<Location>(
                pLocation));
    }
    
    @Override
    public void step() throws RollbackException {
        LOG.finest("Step function");
        final Location currentLocation =
                (Location) this.getVariable(KernelVariables.LOCATION).get();
        final Map<Long, Location> agentMap =
                this.rangeQuery(new Location(currentLocation.getX() - 1,
                        currentLocation.getY() - 1), new Location(
                        currentLocation.getX() + 1,
                        currentLocation.getY() + 1));
        final List<Location> neighbourLocations =
                currentLocation.neighbours();
        neighbourLocations.removeAll(agentMap.values());
        if (!neighbourLocations.isEmpty()) {
            this.setVariable(
                KernelVariables.LOCATION,
                new Value<Location>(neighbourLocations
                        .get(RandomNumberGenerator.nextInt(neighbourLocations
                                .size()))));
        }
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
        final Location location = (Location) this.getVariable(KernelVariables.LOCATION).get();
        report.append(location.getX());
        report.append(" ");
        report.append(location.getY());
        report.append(" ");
        return report.toString();
    }
}
