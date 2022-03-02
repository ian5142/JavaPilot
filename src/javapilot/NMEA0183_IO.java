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
    protected String read () {
        byte [] readArray = controller.testRead();
        String readString = new String(readArray);
        return readString;
    }
    
    protected double readHDG () {
        double heading = 0;
        String line = read();
        if (line.startsWith("$HCHDG")) {
            
        }
        return heading;
    }
}
