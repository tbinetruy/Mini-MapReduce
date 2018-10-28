import java.util.ArrayList;
import java.io.IOException;
import java.util.stream.Collectors;
import java.io.BufferedInputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.InputStream;
import java.util.List;

public class Helpers {
    public Helpers() {
    }
    public ArrayList<String> getReachableMachines(ArrayList<String> list_m) {
        ArrayList<List<String>> arguments = new ArrayList<>();
        for(String m: list_m) {
            List<String> l = new ArrayList<String>();
            l.add("ssh");
            l.add("binetruy@" + m);
            l.add("hostname");
            arguments.add(l);
        }
        ArrayList<Process> list_p = this.parallelizeProcesses(arguments);
        ArrayList<String> list_reachable_m = new ArrayList<>();

        for(int i = 0; i < list_p.size(); i++) {
            Process p = list_p.get(i);
            String s = this.readOutput(p);
            String currentMachine = list_m.get(i);
            if(s.equals(currentMachine)) {
                System.out.println(currentMachine + ": connection working.");
                list_reachable_m.add(currentMachine);
            } else
                System.err.println("Error: " + currentMachine + ": connection NOT working.");
        }

        return list_reachable_m;
    }

    public ArrayList<Process> parallelizeProcesses(ArrayList<List<String>> arguments) {
        ArrayList<Process> list_p = new ArrayList<>();

        for(int i = 0; i < arguments.size(); i++) {
            ProcessBuilder pb = new ProcessBuilder();
            pb.command(arguments.get(i));

            try {
                Process p = pb.start();
                list_p.add(p);
            } catch (IOException e) {
                System.err.println("An error has occurred while starting the process " + Integer.toString(i) + ".");
                System.err.println(e);
            }
        }

        this.waitForProcesses(list_p);

        return list_p;
    }
    public void waitForProcesses(ArrayList<Process> list_p) {
        for(Process p : list_p) {
            try {
                p.waitFor();
            } catch(InterruptedException e) {
                System.err.println("An error has occurred while waiting for a process.");
            }
        }
    }
    public String inputStream2String(InputStream is, boolean isError) {
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

        String stdout = this.inputStream2String(is, false);
        String stderr = this.inputStream2String(is2, true);

        if(stderr != null)
            return stderr;
        else
            return stdout;
    }
}
