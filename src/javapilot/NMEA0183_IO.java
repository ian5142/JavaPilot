/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package javapilot;

/**
 *
 * @author Ian
 */
public class NMEA0183_IO {
    RS232Control controller;
    double currentHDG;
    
    public NMEA0183_IO () {
        controller = new RS232Control();
        currentHDG = 0.0;
    }
    
    /**
     * Tries to read a line from the serial port
     *
     * @return The line read from the serial port
     */
    protected String read() {
        byte [] readArray = controller.testRead();
        String readString = new String(readArray);
        return readString;
    }
}
