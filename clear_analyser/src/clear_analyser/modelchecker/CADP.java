package clear_analyser.modelchecker;

import clear_analyser.graph.Counterexample;
import clear_analyser.utils.STDOut;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;


public class CADP extends ModelChecker {
    private String commands = "";
    private String cexpFileName = "cexp";

    /**
     * If the counterexample LTS is not needed
     * @param fullLtsName
     * @param prop
     * @param baseDir
     * @param outputWriter
     */
    public CADP(String fullLtsName, String prop, String baseDir, STDOut
            outputWriter           ) {
        super(fullLtsName, prop, baseDir, outputWriter);
    }

    /**
     * If the counterexample LTS is needed
     * @param fullLtsName
     * @param cexpLtsName
     * @param prop
     * @param baseDir
     * @param outputWriter
     */
    public CADP(String fullLtsName, String cexpLtsName, String prop, String baseDir, STDOut
     outputWriter           ) {
        super(fullLtsName, cexpLtsName, prop, baseDir, outputWriter);
    }

    public String getCexpFileName() {
        return this.cexpFileName;
    }

    // FIXME: all part regarding bad graph generation is missing!!
    // Currently it needs to be generated before starting this tool

    @Override
    public void traceGenerator(int depth) {
        // bcg_open res.bcg executor ´depth val´ 2
        try {
            List<String> command = Arrays.asList("bcg_open" , this.getFullLtsName()+".bcg" , "executor", Integer.toString(depth)," 2 ");
            cmdCaller(command, this.getBaseDir()+"/"+this.cexpFileName+".seq", true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public Counterexample shortestCounterexampleGen(boolean safety) {
        // from Python: count_ex_cmd = 'bcg_open ' + model + ' evaluator4 -bfs -diag ' + curr_cexp + '.bcg ' + prop
        try {
            List<String> command = Arrays.asList("bcg_open" , this.getFullLtsName()+".bcg" , "evaluator4", "-bfs", "-diag",
                    this.cexpFileName+".bcg", this.getProp()+".mcl" );
            System.out.println("DEBUGGING: command : " + command);
            //cmdCaller(command, this.getBaseDir()+"/"+this.cexpFileName+".bcg", true);
            cmdCaller(command, null, true);
            bcgToAut(this.cexpFileName+".bcg", this.cexpFileName+".aut");  // FIXME: I do not like the use of extensions...
            Counterexample counterexample = new Counterexample(outputWriter);
            counterexample.autLoader(getBaseDir(), cexpFileName, false, safety);
            return counterexample;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * // TODO: not tested
     * @param depth maximum lenght of the generated trace, 0 for unlimited lenght
     * @param maxAttempts maximum number of attempts for the generation of a counterexample
     * @return a Counterexample object if the trace is a counterexample, null otherwise
     */
    @Override
    public Counterexample counterexampleGen(int depth, int maxAttempts, boolean safety) {
        try {
            int i;
            for(i=0;i<maxAttempts;i++) {
                // System.out.println("Attempt "+i+" to produce cexp");
                traceGenerator(depth);
                if (evaluateSeq()) {
                    // System.out.println("Cexp found");
                    Counterexample counterexample = new Counterexample(outputWriter);
                    counterexample.autLoader(getBaseDir(), cexpFileName, false, safety);
                    return counterexample;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } // if the trace is not a sequence, if the limit of attempts is exceeded, or if an exception is catched, return null
        return null;
    }

    @Override
    public boolean evaluateSeq() {
        // OLD seq.open res.seq evaluator4 -diag prop.mcl
        // NEW 'seq.open cexp.seq evaluator4 -bfs -diag ' + curr_cexp + '.bcg ' + prop + ' > log.tmp'
        boolean res = false;
        String logFile = "log.tmp";
        try {
            List<String> command = Arrays.asList("seq.open",  this.cexpFileName+".seq", "evaluator4", "-bfs", "-diag",
                    this.cexpFileName+".bcg", this.getProp()+".mcl" );
            cmdCaller(command, this.getBaseDir()+"/"+logFile, true);
            int seqres = cmdCaller(Arrays.asList("grep","TRUE", logFile), null, true);
            if (seqres==1) {
                // This is a counterexemple (bad)
                //cmdCaller(Arrays.asList("bcg_io", this.cexpFileName+".bcg", this.cexpFileName+".aut"), null, true);
                bcgToAut(this.cexpFileName+".bcg", this.cexpFileName+".aut");
                res = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return res;
    }

    // Conversion from .bcg file to .aut file
    public void bcgToAut(String bcgFileName, String autFileName) {
        //String extension = inputFile.substring(inputFile.length() - 3);
        //if (!extension.equals("bcg")) {
            // @TODO: throw exception
        //}
        List<String> cmdString = Arrays.asList("bcg_io", bcgFileName, "-aldebaran", autFileName);
        try {
            System.out.println("DEBUGGER : command: " + cmdString);
            cmdCaller(cmdString, null, true);
        }
        catch (IOException e){
            e.printStackTrace();
        }
    }

    // Conversion from .aut file to .bcg file
    public void autToBcg(String bcgFileName, String autFileName){
        //String extension = inputFile.substring(inputFile.length() - 3);
        //if (!extension.equals("aut")) {
        // @TODO: throw exception
        //}
        List<String> cmdString = Arrays.asList("bcg_io", autFileName, "-bcg", bcgFileName);
        System.out.println("DEBUG:[autToBcg]: " + cmdString);
        try {
            cmdCaller(cmdString, null, true);
        }
        catch (IOException e){
            e.printStackTrace();
        }
    }
}
