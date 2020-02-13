package org.example;

import java.io.IOException;

public class App  {
    public static void main( String[] args ) throws InterruptedException, IOException {
        RobotArm r = new RobotArm("COM3");

        r.connect();
        if (!r.isOpen()) {
            System.err.println("Cannot open Port");
            return;
        }

        r.sendCommand(r.INITPOSITION);
        Thread.sleep(5000);
        r.switchGripper();
        Thread.sleep(5000);
        r.switchGripper();
        r.close();
    }
}
