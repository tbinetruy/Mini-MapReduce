import java.util.ArrayList;
import java.io.IOException;
import java.lang.InterruptedException;
import java.lang.reflect.Array;
import java.util.stream.Collectors;
import java.util.Arrays;
import java.util.List;

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


        ArrayList<List<String>> arguments = new ArrayList<>();
        for(String m: list_m) {
            String cmd = "ssh binetruy@" + m + " mkdir -p /tmp/binetruy; scp Slave.jar binetruy@" + m + ":/tmp/binetruy/";
            List<String> cmds = new ArrayList<String>();
            cmds.add("bash");
            cmds.add("-c");
            cmds.add(cmd);
            arguments.add(cmds);
        }

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
