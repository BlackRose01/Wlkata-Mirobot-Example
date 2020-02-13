package org.example;

import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortDataListener;
import com.fazecast.jSerialComm.SerialPortEvent;

import java.io.IOException;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Locale;
import java.util.Objects;

public class RobotArm implements Serializable {
    public final String HOMING = "$h\r\n";
    public final String SINGLEMOVING = "$HH\r\n";
    public final String RESET = "!\r\n";
    public final String INITPOSITION = "G90 G01 X105.00 Y25.00 Z-55.00 A170.00 B30.00 C0.00 F2000.00\r\n";
    public final String ZEROPOSITION = "M21G90G01X0Y0Z0A0B0C0\r\n";

    private final String[] GRIPPER = { "M3S1000M4E65\r\n", "M3S0M4E40\r\n" };

    private String port = null;
    private double axisX = 0.0;
    private double axisY = 0.0;
    private double axisZ = 0.0;
    private double axisA = 0.0;
    private double axisB = 0.0;
    private double axisC = 0.0;
    private double speed = 2000.0;
    private boolean gripperOpen = false;

    private SerialPort p = null;
    private String commands = "G90 G01";

    /**
     * Constructor: Set port
     * @param port
     * @throws InterruptedException
     * @throws IOException
     */
    public RobotArm(String port) {
        this.port = port;
    }

    public RobotArm() {
        this(SerialPort.getCommPorts()[0].getPortDescription());
    }

    /**
     * Connect with port, set up Parameters and send HOMING command
     * @return
     * @throws InterruptedException
     */
    public boolean connect() throws InterruptedException {
        if (p != null && p.isOpen())
            this.close();

        p = SerialPort.getCommPort(this.port);

        if (!p.openPort())
            return false;

        p.setComPortParameters(115200, 8, SerialPort.ONE_STOP_BIT, SerialPort.NO_PARITY);
        p.setComPortTimeouts(SerialPort.TIMEOUT_READ_BLOCKING | SerialPort.TIMEOUT_WRITE_BLOCKING, 3000, 3000);
        p.setDTR();
        p.setRTS();

        p.addDataListener(new SerialPortDataListener() {
            @Override
            public int getListeningEvents() {
                return SerialPort.LISTENING_EVENT_DATA_WRITTEN;
            }

            @Override
            public void serialEvent(SerialPortEvent serialPortEvent) {
                if (serialPortEvent.getEventType() == SerialPort.LISTENING_EVENT_DATA_WRITTEN)
                    System.out.println("All bytes were successfully transmitted!");
            }
        });

        if (!p.isOpen())
            return false;

        while (p.bytesAvailable() == 0)
            Thread.sleep(20);

        this.sendCommand(this.HOMING);

        return true;
    }

    /**
     * Return the Serial Port status
     * @return
     */
    public boolean isOpen() {
        return p.isOpen();
    }

    /**
     * Close port
     * @return
     */
    public boolean close() {
        return this.p.closePort();
    }

    /**
     * Send the command to the roboter arm
     * @param command
     */
    public void sendCommand(String command) throws InterruptedException {
        p.writeBytes(command.getBytes(), command.getBytes().length);

        while (p.bytesAvailable() == 0)
            Thread.sleep(20);
    }

    /**
     * Open or Close the Gripper
     */
    public void switchGripper() throws InterruptedException {
        if (this.gripperOpen)
            this.sendCommand(this.GRIPPER[0]);
        else
            this.sendCommand(this.GRIPPER[1]);

        this.gripperOpen = !this.gripperOpen;
    }

    /**
     * Format current parameters to GCode String
     * @return
     */
    public String formatter() {
        return String
                .format(Locale.ROOT, "%s X%.2f Y%.2f Z%.2f A%.2f B%.2f C%.2f F%.2f", this.commands, this.axisX, this.axisY, this.axisZ, this.axisA, this.axisB, this.axisC, this.speed)
                .concat("\r\n");
    }

    /**
     * Format current parameters with given command to GCode String
     * @param gCodeCommand
     * @return
     */
    public String formatter(String gCodeCommand) {
        return String
                .format(Locale.ROOT, "%s X%.2f Y%.2f Z%.2f A%.2f B%.2f C%.2f F%.2f", gCodeCommand, this.axisX, this.axisY, this.axisZ, this.axisA, this.axisB, this.axisC, this.speed)
                .concat("\r\n");
    }

    /**
     * Format given parameters to GCode String
     * @param gCodeCommand
     * @param axisX
     * @param axisY
     * @param axisZ
     * @param axisA
     * @param axisB
     * @param axisC
     * @return
     */
    public String formatter(String gCodeCommand, double axisX, double axisY, double axisZ, double axisA, double axisB, double axisC) {
        return String
                .format(Locale.ROOT, "%s X%.2f Y%.2f Z%.2f A%.2f B%.2f C%.2f F%.2f", gCodeCommand, axisX, axisY, axisZ, axisA, axisB, axisC, this.speed)
                .concat("\r\n");
    }

    //--------------------------------------------------------------------------------------
    //--------------------------------------------------------------------------------------
    // Getter and Setter

