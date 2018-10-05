import java.nio.file.Files;
import java.nio.file.Paths;
import java.io.IOException;
import java.util.HashMap;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Collectors;
import java.util.Collections;
import java.util.Comparator;

class Line {
    public String word;
    public Integer count;
    public Line(String word, Integer count) {
        this.word = word;
        this.count = count;
    }
};

public class Entry {
    String fileContent;
    ArrayList<Line> wc;
    public Entry(String filename) {
        this.setFileContent(filename);

        String[] content = this.fileContent
            .replace("\n", " ")
            .split(" ");

        HashMap<String, Integer> map = new HashMap<>();

        for(String s : content) {
            if(map.containsKey(s)) {
                map.put(s, map.get(s) + 1);
            } else {
                map.put(s, 1);
            }
        }

        // System.out.println(Arrays.asList(map));

        this.wc = this.hashMapToList(map);
        // this.printWc();
        this.deepSortWc();
        this.printWc();
    }
    public void printWc() {
        for(Line l : this.wc) {
            System.out.println(l.word.concat(" ").concat(Integer.toString(l.count)));
        }
    }
    public void sortWc() {
        Collections.sort(this.wc, new Comparator<Line>(){
                public int compare(Line a, Line b) {
                    return (a.count > b.count) ? -1 : 1;
                }
            });
    }
    public void deepSortWc() {
        Collections.sort(this.wc, new Comparator<Line>(){
                public int compare(Line a, Line b) {
                    if (a.count > b.count)
                        return -1;
                    else if (a.count == b.count) {
                        return a.word.compareTo(b.word);
                    }
                    else
                        return 1;
                }
            });
    }
    public ArrayList<Line> hashMapToList(HashMap<String, Integer> map) {
        ArrayList<Line> result = new ArrayList<>();
        for(HashMap.Entry<String, Integer> e : map.entrySet()) {
            String key = e.getKey();
            Integer value = e.getValue();
            result.add(new Line(key, value));
        }


        return result;
    }
    public void setFileContent(String filename) {
        try {
            this.fileContent = new String(Files.readAllBytes(Paths.get(filename)));
        } catch (IOException e) {
            System.out.println("An error has occurred while reading the file.");
        }
    }
    public static void main(String[] args) {
        Entry e = new Entry("words.dat");
    }
}
