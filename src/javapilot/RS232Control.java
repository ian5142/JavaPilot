/**
 * JavaPilot Project
 * Copyright (C) 2022 Ian Van Schaick
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or 
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package javapilot;

import java.util.Scanner;
import jssc.*; // Java Simple Serial Connector, the library that contains the serial methods

/**
 * Contains low-level methods to receive incoming serial data. All of it is passed to NMEA0183_IO methods.
 * @author Ian Van Schaick
 */
public class RS232Control {
    static SerialPort serialPort;
    String portName;
    static long portOpen;
    StringBuilder message;
    Boolean receivingMessage;
    SerialPortReader reader;
    String readLine;
    
    public RS232Control () {
        portName = "/dev/ttyACM0";
        serialPort = new SerialPort(portName);
        message = new StringBuilder();
        receivingMessage = false;
        reader = new SerialPortReader();
        readLine = "";
    }
    
    /**
     * Finds the serial port to be used, in Windows type COM1, for example In
     * Linux, type /dev/pts/3 for example. The custom USB-RS232 device, using a
     * MCP2200, is on /dev/ttyACM0/
     * All serial ports may not be listed.
     *
     * @return The serial port name in String format, used to open and close the
     * port
     */
    private String findPort() {
        System.out.println("List of COM ports:");
        String[] portNames = SerialPortList.getPortNames();
        for (String portName1 : portNames) {
            System.out.println(portName1);
        }
        
        System.out.println("What COM port are you using?");
        System.out.println("Please type it in how it appears above.");
        Scanner sc = new Scanner(System.in);
        String port = "";
        if (sc.hasNext()) {
            port = sc.next();
        } else {

        }
        return port;
    }
    
    /**
     * Checks if the serial port is connected
     * @return Returns true if any of the serial ports found using getPortNames() 
     * matches the portName global variable (what ever the user types in when 
     * findPort() is called).
     */
    protected boolean serialConnected () {
        boolean connected = false;
        String[] portNames = SerialPortList.getPortNames();
        for (String portName1 : portNames) {
            if (portName1.equals(portName) ) {
                connected = true;
//                System.out.println("Connected successfully to serial port: " + portName);
            }
            else {
//                System.out.println("Can not connect to serial port: " + portName);
            }
        }
        return connected;
    }

    /**
     * Opens a COM port at the specified settings (9600 8N1)
     * Can throw an error opening the port
     */
    private void open() {
        try {
            serialPort.openPort();
            serialPort.setParams(SerialPort.BAUDRATE_9600,
                    SerialPort.DATABITS_8,
                    SerialPort.STOPBITS_1,
                    SerialPort.PARITY_NONE);

            serialPort.setFlowControlMode(SerialPort.FLOWCONTROL_NONE);
            int mask = SerialPort.MASK_RXCHAR;
            serialPort.setEventsMask(mask);
            serialPort.addEventListener(reader);
            serialPort.setRTS(false);
            serialPort.setDTR(false);
        } catch (SerialPortException ex) {
            System.out.println("Error in opening COM-port: " + ex);
        }
    }

    /**
     * Closes the serial port, can throw a SerialPortException error.
     *
     * @return
     */
    private boolean close() {
        boolean success = false;
        try {
            serialPort.closePort();
            success = true;
        } catch (SerialPortException ex) {
            System.out.println("Error in closing COM-port: " + ex);
//            Logger.getLogger(RS232Control.class.getName()).log(Level.ERROR, null, ex);
        }
        return success;
    }
    
    /**
     * Opens the serial port. Tries to read a string from the serial port.
     * Closes the serial port.
     *
     * @return Returns the byte array read from the serial port.
     */
    protected byte [] testRead() {
        byte [] readArray = null;
        try {
            open();
            readArray = serialPort.readBytes(30);
            close();
        } 
        
        catch (SerialPortException ex) {
            System.out.println("Error in receiving string from COM-port: " + ex);
//            Logger.getLogger(RS232Control.class.getName()).log(Level.SEVERE, null, ex);
        }
        return readArray;
    }
    
    /**
     * In this class must implement the method serialEvent, through it we learn
     * about events that happened to our port. But we will not report on all
     * events but only those that we put in the mask. In this case the arrival
     * of the data and change the status lines CTS and DSR
     */
    private class SerialPortReader implements SerialPortEventListener {

        /**
         * Reads the data bit by bit from the serial port Can throw a
         * SerialPortException error
         *
         * @param event
         */
        @Override
        public void serialEvent(SerialPortEvent event) {
            if (event.isRXCHAR() && event.getEventValue() == 10) {
                try {
                    String line = serialPort.readString(event.getEventValue());
//                    acknowledgeStr + acknowledgeStr + 
                    System.out.println("serialEvent: " + line);
                    if (line.contains((char) 0x6 + "")) {
                        System.out.println("Acknowledged");
                        
                    }
                    else {
                        
                    }
//                    System.out.println("Received response: " + readLine);
               
                } catch (SerialPortException ex) {
                    System.out.println("Error in receiving string from COM-port: " + ex);
                }
            }
        }
    }

    /**
     * Prints out the message read from the serial port
     *
     * @param message
     */
    protected void processMessage(String message) {
//        System.out.println(message);
    }
}