    /**
     * Get the Serial Port name
     * @return
     */
    public String getPort() {
        return port;
    }

    /**
     * Set the Serial Port name
     * @param port
     */
    public void setPort(String port) {
        this.port = port;
    }

    /**
     * Get the commands
     * @return
     */
    public String getCommands() {
        return commands;
    }

    /**
     * Set the commands
     * @param commands
     */
    public void setCommands(String commands) {
        this.commands = commands;
    }

    /**
     * Get Position of axis X
     * @return
     */
    public double getAxisX() {
        return axisX;
    }

    /**
     * Set Position of axis X
     * @param axisX
     */
    public void setAxisX(double axisX) {
        this.axisX = axisX;
    }

    /**
     * Get Position of axis Y
     * @return
     */
    public double getAxisY() {
        return axisY;
    }

    /**
     * Set Position of axis Y
     * @param axisY
     */
    public void setAxisY(double axisY) {
        this.axisY = axisY;
    }

    /**
     * Get Position of axis Z
     * @return
     */
    public double getAxisZ() {
        return axisZ;
    }

    /**
     * Set Position of axis Z
     * @param axisZ
     */
    public void setAxisZ(double axisZ) {
        this.axisZ = axisZ;
    }

    /**
     * Get Orientation for axis A
     * @return
     */
    public double getAxisA() {
        return axisA;
    }

    /**
     * Set Orientation for axis A
     * @param axisA
     */
    public void setAxisA(double axisA) {
        this.axisA = axisA;
    }

    /**
     * Get Orientation for axis B
     * @return
     */
    public double getAxisB() {
        return axisB;
    }

    /**
     * Set Orientation for axis B
     * @param axisB
     */
    public void setAxisB(double axisB) {
        this.axisB = axisB;
    }

    /**
     * Get Orientation for axis C
     * @return
     */
    public double getAxisC() {
        return axisC;
    }

    /**
     * Set Orientation for axis C
     * @param axisC
     */
    public void setAxisC(double axisC) {
        this.axisC = axisC;
    }

    /**
     * Get movement speed
     * @return
     */
    public double getSpeed() {
        return speed;
    }

    /**
     * Set movement speed
     * @param speed
     */
    public void setSpeed(double speed) {
        this.speed = speed;
    }

    /**
     * Returns Gripper Status
     * @return
     */
    public boolean isGripperOpen() {
        return gripperOpen;
    }

    //--------------------------------------------------------------------------------------
    //--------------------------------------------------------------------------------------
    // Stringify

    @Override
    public String toString() {
        return "RobotArm{" +
                "HOMING='" + HOMING + '\'' +
                ", SINGLEMOVING='" + SINGLEMOVING + '\'' +
                ", RESET='" + RESET + '\'' +
                ", INITPOSITION='" + INITPOSITION + '\'' +
                ", ZEROPOSITION='" + ZEROPOSITION + '\'' +
                ", GRIPPER=" + Arrays.toString(GRIPPER) +
                ", axisX=" + axisX +
                ", axisY=" + axisY +
                ", axisZ=" + axisZ +
                ", axisA=" + axisA +
                ", axisB=" + axisB +
                ", axisC=" + axisC +
                ", speed=" + speed +
                ", gripperOpen=" + gripperOpen +
                ", p=" + p +
                ", commands='" + commands + '\'' +
                '}';
    }

    //--------------------------------------------------------------------------------------
    //--------------------------------------------------------------------------------------
    // Hashing and Equal-Function

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RobotArm robotArm = (RobotArm) o;
        return Double.compare(robotArm.getAxisX(), getAxisX()) == 0 &&
                Double.compare(robotArm.getAxisY(), getAxisY()) == 0 &&
                Double.compare(robotArm.getAxisZ(), getAxisZ()) == 0 &&
                Double.compare(robotArm.getAxisA(), getAxisA()) == 0 &&
                Double.compare(robotArm.getAxisB(), getAxisB()) == 0 &&
                Double.compare(robotArm.getAxisC(), getAxisC()) == 0 &&
                Double.compare(robotArm.getSpeed(), getSpeed()) == 0 &&
                isGripperOpen() == robotArm.isGripperOpen() &&
                Objects.equals(HOMING, robotArm.HOMING) &&
                Objects.equals(SINGLEMOVING, robotArm.SINGLEMOVING) &&
                Objects.equals(RESET, robotArm.RESET) &&
                Objects.equals(INITPOSITION, robotArm.INITPOSITION) &&
                Objects.equals(ZEROPOSITION, robotArm.ZEROPOSITION) &&
                Arrays.equals(GRIPPER, robotArm.GRIPPER) &&
                Objects.equals(p, robotArm.p) &&
                Objects.equals(commands, robotArm.commands);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(HOMING, SINGLEMOVING, RESET, INITPOSITION, ZEROPOSITION, getAxisX(), getAxisY(), getAxisZ(), getAxisA(), getAxisB(), getAxisC(), getSpeed(), isGripperOpen(), p, commands);
        result = 31 * result + Arrays.hashCode(GRIPPER);
        return result;
    }
}
