/**
 * 
 */
package mwgrid.middleware.experiment;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Calendar;
import java.util.logging.Logger;

import mwgrid.common.RandomNumberGenerator;
import mwgrid.middleware.agent.ValidationAgent;
import mwgrid.middleware.distributedobject.Location;
import mwgrid.middleware.kernel.Kernel;
import mwgrid.middleware.kernel.SchedulerListener;

/**
 * @author Dr B.G.W. Craenen <b.g.w.craenen@cs.bham.ac.uk>
 */
public class TestExperiment implements SchedulerListener {
    private static final Logger LOG = Logger.getLogger(TestExperiment.class
            .getPackage().getName());
    private static int fNumberOfALPs = 4;
    private static int fNumberOfCLPs = 3;
    private static int fStartTime;
    private static int fEndTime = 500;
    private static int fNumberOfAgents = 4;
    private static boolean fRunDistributed = true;
    private static String fDataLocation = "./results/";
    private static final String OUTPUTFILENAME = "output.csv";
    private static final Location STARTLOCATION = new Location(0, 0);
    
    /**
     * Constructor
     */
    public TestExperiment() {
        LOG.finest("Declare kernel");
        if (fRunDistributed) Kernel.getDistributedInstance(this,
            fNumberOfALPs, fNumberOfCLPs, fStartTime, fEndTime);
        else Kernel.getSequentionalInstance(this, fEndTime);
        LOG.finest("Initialise data location");
        final long minutes = Calendar.getInstance().getTimeInMillis() / 60000;
        fDataLocation =
                "./results/" + minutes + "-" + fNumberOfCLPs + "-"
                        + fNumberOfALPs + "-" + fNumberOfAgents + "/"
                        + Kernel.getSSVHandler().getRank() + "/";
        final File dataLocationFile = new File(fDataLocation);
        if (!dataLocationFile.exists()) {
            dataLocationFile.mkdirs();
        } else {
            for (File file : dataLocationFile.listFiles()) {
                file.delete();
            }
        }
        LOG.finest("Initialise random number generator");
        RandomNumberGenerator.setSeed(0);
        LOG.finest("Initialise agents");
        for (int agentCounter = 0; agentCounter < fNumberOfAgents; agentCounter++) {
            final Location agentLocation =
                    new Location(STARTLOCATION.getX(), STARTLOCATION.getY()
                            + agentCounter);
            new ValidationAgent(agentLocation);
        }
        LOG.finest("Initialise kernel");
        Kernel.getSSVHandler().initialise(fDataLocation);
        LOG.finest("Start simulation");
        Kernel.startSimulation();
    }
    
    /**
     * @param pArguments
     *            - arguments
     */
    public static void main(final String[] pArguments) {
        assert pArguments.length == 6;
        fNumberOfALPs = Integer.parseInt(pArguments[0]);
        fNumberOfCLPs = Integer.parseInt(pArguments[1]);
        fStartTime = Integer.parseInt(pArguments[2]);
        fEndTime = Integer.parseInt(pArguments[3]);
        fNumberOfAgents = Integer.parseInt(pArguments[4]);
        fRunDistributed = Boolean.parseBoolean(pArguments[5]);
        new TestExperiment();
    }
    
    @Override
    public void collectReport(final long pAgentId, final int pTime,
            final String pReport) {
        try {
            final BufferedWriter outputFile =
                    new BufferedWriter(new FileWriter(fDataLocation
                            /*+ pAgentId + "-"*/ + OUTPUTFILENAME, true));
            outputFile.write(pReport);
            outputFile.newLine();
            outputFile.flush();
            outputFile.close();
        } catch (final IOException e) {
            LOG.severe("IOException caught while writing to output file");
            e.printStackTrace();
            System.exit(1);
        }
    }
}
