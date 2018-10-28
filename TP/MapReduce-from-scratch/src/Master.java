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
import java.util.Arrays;


public class Master {
    Helpers h;
    ArrayList<String> list_m;
    public Master() {
        this.h = new Helpers();

        this.list_m = new ArrayList<>();
        this.list_m.add("c133-07");
        this.list_m.add("c133-08");
        this.list_m.add("c133-09");

        HashMap<String, String> splitLocations = this.deploySplits(this.list_m);

        HashMap<String, Process> slavesLocationMap = this.runSlaves(splitLocations);
        ArrayList<Process> list_p = new ArrayList<>();
        for(String key: slavesLocationMap.keySet()) {
            list_p.add(slavesLocationMap.get(key));
        }

        ArrayList<Process> list_p_success = h.timeoutProcesses(list_p);
        HashMap<String, ArrayList<String>> keyUMMap = this.getKeyUMMap(slavesLocationMap);
        this.printKeyUMMap(keyUMMap);
        this.getMapLocations(splitLocations);

        System.out.println("Phase de MAP terminée.");
    }
    void printKeyUMMap(HashMap<String, ArrayList<String>> map) {
        for(String key: map.keySet()) {
            String str = key + " - < ";
            for(String UM: map.get(key)) {
                str = str.concat(UM).concat(" ");
            }
            str = str.concat(">").replace("\n", " ");
            System.out.println(str);
        }
    }
    HashMap<String, ArrayList<String>> getKeyUMMap(HashMap<String, Process> map) {
        HashMap<String, ArrayList<String>> UMKeyMap = new HashMap<>();
        for(String key: map.keySet()) {
            ArrayList<Process> list_p = new ArrayList<>();
            list_p.add(map.get(key));
            System.out.println(key);
            ArrayList<String> outputs = new ArrayList<String>(Arrays.asList(this.readOutput(list_p).get(0).split("\n")));
            UMKeyMap.put(key, outputs);
        }

        HashMap<String, ArrayList<String>> keyUMMap = new HashMap<>();
        for(String UM: UMKeyMap.keySet()) {
            for(String word: UMKeyMap.get(UM)) {
                if(keyUMMap.get(word) == null) {
                    ArrayList<String> UMList = new ArrayList<>();
                    UMList.add(UM.replace("/S", "/UM"));
                    keyUMMap.put(word, UMList);
                } else {
                    keyUMMap.get(word).add(UM.replace("/S", "/UM"));
                }
            }
        }

        return keyUMMap;
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
            splitLocations.put("/tmp/binetruy/splits/" + splitName, machine);
        }

        ArrayList<Process> list_p = h.parallelizeProcesses(arguments);
        h.waitForProcesses(list_p);

        return splitLocations;
    }
    public HashMap<String, Process> runSlaves(HashMap<String, String> splitLocations) {
        ArrayList<List<String>> arguments = new ArrayList<>();

        // start processes in parallel
        ArrayList<String> list_m = new ArrayList<>();
        for(String splitName: splitLocations.keySet()) {
            List<String> l = new ArrayList<>();
            String machineName = splitLocations.get(splitName);
            l.add("ssh");
            l.add("binetruy@" + machineName);
            l.add("java");
            l.add("-jar");
            l.add("/tmp/binetruy/Slave.jar");
            l.add("0");
            l.add(splitName);
            arguments.add(l);
            list_m.add(splitName);
        }

        ArrayList<Process> list_p = h.parallelizeProcesses(arguments);
        HashMap<String, Process> slaveProcessLocation = new HashMap<>();
        for(int i = 0; i < list_m.size(); i++) {
            slaveProcessLocation.put(list_m.get(i), list_p.get(i));
        }

        return slaveProcessLocation;
    }
    public ArrayList<String> readOutput(ArrayList<Process> list_p) {
        ArrayList<String> outputs = new ArrayList<>();
        for(Process p : list_p) {
            String output = h.readOutput(p);
            outputs.add(output);
        }

        return outputs;
    }
    public static void main(String[] args) {
        Master m = new Master();
    }
}
