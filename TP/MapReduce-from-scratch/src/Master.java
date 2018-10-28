// javac -d bin src/Master.java && cd bin && jar -cfm build/Master.jar Master.mf Master.class && cd ..
// javac -d bin src/Master.java


import java.io.BufferedInputStream;
import java.io.InputStreamReader;
import java.io.InputStream;
import java.io.BufferedReader;
import java.util.ArrayList;
import java.io.IOException;
import java.lang.InterruptedException;
import java.util.concurrent.TimeUnit;
import java.lang.IllegalThreadStateException;


public class Master {
    Helpers h;
    public Master() {
        this.h = new Helpers();

        ArrayList<String> list_m = new ArrayList<>();
        list_m.add("c133-07");
        list_m.add("c133-08");
        list_m.add("c133-09");

        ArrayList<Process> list_p = this.startProcesses(list_m);
        ArrayList<Process> list_p_new = this.timeoutProcesses(list_p);
        this.readOutput(list_p_new);
        System.out.println("finished");
    }
    public ArrayList<Process> startProcesses(ArrayList<String> list_m) {
        ArrayList<Process> list_p = new ArrayList<>();
        // start processes in parallel
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
                System.err.println("An error has occurred while starting the process " + Integer.toString(i) + ".");
            }
        }

        return list_p;
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
                    System.out.println("waiting");
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

        ArrayList<Process> list_p_new = new ArrayList<>();
        for(int i = 0; i < list_p.size(); i++) {
            if(!killed_processes.contains(i)) {
                list_p_new.add(list_p.get(i));
            }
        }

        return list_p_new;
    }
    public void destroyProcess(Process p, int i, ArrayList<Integer> killed_processes) {
        System.err.println("Timeout, destroying process " + Integer.toString(i, 10));
        p.destroy();
        killed_processes.add(i);
    }
    public void readOutput(ArrayList<Process> list_p) {
        for(Process p : list_p) {
            h.readOutput(p);
        }
    }
    public static void main(String[] args) {
        Master m = new Master();
    }
}
