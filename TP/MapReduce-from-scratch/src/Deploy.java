import java.io.InputStream;
import java.io.BufferedInputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.util.ArrayList;
import java.io.IOException;
import java.lang.InterruptedException;
import java.util.stream.Collectors;
import java.util.Arrays;

public class Deploy {
    public Deploy() {
        ArrayList<String> list_m = new ArrayList<>();
        list_m.add("c133-07");
        list_m.add("c133-08");
        list_m.add("c133-09");

        ArrayList<String> list_working_m = this.getReachableMachines(list_m);
        this.deploySlave(list_working_m);
    }
    public void deploySlave(ArrayList<String> list_m) {
        ArrayList<String> arguments = list_m.stream()
            .map(m -> "ssh binetruy@" + m + " mkdir -p /tmp/binetruy; scp Slave.jar binetruy@" + m + ":/tmp/binetruy/")
            .collect(Collectors.toCollection(ArrayList::new));
        ArrayList<Process> list_p = this.parallelizeProcesses(arguments);
        for(int i = 0; i < list_m.size(); i++) {
            Process p = list_p.get(i);
            String s = this.readOutput(p);
            if(s == null)
                System.out.println(list_m.get(i) +  ": Deploy successful");
            else
                System.err.println(s);
        }
    }
    public ArrayList<String> getReachableMachines(ArrayList<String> list_m) {
        ArrayList<String> arguments = list_m.stream()
            .map(m -> "ssh binetruy@" + m + " hostname")
            .collect(Collectors.toCollection(ArrayList::new));
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
    public ArrayList<Process> parallelizeProcesses(ArrayList<String> arguments) {
        ArrayList<Process> list_p = new ArrayList<>();

        for(int i = 0; i < arguments.size(); i++) {
            ProcessBuilder pb = new ProcessBuilder("bash", "-c", arguments.get(i));

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

        return list_p;
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
    public static void main(String[] args) {
        Deploy m = new Deploy();
    }
}
