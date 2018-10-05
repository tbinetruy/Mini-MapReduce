// TP1
public class TP1 {

    public static void main(String[] args) {
        // Prints "Hello, World" to the terminal window.
        System.out.println("Hello, World");
        String content = new String(Files.readAllBytes(Paths.get("words.dat")));
    }
}
