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

import com.google.common.util.concurrent.SimpleTimeLimiter;
import com.google.common.util.concurrent.TimeLimiter;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

/**
 * The main class, contains the main method which creates objects for NMEA0183_IO and RelayControl.
 * @author Ian Van Schaick
 */
public class JavaPilot {
    
    final static String Digits = "(\\p{Digit}+)";
    final static String HexDigits = "(\\p{XDigit}+)";
// an exponent is 'e' or 'E' followed by an optionally 
// signed decimal integer.
    final static String Exp = "[eE][+-]?" + Digits;
    final static String fpRegex
            = ("[\\x00-\\x20]*"
            + // Optional leading "whitespace"
            "[+-]?("
            + // Optional sign character
            "NaN|"
            + // "NaN" string
            "Infinity|"
            + // "Infinity" string
            // Digits ._opt Digits_opt ExponentPart_opt FloatTypeSuffix_opt
            "(((" + Digits + "(\\.)?(" + Digits + "?)(" + Exp + ")?)|"
            + // . Digits ExponentPart_opt FloatTypeSuffix_opt
            "(\\.(" + Digits + ")(" + Exp + ")?)|"
            + // Hexadecimal strings
            "(("
            + // 0[xX] HexDigits ._opt BinaryExponent FloatTypeSuffix_opt
            "(0[xX]" + HexDigits + "(\\.)?)|"
            + // 0[xX] HexDigits_opt . HexDigits BinaryExponent FloatTypeSuffix_opt
            "(0[xX]" + HexDigits + "?(\\.)" + HexDigits + ")"
            + ")[pP][+-]?" + Digits + "))"
            + "[fFdD]?))"
            + "[\\x00-\\x20]*");// Optional trailing "whitespace"
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        RelayControl relay = new RelayControl ();
        NMEA0183_IO serialReader = new NMEA0183_IO();
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        double oldHeading;
        ExecutorService es = Executors.newSingleThreadExecutor();
        TimeLimiter timeLimiter = SimpleTimeLimiter.create(es);
        String headingStr = "";
        //Get the Desired heading from the command line. Initial = 0
        double desiredHeading = 0;
        double currentHeading = 0;
        byte count = 0;
        
        while (true) {
            count++;
            //Get the Current heading from the Arduino
            currentHeading = serialReader.readHDG();
            System.out.println("Current Heading: " + currentHeading);
            System.out.println("Desired Heading: " + desiredHeading);

            ArrayList<Integer> CW_list = new ArrayList<Integer>(4); //Create an ArrayList to hold values for clockwise counting
            CW_list.add((int) desiredHeading); //desired heading
            CW_list.add((int) currentHeading); //current heading
            CW_list.add((int) currentHeading); //current count position 
            CW_list.add(0); //number of degrees counted
            CW_list = relay.calculateDirection(CW_list, true);
//                System.out.println("Finished counting up");
            // Do the same thing but counter clockwise.
            ArrayList<Integer> CCW_list = new ArrayList<Integer>(4);
            CCW_list.add((int) desiredHeading); //desired heading
            CCW_list.add((int) currentHeading); //current heading
            CCW_list.add((int) currentHeading); //current count position 
            CCW_list.add(0); //number of degrees counted
            CCW_list = relay.calculateDirection(CCW_list, false);
//            System.out.println("Finished counting down");

            int CW_count = CW_list.get(3);
            int CCW_count = CCW_list.get(3);
            
            int desiredHeadingP3 = (int) desiredHeading + 3;
            int desiredHeadingS3 = (int) desiredHeading - 3;
            
            int currentHeadingP3 = (int) currentHeading + 3;
            int currentHeadingS3 = (int) currentHeading - 3;
            System.out.println("CW-List count: " + CW_count);
            System.out.println("CCW-List count: " + CCW_count);
            if ( (desiredHeading == currentHeading) || (desiredHeading == currentHeading)// Turns both relays off.
                    || ( (desiredHeadingP3 <= currentHeading) && (desiredHeadingS3 >= currentHeading) )
                    || ( (desiredHeading <= currentHeadingP3) && (desiredHeading >= currentHeadingS3) ) ) {
                    //The +/-3 allow for a hysteresis of 3 degrees
                    //error either side. So that the relays don't chatter too much.
                boolean RelayOFF = relay.RelayOFF(0);
                boolean RelayOFF1 = relay.RelayOFF(1);
            }
            else if (CCW_count < CW_count) { 
                boolean RelayON = relay.RelayON(0);
                boolean RelayOFF1 = relay.RelayOFF(1);
                System.out.println("Turning Left");
            }
            else if (CCW_count > CW_count) { 
                boolean RelayON1 = relay.RelayON(1); 
                boolean RelayOFF = relay.RelayOFF(0);
                System.out.println("Turning Right");
            }
            for (int i = 0 ; i <= 17 ; i++) {
                System.out.println(); //Add 17 blank lines so Waiting line ends up at bottom of window
            }
            if (count == 10) {
                oldHeading = desiredHeading;
                System.out.print("Waiting for new desired heading: ");
                double newHeading = -1;
                try { 
                    headingStr = timeLimiter.callWithTimeout(reader::readLine, 5, TimeUnit.SECONDS);
                } catch (TimeoutException ex) {
                    //Do nothing if it times out.
                } catch (InterruptedException ex) {
                    //Do nothing if it encounters an interupt exception.
                } catch (ExecutionException ex) {
                    //Do nothing if it encounters a execution exception.
                }
                if (Pattern.matches(fpRegex, headingStr)) {
                    newHeading = Double.parseDouble(headingStr);
                    if (newHeading >= 360 || newHeading < 0) {
                        desiredHeading = oldHeading;
                        System.out.println("Not a valid heading.");
                    }
                    else if (newHeading != -1) {
                        desiredHeading = newHeading;
                        System.out.println();
                    }
                    else {
                        System.out.println("Timed out");
                        desiredHeading = oldHeading;
                    }
                }
                count = 0;
            }
        } //End of while (true) loop
        
    } // End of main method
} // End of Class
