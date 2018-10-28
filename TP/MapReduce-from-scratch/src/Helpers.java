import java.util.ArrayList;
import java.io.IOException;
import java.util.stream.Collectors;
import java.io.BufferedInputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.InputStream;
import java.util.List;
import java.util.concurrent.TimeUnit;

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

        return list_p;
    }
    public ArrayList<Process> waitForProcesses(ArrayList<Process> list_p) {
        ArrayList<Process> successfulProcesses = new ArrayList<>();
        for(Process p : list_p) {
            try {
                p.waitFor();
                successfulProcesses.add(p);
            } catch(InterruptedException e) {
                System.err.println("An error has occurred while waiting for a process.");
            }
        }

        return successfulProcesses;
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
    public ArrayList<Process> timeoutProcesses(ArrayList<Process> list_p) {
        boolean wasProcessKilled = false;
        ArrayList<Integer> killed_processes = new ArrayList<>();
        long start = System.currentTimeMillis();
        long end = start + 14000;
        for(int i = 0; i < list_p.size(); i++) {
            Process p = list_p.get(i);
            long time = System.currentTimeMillis() - start;
            try {
                long deltaT = (end - start) / 1000;
                if(deltaT > 0) {
                    boolean timeout = p.waitFor(deltaT, TimeUnit.SECONDS);
                    if(!timeout) {
                        this.destroyProcess(p, i, killed_processes);
                    }
                } else {
                    // kill all other processes
                    this.destroyProcess(p, i, killed_processes);
                }
                start = System.currentTimeMillis();
            } catch(InterruptedException e) {
                System.err.println("An error has occurred while waiting for a process.");
            }
        }

        ArrayList<Process> list_p_success = new ArrayList<>();
        for(int i = 0; i < list_p.size(); i++) {
            if(!killed_processes.contains(i)) {
                list_p_success.add(list_p.get(i));
            }
        }

        return list_p_success;
    }
    public void destroyProcess(Process p, int i, ArrayList<Integer> killed_processes) {
        System.err.println("Timeout, destroying process " + Integer.toString(i, 10));
        p.destroy();
        killed_processes.add(i);
    }
}
