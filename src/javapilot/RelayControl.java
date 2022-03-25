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

import com.pi4j.Pi4J;
import com.pi4j.context.Context;
import com.pi4j.io.gpio.digital.DigitalOutput;
import com.pi4j.io.gpio.digital.DigitalState;
import com.pi4j.util.Console;
import java.util.concurrent.TimeUnit;

/**
 * Contains methods to control the relays via GPIO on the Raspberry PI 4.
 * @author Ian
 */
public class RelayControl {
    public static final int DIGITAL_OUTPUT_PIN = 4;
    public static final int DIGITAL_OUTPUT_PIN2 = 14;
    Context pi4j = Pi4J.newAutoContext();
    DigitalOutput leftOutput;
    DigitalOutput rightOutput;
    Console console;
    
    /**
     * The Relay Control Constructor
     */
    public RelayControl () {
        startPI4J ();
    }
    
    /**
     * Turns the Relay on.
     * @param turnDirection The turn direction, 0 for left, 1 for right. Default is 0.
     * @return 
     */
    protected int RelayON (int turnDirection) {
        int error = 0;
        if (turnDirection != 0 && turnDirection != 1) { // to handle if turn direction is something other than 0 or 1
            turnDirection = 0; // Turn on Left Relay
        }
        
        if (turnDirection == 0) { // Turn on Left Relay
            leftOutput.low();
        }
        else if (turnDirection == 1) { // Turn on Right Relay
            rightOutput.low();
        }
        
        System.out.print("CURRENT DIGITAL OUTPUT [" + leftOutput + "] STATE IS [");
        System.out.println(leftOutput.state() + "]");
        
        // pulse to HIGH state for 3 seconds
        System.out.println("PULSING OUTPUT STATE TO HIGH FOR 3 SECONDS");
        leftOutput.pulse(3, TimeUnit.SECONDS, DigitalState.HIGH);
        System.out.println("PULSING OUTPUT STATE COMPLETE");

        return error;
    }
    
    protected int RelayOFF (int turnDirection) {
        int error = 0;
        if (turnDirection != 0 && turnDirection != 1) { // to handle if turn direction is something other than 0 or 1
            turnDirection = 0;
        }
        
        if (turnDirection == 0) { // Turn on Left Relay
            leftOutput.high();
        }
        else if (turnDirection == 1) { // Turn on Right Relay
            rightOutput.high();
        }
        return error;
    }
    
    protected void startPI4J () {
        console = new Console();
        // create Pi4J console wrapper/helper
        // (This is a utility class to abstract some of the boilerplate stdin/stdout code)
        
        // print program title/header
        console.title("<-- The Pi4J Project -->", "Basic Digital Output Example");

        // allow for user to exit program using CTRL-C
        console.promptForExit();
        
        // setup a digital output listener to listen for any state changes on the digital output
        leftOutput.addListener(System.out::println);
        rightOutput.addListener(System.out::println);
        
        // create a digital output instance using the default digital output provider
        leftOutput = pi4j.dout().create(DIGITAL_OUTPUT_PIN);
        leftOutput.config().shutdownState(DigitalState.HIGH);
        rightOutput = pi4j.dout().create(DIGITAL_OUTPUT_PIN2);
        rightOutput.config().shutdownState(DigitalState.HIGH);
    }
    
    protected void shutdownPI4J () {
        // shutdown Pi4J
        console.println("ATTEMPTING TO SHUTDOWN/TERMINATE THIS PROGRAM");
        pi4j.shutdown();
    }
}

