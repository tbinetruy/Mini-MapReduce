// jar cmf Slave.mf Slave.jar Slave.class Slave.java

import java.lang.Thread;
import java.lang.InterruptedException;

public class Slave {
    public Slave() {
        try {
            Thread.sleep(10000);
        } catch(InterruptedException e) {
            System.err.println("Thread error.");
        }
        System.out.println(1 + 2);
    }
    public static void main(String[] args) {
        Slave m = new Slave();
    }
}
