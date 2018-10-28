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
        HashMap<String, String> mapLocations = this.getMapLocations(splitLocations);

        System.out.println("Phase de MAP terminée.");

        HashMap<String, HashMap<String, ArrayList<String>>> machineWordsMap =
            this.prepareShuffle(mapLocations, keyUMMap, list_m);

        System.out.println("Phase de préparation du SHUFFLE terminée.");

        this.requestSlaveShuffle(machineWordsMap);

        System.out.println("Phase du SHUFFLE terminée.");

        this.requestSlaveReduce(machineWordsMap);

        System.out.println("Phase du Reduce terminée.");

    }
    void requestSlaveReduce(HashMap<String, HashMap<String, ArrayList<String>>> machineWordsMap) {
        ArrayList<List<String>> cmds = new ArrayList<>();
        for(String machine: machineWordsMap.keySet()) {
            int counter = 0;
            for(String word: machineWordsMap.get(machine).keySet()) {
                List<String> cmd = new ArrayList<>();
                cmd.add("ssh");
                cmd.add("binetruy@" + machine);
                cmd.add("java");
                cmd.add("-jar");
                cmd.add("/tmp/binetruy/Slave.jar");
                cmd.add("2");
                cmd.add(word);
                cmd.add("/tmp/binetruy/maps/SM" + Integer.toString(counter) + ".txt");
                cmd.add("/tmp/binetruy/reduces/RM" + Integer.toString(counter) + ".txt");
                System.out.println(cmd);

                cmds.add(cmd);

                counter++;
            }
        }

        ArrayList<Process> list_p = h.parallelizeProcesses(cmds);
        h.waitForProcesses(list_p);
        for(Process p: list_p) {
            h.readOutput(p);
        }
    }
    void requestSlaveShuffle(HashMap<String, HashMap<String, ArrayList<String>>> machineWordsMap) {
        ArrayList<List<String>> cmds = new ArrayList<>();
        for(String machine: machineWordsMap.keySet()) {
            int counter = 0;
            for(String word: machineWordsMap.get(machine).keySet()) {
                List<String> cmd = new ArrayList<>();
                cmd.add("ssh");
                cmd.add("binetruy@" + machine);
                cmd.add("java");
                cmd.add("-jar");
                cmd.add("/tmp/binetruy/Slave.jar");
                cmd.add("1");
                cmd.add(word);
                cmd.add("/tmp/binetruy/maps/SM" + Integer.toString(counter) + ".txt");

                for(String UM: machineWordsMap.get(machine).get(word)) {
                    cmd.add(UM);
                }
                System.out.println(cmd);

                cmds.add(cmd);

                counter++;
            }
        }

        ArrayList<Process> list_p = h.parallelizeProcesses(cmds);
        h.waitForProcesses(list_p);
        for(Process p: list_p) {
            h.readOutput(p);
        }
    }
    HashMap<String, HashMap<String, ArrayList<String>>> prepareShuffle(
        HashMap<String, String> mapLocations,
        HashMap<String, ArrayList<String>> keyUMMap,
        ArrayList<String> list_m
    ) {
        /*
          keyUMMap:
            Car - < /tmp/binetruy/splits/UM1.txt /tmp/binetruy/splits/UM2.txt >
            River - < /tmp/binetruy/splits/UM0.txt /tmp/binetruy/splits/UM1.txt >
            Deer - < /tmp/binetruy/splits/UM0.txt /tmp/binetruy/splits/UM2.txt >
            Beer - < /tmp/binetruy/splits/UM0.txt /tmp/binetruy/splits/UM2.txt >

          mapLocations:
            /tmp/binetruy/maps/UM0.txt - c133-07
            /tmp/binetruy/maps/UM1.txt - c133-08
            /tmp/binetruy/maps/UM2.txt - c133-09

          result:
            Machine 1:
              Car: UM1, UM2
              Beer: UM0, UM2
              ==> UM0, UM1, UM2
            Machine 2:
              River: UM0, UM1
            Machine 3:
              Deer: UM0, UM2
         */
        HashMap<String, HashMap<String, ArrayList<String>>> machineWordsMap = new HashMap<>();
        int counter = 0;
        for(String word: keyUMMap.keySet()) {
            String machineName = list_m.get(counter % list_m.size());
            counter++;
            HashMap<String, ArrayList<String>> wordUMsMap = new HashMap<>();
            ArrayList<String> UMList = keyUMMap.get(word);
            wordUMsMap.put(word, UMList);
            if(machineWordsMap.containsKey(machineName)) {
                machineWordsMap.get(machineName).put(word, UMList);
            } else {
                machineWordsMap.put(machineName, wordUMsMap);
            }
        }

        for(String machine: machineWordsMap.keySet()) {
            System.out.println(machine);
            for(String word: machineWordsMap.get(machine).keySet()) {
                System.out.println(word);
                for(String UM: machineWordsMap.get(machine).get(word)) {
                    System.out.println(UM);
                }
            }
        }

        this.transferUMs(machineWordsMap, mapLocations);

        return machineWordsMap;
    }
    void transferUMs(HashMap<String, HashMap<String, ArrayList<String>>> machineWordsLocation, HashMap<String, String> mapLocations) {
        HashMap<String, ArrayList<String>> machinesToUMsNeeded = new HashMap<>();
        for(String machine: machineWordsLocation.keySet()) {
            ArrayList<String> UMs = new ArrayList<>();
            for(String word: machineWordsLocation.get(machine).keySet()) {
                for(String UM: machineWordsLocation.get(machine).get(word)) {
                    if(!UMs.contains(UM) && !machine.equals(mapLocations.get(UM))) {
                        UMs.add(UM);
                    }
                }
            }
            machinesToUMsNeeded.put(machine, UMs);
            System.out.println(machine);
            System.out.println(UMs);
        }

        ArrayList<List<String>> cmds = new ArrayList<>();
        for(String machineTo: machinesToUMsNeeded.keySet()) {
            for(String UM: machinesToUMsNeeded.get(machineTo)) {
                String machineFrom = mapLocations.get(UM);
                List<String> cmd = new ArrayList<>();
                String pushString = "ssh binetruy@" + machineFrom + " 'scp " + UM + " binetruy@" + machineTo + ":/tmp/binetruy/maps/'";
                System.out.println(pushString);
                cmd.add("ssh");
                cmd.add("binetruy@" + machineFrom);
                cmd.add("scp " + UM + " binetruy@" + machineTo + ":/tmp/binetruy/maps/");
                cmds.add(cmd);
            }
        }
        ArrayList<Process> list_p = h.parallelizeProcesses(cmds);
        h.waitForProcesses(list_p);
        for(Process p: list_p) {
            h.readOutput(p);
        }
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
                    UMList.add(UM.replace("/S", "/UM").replace("/splits", "/maps"));
                    keyUMMap.put(word, UMList);
                } else {
                    keyUMMap.get(word).add(UM.replace("/S", "/UM").replace("/splits", "/maps"));
                }
            }
        }

        return keyUMMap;
    }
    HashMap<String, String> getMapLocations(HashMap<String, String> splitLocations) {
        HashMap<String, String> mapLocations = new HashMap<>();
        for(String splitname: splitLocations.keySet()) {
            String absolutePath = "/tmp/binetruy/splits/";
            String mapname = "/tmp/binetruy/maps/UM" + splitname.substring(absolutePath.length() + 1);
            mapLocations.put(mapname, splitLocations.get(splitname));
            System.out.println(mapname + " - " + splitLocations.get(splitname));
        }
        return mapLocations;
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
