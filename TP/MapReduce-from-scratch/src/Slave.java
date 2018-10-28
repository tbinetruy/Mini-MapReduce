import java.lang.Thread;
import java.lang.InterruptedException;
import java.nio.file.Files;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.io.BufferedWriter;
import java.io.FileWriter;

class Line {
    public String word;
    public Integer count;
    public Line(String word, Integer count) {
        this.word = word;
        this.count = count;
    }
};

public class Slave {
    Helpers h;
    public Slave() {
        this.h = new Helpers();
    }
    public void map(String filename) {
        String fileContent = this.getFileContent(filename);
        String[] content = fileContent
            .replace("\n", " ")
            .split(" ");

        ArrayList<Line> mapping = new ArrayList<>();
        ArrayList<String> keys = new ArrayList<>();
        for(String key: content) {
            Line l = new Line(key, new Integer(1));
            mapping.add(l);
            if(!keys.contains(key)) {
                keys.add(key);
            }
        }

        boolean success = this.createDirectory("/tmp/binetruy/maps");
        String absoluteSplitPath = "/tmp/binetruy/splits/";
        if(success) {
            String filePath = "/tmp/binetruy/maps/UM" + filename.substring(absoluteSplitPath.length() + 1);
            this.writeFile(mapping, filePath);
            for(String key: keys) {
                System.out.println(key);
            }
        }
    }
    boolean writeFile(ArrayList<Line> content, String filePath) {
        BufferedWriter out = null;

        try {
            FileWriter fstream = new FileWriter(filePath);
            out = new BufferedWriter(fstream);
            for(Line l: content) {
                out.write(l.word + " " + Integer.toString(l.count) + "\n");
            }
            out.close();
        }
        catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
        }

        return true;
    }
    public String getFileContent(String filename) {
        String fileContent = new String();

        try {
            fileContent = new String(Files.readAllBytes(Paths.get(filename)));
        } catch (IOException e) {
            System.out.println("An error has occurred while reading the file.");
            System.out.println(e);
        }

        return fileContent;
    }
    public boolean createDirectory(String dirname) {
        ArrayList<List<String>> args = new ArrayList<>();
        ArrayList<String> arg1 = new ArrayList<>();
        arg1.add("mkdir");
        arg1.add("-p");
        arg1.add(dirname);

        args.add(arg1);

        ArrayList<Process> list_p = h.parallelizeProcesses(args);
        ArrayList<Process> successful_p = h.waitForProcesses(list_p);

        return successful_p.get(0) == list_p.get(0);
    }
    public static void main(String[] args) {
        Slave slave = new Slave();
        int mode = Integer.parseInt(args[0]);
        if(mode == 0) {
            slave.map(args[1]);
        }
    }
}
