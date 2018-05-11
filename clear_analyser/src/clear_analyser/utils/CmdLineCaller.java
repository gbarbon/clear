package clear_analyser.utils;

import java.io.*;
import java.util.List;

/**
 * Created by gbarbon.
 */
public class CmdLineCaller {

    public static int cmdCaller(List<String> commands, String outFile, String baseDir, boolean
                                error_on) throws IOException {
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
                } catch (FileNotFoundException |UnsupportedEncodingException e) {
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
