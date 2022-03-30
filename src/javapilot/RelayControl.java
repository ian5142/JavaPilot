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

import com.diozero.api.DigitalOutputDevice;
import java.util.ArrayList;

/**
 * Contains methods to control the relays via GPIO on the Raspberry PI 4.
 * @author Ian Van Schaick
 */
public class RelayControl {
    public static final int DIGITAL_OUTPUT_PIN = 2; //Left Relay control pin
    public static final int DIGITAL_OUTPUT_PIN2 = 3; //Right Relay control pin
    DigitalOutputDevice leftOutput; //DigitalOutputDevice object used to control the left output pin
    DigitalOutputDevice rightOutput; //DigitalOutputDevice object used to control the right output pin
    /**
     * The Relay Control Constructor
     */
    public RelayControl () {
        leftOutput = new DigitalOutputDevice(DIGITAL_OUTPUT_PIN, false, true);
        rightOutput = new DigitalOutputDevice(DIGITAL_OUTPUT_PIN2, false, true);
    }
    
    /**
     * Turns the Relay on.
     * @param turnDirection The turn direction, 0 for left, 1 for right. Default is 0.
     * @return 
     */
    protected boolean RelayON (int turnDirection) {
        boolean relayON = false;
        if (turnDirection != 0 && turnDirection != 1) { // to handle if turn direction is something other than 0 or 1
            turnDirection = 0; // Turn on Left Relay
        }
        
        if (turnDirection == 0) { // Turn on Left Relay
            leftOutput.on();
            relayON = leftOutput.isOn();
        }
        else if (turnDirection == 1) { // Turn on Right Relay
            rightOutput.on();
            relayON = rightOutput.isOn();
        }

        return relayON;
    }
    
    /**
     * Turns the relay off.
     * @param turnDirection The turn direction, 0 for left, 1 for right. Default is 0.
     * @return True if relay is still ON, false if relay is successfully turned off.
     */
    protected boolean RelayOFF (int turnDirection) {
        boolean relayON = true;
        if (turnDirection != 0 && turnDirection != 1) { // to handle if turn direction is something other than 0 or 1
            turnDirection = 0;
        }
        
        if (turnDirection == 0) { // Turn on Left Relay
            leftOutput.off();
            relayON = leftOutput.isOn();
        }
        else if (turnDirection == 1) { // Turn on Right Relay
            rightOutput.off();
            relayON = rightOutput.isOn();
        }
        return relayON;
    }
    
    /**
     * 
     * @param list
     * @param direction
     * @return 
     */
    protected ArrayList calculateDirection (ArrayList<Integer> list, boolean direction) {
        int desired = list.get(0);
        int current = list.get(1);
        int countCurrent = list.get(2);
        int count = list.get(3);
        if (direction) { //counts clockwise if true
            if (countCurrent == 360) { //If current counting position is 360, start counting up from 0
                countCurrent = 0;
            }
//            System.out.println("countCurrent: " + countCurrent);
            if ( ( (desired < countCurrent) || (desired > countCurrent) ) ) { //&& ((desired + 2 != countCurrent) || (desired - 2 != countCurrent) )
//                System.out.println("Counting up");
                count++;
                if (count < 360) {
//                    System.out.println("Count: " + count);
                    countCurrent++;
                    list.set(2, countCurrent);
                    list.set(3, count);
                    list = calculateDirection(list, direction);
                }
            }
            else if (countCurrent == desired) {
//                System.out.println("countCurrent = Desired.");
            }
        }
        else if (!direction) { //counts counter-clockwise if false
//            System.out.println("countCurrent2: " + countCurrent);
//            System.out.println("desired2: " + desired);
            if ( ( (desired < countCurrent) || (desired > countCurrent)) ) { //&& ((desired + 2 != countCurrent) || (desired - 2 != countCurrent) )
//                System.out.println("Counting down");
                count++;
                if (countCurrent == 0) {//If current counting position is 0, start counting down from 360
                    countCurrent = 360;
                }
                if (count < 360) {
//                    System.out.println("Count: " + count);
                    countCurrent--;
                    list.set(2, countCurrent);
                    list.set(3, count);
                    list = calculateDirection(list, direction);
                }
            }
            else if (countCurrent == desired) {
//                System.out.println("countCurrent = Desired.2");
            }
        }
        return list;
    }
}

