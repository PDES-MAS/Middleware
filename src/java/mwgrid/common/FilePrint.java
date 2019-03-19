package mwgrid.common;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.logging.Logger;

/**
 * @author Dr B.G.W. Craenen <b.g.w.craenen@cs.bham.ac.uk>
 */
public final class FilePrint {
    private static final Logger LOG = Logger.getLogger(FilePrint.class
            .getPackage().getName());
    private static String fDataLocation = "./resources/";
    
    public enum Filename {
        SSVH_ADD("SSVH_Add"),
        SSVH_RQ("SSVH_RQ"),
        SSVH_READ("SSVH_Read"),
        SSVH_WRITE("SSVH_Write"),
        SSVH_ROLLBACK("SSVH_Rollback"),
        SCHED_STEP("SCHED_Step"),
        SCHED_MEM("SCHED_Mem"),
        SCHED_ROLLBACK("SCHED_Rollback"),
        DO_ADD("DO_Add"),
        DO_GET("DO_Get"),
        DO_GET_ID("DO_GetID"),
        DO_SET("DO_Set"),
        DO_SET_ID("DO_SetID"),
        DO_RQ("DO_RQ"),
        DO_GETMESSAGE("DO_Get_Message"),
        DO_SENDMESSAGE("DO_Send_Message"),
        TRACE("Trace");
        private final String fFilename;
        
        /**
         * @param pFilename
         *            - filename
         */
        private Filename(final String pFilename) {
            this.fFilename = pFilename;
        }
        
        /**
         * @return (String) filename
         */
        public String get() {
            return this.fFilename;
        }
    }
    
    /**
     * Private constructor
     */
    private FilePrint() {
        // Private constructor
    }
    
    /**
     * Print a line to a file
     * 
     * @param pRank
     *            - rank
     * @param pFilename
     *            - filename
     * @param pLine
     *            - line to print
     */
    public static void printToFile(final int pRank, final Filename pFilename,
            final String pLine) {
        try {
            final BufferedWriter outputFile =
                    new BufferedWriter(new FileWriter(fDataLocation
                            + pFilename.get() + ".csv", true));
            outputFile.write(pLine);
            outputFile.newLine();
            outputFile.flush();
            outputFile.close();
        } catch (IOException e) {
            LOG.severe("IOException caught while writing to file");
            e.printStackTrace();
        }
    }
    
    /**
     * @param pDataLocation
     *            - data location to set
     */
    public static void setDataLocation(final String pDataLocation) {
        fDataLocation = pDataLocation;
    }
}
