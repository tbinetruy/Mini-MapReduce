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
import java.util.HashMap;


public class Master {
    Helpers h;
    public Master() {
        this.h = new Helpers();

        ArrayList<String> list_m = new ArrayList<>();
        list_m.add("c133-07");
        list_m.add("c133-08");
        list_m.add("c133-09");

        HashMap<String, String> splitLocations = this.deploySplits(list_m);

        ArrayList<Process> list_p = this.runSlaves(splitLocations);
        ArrayList<Process> list_p_success = h.timeoutProcesses(list_p);
        this.readOutput(list_p_success);
        this.getMapLocations(splitLocations);
    }
    void getMapLocations(HashMap<String, String> splitLocations) {
        HashMap<String, String> mapLocations = new HashMap<>();
        for(String splitname: splitLocations.keySet()) {
            String absolutePath = "/tmp/binetruy/splits/";
            String mapname = "/tmp/binetruy/maps/UM" + splitname.substring(absolutePath.length() + 1);
            mapLocations.put(mapname, splitLocations.get(splitname));
            System.out.println(mapname + " - " + splitLocations.get(splitname));
        }
    }
    public HashMap<String, String> deploySplits(ArrayList<String> list_m) {
        ArrayList<String> list_working_m = h.getReachableMachines(list_m);

        ArrayList<List<String>> arguments = new ArrayList<>();
        HashMap<String, String> splitLocations = new HashMap<>();
        int numberOfSplits = 3;
        for(int i = 0; i < numberOfSplits; i++) {
            String machine = list_working_m.get(i % list_working_m.size());
            String splitName = "S" + i + ".txt";
            String cmd = "ssh binetruy@" + machine + " mkdir -p /tmp/binetruy/splits; scp " + splitName + " binetruy@" + machine + ":/tmp/binetruy/splits/";
            List<String> cmds = new ArrayList<String>();
            cmds.add("bash");
            cmds.add("-c");
            cmds.add(cmd);
            arguments.add(cmds);
            System.out.println(cmds);
            splitLocations.put("/tmp/binetruy/splits/" + splitName, machine);
        }

        ArrayList<Process> list_p = h.parallelizeProcesses(arguments);
        h.waitForProcesses(list_p);
        this.readOutput(list_p);

        return splitLocations;
    }
    public ArrayList<Process> runSlaves(HashMap<String, String> splitLocations) {
        ArrayList<Process> list_p = new ArrayList<>();
        ArrayList<List<String>> arguments = new ArrayList<>();

        // start processes in parallel
        for(String splitName: splitLocations.keySet()) {
            List<String> l = new ArrayList<>();
            l.add("ssh");
            l.add("binetruy@" + splitLocations.get(splitName));
            l.add("java");
            l.add("-jar");
            l.add("/tmp/binetruy/Slave.jar");
            l.add("0");
            l.add(splitName);
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
