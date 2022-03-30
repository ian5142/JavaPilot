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
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The main class, contains the main method which creates objects for NMEA0183_IO and RelayControl.
 * @author Ian Van Schaick
 */
public class JavaPilot {
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        Timer timer = new Timer();
        DesiredHeading dh = new DesiredHeading();
        timer.schedule(dh, 0, 2000);
        RelayControl relay = new RelayControl ();
        NMEA0183_IO serialReader = new NMEA0183_IO();

        while (true) {
            //Get the Current heading from the Arduino
            double currentHeading = serialReader.readHDG();
            System.out.println("Current Heading: " + currentHeading);
            //Get the Desired heading from the command line. Initial = 0
            double desiredHeading = dh.getDesiredHeading();
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
            
            System.out.println("CW-List count: " + CW_count);
            System.out.println("CCW-List count: " + CCW_count);
            
            if ( (desiredHeading == currentHeading) || (desiredHeading == currentHeading)// Turns both relays off.
                    || (desiredHeading + 3 == currentHeading) || (desiredHeading - 3 == currentHeading 
                    || (desiredHeading == currentHeading + 3) || (desiredHeading == currentHeading - 3))) {
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
                System.out.println();
            }
            try {
                Thread.sleep(2000);
            } catch (InterruptedException ex) {
                Logger.getLogger(JavaPilot.class.getName()).log(Level.SEVERE, null, ex);
            }
        } //End of while (true) loop
        
    } // End of main method
} // End of Class

/**
 * 
 * Internal class to ask the user for a desired heading via the command-line
 * interface. Contains a getDesiredHeading method to return the desired heading 
 * to the main method.
 */
class DesiredHeading extends TimerTask {
    protected static BufferedReader reader;
    protected static double heading;
    protected static double oldHeading;
    @Override
    public void run() {
    ExecutorService es = Executors.newSingleThreadExecutor();
    TimeLimiter timeLimiter = SimpleTimeLimiter.create(es);
       reader = new BufferedReader(
            new InputStreamReader(System.in));
       String headingStr = "";
       oldHeading = heading;
       System.out.print("Waiting for new desired heading: ");
        try { 
            headingStr = timeLimiter.callWithTimeout(reader::readLine, 10, TimeUnit.SECONDS);
        } catch (TimeoutException ex) {
            //Do nothing if it times out.
        } catch (InterruptedException ex) {
            Logger.getLogger(DesiredHeading.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ExecutionException ex) {
            Logger.getLogger(DesiredHeading.class.getName()).log(Level.SEVERE, null, ex);
        }
        if (!headingStr.isBlank()){
            double newHeading = Double.parseDouble(headingStr);
            if (newHeading > 360 || newHeading < 0) {
                heading = oldHeading;
                System.out.println("Not a valid heading.");
            }
            else {
                heading = newHeading;
            }
            System.out.println();
        }
        else {
            System.out.println("Timed out");
            heading = oldHeading;
        }
    }
    
    /**
     * Returns the desired heading as a double.
     * @return Returns the desired heading.
     */
    protected double getDesiredHeading () {
        return heading;
    }
}
