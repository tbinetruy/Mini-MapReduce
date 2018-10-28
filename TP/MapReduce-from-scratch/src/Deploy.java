import java.util.ArrayList;
import java.io.IOException;
import java.lang.InterruptedException;
import java.util.stream.Collectors;
import java.util.Arrays;

public class Deploy {
    Helpers h;
    public Deploy() {
        this.h = new Helpers();

        ArrayList<String> list_m = new ArrayList<>();
        list_m.add("c133-07");
        list_m.add("c133-08");
        list_m.add("c133-09");

        ArrayList<String> list_working_m = h.getReachableMachines(list_m);
        this.deploySlave(list_working_m);
    }
    public void deploySlave(ArrayList<String> list_m) {
        ArrayList<String> arguments = list_m.stream()
            .map(m -> "ssh binetruy@" + m + " mkdir -p /tmp/binetruy; scp Slave.jar binetruy@" + m + ":/tmp/binetruy/")
            .collect(Collectors.toCollection(ArrayList::new));
        ArrayList<Process> list_p = h.parallelizeProcesses(arguments);
        for(int i = 0; i < list_m.size(); i++) {
            Process p = list_p.get(i);
            String s = h.readOutput(p);
            if(s == null)
                System.out.println(list_m.get(i) +  ": Deploy successful");
            else
                System.err.println(s);
        }
    }
    public static void main(String[] args) {
        Deploy m = new Deploy();
    }
}
