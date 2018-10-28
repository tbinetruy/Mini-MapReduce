// javac -d bin src/Master.java && cd bin && jar -cfm build/Master.jar Master.mf Master.class && cd ..
// javac -d bin src/Master.java


import java.io.BufferedInputStream;
import java.io.InputStreamReader;
import java.io.InputStream;
import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.List;
import java.io.IOException;
import java.lang.InterruptedException;
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
        ArrayList<Process> list_p_new = h.timeoutProcesses(list_p);
        this.readOutput(list_p_new);
    }
    public ArrayList<Process> startProcesses(ArrayList<String> list_m) {
        ArrayList<Process> list_p = new ArrayList<>();
        ArrayList<List<String>> arguments = new ArrayList<>();

        // start processes in parallel
        for(int i = 0; i < list_m.size(); i++) {
            List<String> l = new ArrayList<>();
            l.add("ssh");
            l.add("binetruy@" + list_m.get(i));
            l.add("java");
            l.add("-jar");
            l.add("/tmp/binetruy/Slave.jar");
            arguments.add(l);
        }

        return h.parallelizeProcesses(arguments);
    }
    public void readOutput(ArrayList<Process> list_p) {
        for(Process p : list_p) {
            System.out.println(h.readOutput(p));
        }
    }
    public static void main(String[] args) {
        Master m = new Master();
    }
}
