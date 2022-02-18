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
public class NMEA_Data_IO {
    static SerialPort serialPort;
    String portName;
    static long portOpen;
    StringBuilder message;
    Boolean receivingMessage;
    SerialPortReader reader;
    String readLine;
    
    public NMEA_Data_IO () {
        portName = "/dev/ttyUSB0";
        serialPort = new SerialPort(portName);
        message = new StringBuilder();
        receivingMessage = false;
        reader = new SerialPortReader();
        readLine = "";
    }
}
