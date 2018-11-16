package clear_analyser.modelchecker;

import clear_analyser.graph.Counterexample;
import clear_analyser.utils.STDOut;

import java.io.*;
import java.util.List;


public abstract class ModelChecker {

    private String fullLtsName;
    private String cexpLtsName;
    private String prop;
    private String baseDir;
    STDOut outputWriter;

    /**
     * If the counterexample TLS is not needed
     * @param fullLtsName
     * @param prop
     * @param baseDir
     * @param outputWriter
     */
    public ModelChecker(String fullLtsName, String prop, String baseDir, STDOut outputWriter) {
        this.fullLtsName = fullLtsName;
        this.prop = prop;
        this.baseDir = baseDir;
        this.outputWriter = outputWriter;
    }

    /**
     * If the counterexample LTS is needed
     * @param fullLtsName
     * @param cexpLtsName
     * @param prop
     * @param baseDir
     * @param outputWriter
     */
    public ModelChecker(String fullLtsName, String cexpLtsName, String prop, String baseDir,
                        STDOut outputWriter) {
        this.fullLtsName = fullLtsName;
        this.cexpLtsName = cexpLtsName;
        this.prop = prop;
        this.baseDir = baseDir;
        this.outputWriter = outputWriter;
    }

    public abstract void traceGenerator(int depth);
    public abstract boolean evaluateSeq();
    public abstract Counterexample shortestCounterexampleGen(boolean safety);
    public abstract Counterexample counterexampleGen(int depth, int maxAttempts, boolean safety);
    //public abstract void ltsGenerator();
    //public abstract LTS badLtsGenerator(String path);
    //public abstract LTS badLtsUser(String path);
    //public abstract void stupidTerminalCaller();
    //public abstract void prunedLtsGenerator();

    /**
    public LTS getLTS() {
        return this.graph;
    }
    public void setGraph(LTS graph) {
        this.graph = graph;
    }*/

    public String getBaseDir() {
        return baseDir;
    }
    public String getFullLtsName() {
        return fullLtsName;
    }
    public String getCexpLtsName() {
        return cexpLtsName;
    }
    public String getProp() {
        return prop;
    }

    public int cmdCaller(List<String> commands, String outFile, boolean error_on) throws IOException {
        ProcessBuilder pb = new ProcessBuilder(commands); // creating the process

        // setting correct directory
        File dirPath = new File(baseDir);
        pb.directory(dirPath);
        pb.environment();

        Process process = pb.start();  // starting process

        // printing command line output
        boolean outToFile = (outFile != null && !outFile.isEmpty());
        new Thread(new Runnable() {
            public void run() {
                PrintWriter writer = null;
                try {
                    if (outToFile)
                        writer = new PrintWriter(outFile, "UTF-8");
                } catch (FileNotFoundException|UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                String line;
                BufferedReader stdoutReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                try {
                    while ((line = stdoutReader.readLine()) != null) {
                        // process procs standard output here
                        if (outToFile)
                            writer.println(line);
                        //else   // comment line for output bonification
                        //    System.out.println(line);
                    }
                    if (outToFile)
                        writer.close();
                } catch (NullPointerException|IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();

        // printing command line errrors
        new Thread(new Runnable() {
            public void run() {
                String line;
                BufferedReader stderrReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
                try {
                    while ((line = stderrReader.readLine()) != null) {
                        // process procs standard error here
                        if (error_on)
                            System.out.println("        " + commands.get(0) + " error: " + line);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();

        // waiting for the end of the process
        int exitValue = 1;
        try {
            exitValue = process.waitFor();
            //if (exitValue!=0)
                //System.out.println("        " + commands.get(0) + " exit value: " + exitValue);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return exitValue;
    }

}
