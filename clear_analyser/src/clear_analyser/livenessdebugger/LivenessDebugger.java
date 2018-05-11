package clear_analyser.livenessdebugger;

import clear_analyser.graph.LTS;
import clear_analyser.utils.STDOut;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.List;

/**
 * Created by gbarbon.
 */
public abstract class LivenessDebugger {
    protected String propFile;
    protected List<String> actions;
    protected LTS fullLTS;
    protected String baseDir;
    protected STDOut writer;

    public LivenessDebugger(String propFile, LTS fullLts, String baseDir, STDOut
            writer) throws Exception {
        this.propFile = propFile;
        this.fullLTS = fullLts;
        this.actions = null;
        this.baseDir = baseDir;
        this.writer = writer;
        simplePropertyParser();  // extraction action in the property
    }

    public LivenessDebugger(List<String> actions, LTS fullLts, STDOut writer) throws
            Exception {
        this.actions = actions;
        this.fullLTS = fullLts;
        this.writer = writer;
    }

    /**
     * Constructor used for deadlock and livelock detection
     * @param fullLts
     * @param writer
     * @throws Exception
     */
    public LivenessDebugger(LTS fullLts, STDOut writer) throws
            Exception {
        this.fullLTS = fullLts;
        this.writer = writer;
    }


    /**
     * TODO: implement parsing of the property (only with inevitability of a single action)
     */
    private void simplePropertyParser() throws Exception {
        // this.action = "";
        BufferedReader br = new BufferedReader(new FileReader(baseDir + "/" + propFile + ".mcl"));
        // TODO: check that the file is actually an mcl file
        // TODO: add exception in case of missing file
        String line;
        while ((line = br.readLine()) != null) {

            // exploiting macros of standard.mcl :
            if (line.toLowerCase().contains("INEVITABLE".toLowerCase())) {
                int firstBraket = line.indexOf("(");
                int lastBraket = line.lastIndexOf(")");
                //this.action = line.substring(firstBraket + 1, lastBraket).replace("\"", "")
                // .trim();
                // TODO: collect all the sequence of action
            }
            // TODO: add a String array to handle multiple inevitability

        }
        br.close();

        //writer.printComplete("simplePropertyParser: action is " + this.action + " ", true, true);
    }

    public abstract void executor();
}
