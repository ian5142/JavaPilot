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
    /**
     * Constructor for NMEA0183_IO
     * Creates a new RS232Control object
     * Assigns a heading value of 0.0 to currentHDG.
     */
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
//                "$HCHDG,120.3,,,17.5,W"; //Used for testing.
        return readString;
    }
    
    /**
     * Calls read() method above. Parses the heading data from a NMEA0183 string.
     * 
     * @return The heading in decimal degrees. (0.0 to 359.9 degrees)
     */
    protected double readHDG () {
        double heading = 0;
        String strHeading = "";
        String line = read();
        if (line.startsWith("$HCHDG")) {
            String [] parts = line.split(",");
            strHeading = parts[1];
            heading = Double.parseDouble(strHeading);
        }
        return heading;
    }
}
