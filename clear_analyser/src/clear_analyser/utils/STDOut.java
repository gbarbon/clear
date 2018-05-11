package clear_analyser.utils;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;

public class STDOut {

    private BufferedWriter res = null;
    private BufferedWriter shortRes = null;
    private BufferedWriter neighbourhoods = null;
    private String path = "";



    public STDOut(String dir, String name) throws IOException {
        this.path = dir;
        this.res = new BufferedWriter(new FileWriter(dir+ "/res"+name+".log"));
        //this.shortRes = new BufferedWriter(new FileWriter(dir+ "/sres"+name+".log"));
        //this.neighbourhoods = new BufferedWriter(new FileWriter(dir+ "/nb.log"));
        printComplete("Test name: " + name, true, true);
        printComplete(new Date().toString(), true, true);
        //printComplete("Algorithm version: "+ algVersion, false);
    }

    public static boolean isEnabled(){
        return false;
    }

    public static void dbugLog(Object o){
        if(isEnabled()) {
            System.out.println("[DBUG] " + o.toString());
        }
    }

    public void print(String str, boolean stdout) throws IOException{
        printComplete(str,  true, stdout);
        printShort(str,  stdout);
    }

    public void printComplete(String str, boolean fileout, boolean stdout) {
        try {
            if (fileout)
                res.write(str + "\n");
            if (stdout)
                System.out.println(str);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void printError(String str, boolean fileout, boolean stdout) {
        try {
            if (fileout)
                res.write("error: "+str + "\n");
            if (stdout)
                System.err.println(str);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void printShort(String str, boolean stdout) throws IOException{
        shortRes.write(str + "\n");
        if (stdout)
            System.out.println(str);
    }

    public void printNb(String str, boolean stdout) throws IOException {
        neighbourhoods.write(str+"\n");
        if (stdout)
            System.out.println(str);
    }

    public void close()  {
        try {
            res.close();
            //shortRes.close();
            //neighbourhoods.close();
            }
        catch (IOException e) {
            e.printStackTrace();
        }
    }


}
