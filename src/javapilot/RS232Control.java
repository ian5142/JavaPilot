/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package javapilot;

import jssc.*; // Java Simple Serial Connector, the library that contains the serial methods

/**
 *
 * @author Ian
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
        portName = "/dev/ttyUSB0";
        serialPort = new SerialPort(portName);
        message = new StringBuilder();
        receivingMessage = false;
        reader = new SerialPortReader();
        readLine = "";
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
