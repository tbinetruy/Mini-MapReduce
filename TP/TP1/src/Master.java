import java.io.InputStream;
import java.io.BufferedInputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.util.ArrayList;
import java.io.IOException;
import java.lang.InterruptedException;


public class Master {
    public Master() {
        ArrayList<String> list_m = new ArrayList<>();
        ArrayList<Process> list_p = new ArrayList<>();
        list_m.add("c133-07");
        list_m.add("c133-08");

        for(int i = 0; i < list_m.size(); i++) {
            ProcessBuilder pb = new ProcessBuilder("ssh",
                                                   "binetruy@" + list_m.get(i),
                                                   "java",
                                                   "-jar",
                                                   "/tmp/binetruy/Slave.jar");
            try {
                Process p = pb.start();
                list_p.add(p);
            } catch (IOException e) {
                System.err.println("An error has occurred while starting the process " + Integer.toString(1) + ".");
            }
        }

        for(Process p : list_p) {
            try {
                p.waitFor();
            } catch(InterruptedException e) {
                System.err.println("An error has occurred while waiting for a process.");
            }
        }

        for(Process p : list_p) {
            this.readOutput(p);
        }
    }
    public void inputString2String(InputStream is, boolean isError) {
        BufferedInputStream bis = new BufferedInputStream(is);
        InputStreamReader isr = new InputStreamReader(is);
        BufferedReader br = new BufferedReader(isr);

        try {
            String line = br.readLine();
            while(line != null) {
                if(!isError)
                    System.out.println(line);
                else
                    System.err.println("e: " + line);

                line = br.readLine();
            }
        } catch (IOException e) {
            System.err.println("Error while reading process output.");
        }
    }
    public void readOutput(Process p) {
        InputStream is = p.getInputStream();
        InputStream is2 = p.getErrorStream();

        this.inputString2String(is, false);
        this.inputString2String(is2, true);
    }
    public static void main(String[] args) {
        Master m = new Master();
    }
}
