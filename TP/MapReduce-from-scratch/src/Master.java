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
    public Master() {
        ArrayList<String> list_m = new ArrayList<>();
        list_m.add("c133-07");
        list_m.add("c133-08");
        list_m.add("c133-09");

        ArrayList<Process> list_p = this.startProcesses(list_m);
        ArrayList<Integer> killed_processes = this.timeoutProcesses(list_p);
        this.readOutput(list_p, killed_processes);
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
    public ArrayList<Integer> timeoutProcesses(ArrayList<Process> list_p) {
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

        return killed_processes;
    }
    public void destroyProcess(Process p, int i, ArrayList<Integer> killed_processes) {
        System.err.println("Timeout, destroying process " + Integer.toString(i, 10));
        p.destroy();
        killed_processes.add(i);
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
    public void readOutput(ArrayList<Process> list_p, ArrayList<Integer> killed_processes) {
        // Read output of not-timed-out processes
        for(int i = 0; i < list_p.size(); i++) {
            Process p = list_p.get(i);
            if(!killed_processes.contains(i))
                this.readProcessOutput(p);
        }
    }
    public void readProcessOutput(Process p) {
        InputStream is = p.getInputStream();
        InputStream is2 = p.getErrorStream();

        this.inputString2String(is, false);
        this.inputString2String(is2, true);
    }
    public static void main(String[] args) {
        Master m = new Master();
    }
}
