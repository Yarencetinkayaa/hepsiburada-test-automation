package core;

public class Delay {

    public static void sleep(long ms) {
        try { Thread.sleep(ms); } catch (InterruptedException ignored) {}
    }
    public static void ms(long millis) {
        try { Thread.sleep(millis); } catch (InterruptedException ignored) {}
    }

    public static void xs() { sleep(200); }
    public static void s()  { sleep(400); }
    public static void m()  { sleep(800); }
    public static void l()  { sleep(1500); }
}
