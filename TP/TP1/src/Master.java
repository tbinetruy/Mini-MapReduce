
public class Master {
    public Master() {
        ArrayList<String> liste_m = new ArrayList<>();
        ArrayList<Process> liste_p = new ArrayList<>();
        list_m.add("c133-06");
        list_m.add("c133-07");
        list_m.add("c133-08");
        list_m.add("c133-09");

        for(int i = 0; i < 4; i++) {
            ProcessBuilder pb = new ProcessBuilder("ssh",
                                                   "binetruy@" + liste_m.get(i),
                                                   "java",
                                                   "-jar",
                                                   "slave.jar");
            Process p = pb.start();
            liste_p.add(p);
        }

        for(Process p : liste_p) {
            p.waitFor();
        }

        for(Process p : list_p) {
            this.readOutput(p);
        }
    }
    public String readOutput(Process p) {
        InputStream is = p.getInputStream();
        InputStream is2 = p.getErrorStream();
        BufferInputStream bis = new BufferInputStream(is);
        InputStreamReader isr = new InputStreamReader(is);
        BufferedReader br = new BufferedReader(isr);
        String line = br.readline();
        return line;
    }
    public static void main(String[] args) {
        Master m = new Master();
    }
}
