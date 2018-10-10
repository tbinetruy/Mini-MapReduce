import java.io.InputStream;
import java.io.BufferedInputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.util.ArrayList;
import java.io.IOException;
import java.lang.InterruptedException;

public class Deploy {
    public Deploy() {
        ArrayList<String> list_m = new ArrayList<>();
        ArrayList<Process> list_p = new ArrayList<>();
        list_m.add("c133-07");
        list_m.add("c133-08");
        list_m.add("c133-09");

        for(int i = 0; i < list_m.size(); i++) {
            ProcessBuilder pb = new ProcessBuilder("ssh",
                                                   "binetruy@" + list_m.get(i),
                                                   "hostname");

            try {
                Process p = pb.start();
                list_p.add(p);
            } catch (IOException e) {
                System.err.println("An error has occurred while starting the process " + Integer.toString(i) + ".");
            }
        }

        for(Process p : list_p) {
            try {
                p.waitFor();
            } catch(InterruptedException e) {
                System.err.println("An error has occurred while waiting for a process.");
            }
        }

        for(int i = 0; i < list_p.size(); i++) {
            Process p = list_p.get(i);
            String s = this.readOutput(p);
            String currentMachine = list_m.get(i);
            if(s.equals(currentMachine))
                System.out.println(currentMachine + ": connection working.");
            else
                System.out.println("Error: " + currentMachine + ": connection NOT working.");
        }
    }
    public String inputString2String(InputStream is, boolean isError) {
        BufferedInputStream bis = new BufferedInputStream(is);
        InputStreamReader isr = new InputStreamReader(is);
        BufferedReader br = new BufferedReader(isr);

        String line = "";
        try {
            line = br.readLine();
        } catch (IOException e) {
            System.err.println("Error while reading process output.");
        }

        return line;
    }
    public String readOutput(Process p) {
        InputStream is = p.getInputStream();
        InputStream is2 = p.getErrorStream();

        String stdout = this.inputString2String(is, false);
        String stderr = this.inputString2String(is2, true);

        if(stderr != null)
            return stderr;
        else
            return stdout;
    }
    public static void main(String[] args) {
        Deploy m = new Deploy();
    }
}
