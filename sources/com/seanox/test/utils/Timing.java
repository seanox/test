/**
 *  LIZENZBEDINGUNGEN - Seanox Software Solutions ist ein Open-Source-Projekt,
 *  im Folgenden Seanox Software Solutions oder kurz Seanox genannt.
 *  Diese Software unterliegt der Version 2 der GNU General Public License.
 *
 *  Seanox Test SDK
 *  Copyright (C) 2018 Seanox Software Solutions
 *
 *  This program is free software; you can redistribute it and/or modify it
 *  under the terms of version 2 of the GNU General Public License as published
 *  by the Free Software Foundation.
 *
 *  This program is distributed in the hope that it will be useful, but WITHOUT
 *  ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 *  FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 *  more details.
 *
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */
package com.seanox.test.utils;

import org.junit.Assert;

/** 
 *  Simple time measurement and testing.<br>
 *  <br>
 *  Timing 1.0 20180102<br>
 *  Copyright (C) 2018 Seanox Software Solutions<br>
 *  All rights reserved.
 *
 *  @author  Seanox Software Solutions
 *  @version 1.0 20180102
 */
public class Timing {

    /** start time */
    private Long startTime;
    
    /** stop time */
    private Long stopTime;
    
    /** Constructor, creates a new Timing object. */
    private Timing() {
        this.reset();
    }
    
    /**
     *  Creates a new Timing object.
     *  The time measurement is not started automatically.
     *  @return the created Timing object
     */
    public static Timing create() {
        return Timing.create(false);
    }
    
    /**
     *  Creates a new Timing object.
     *  With parameter 'start', the measurement will start automatically.
     *  @param  start {@code true} starts with the creation
     *  @return the created Timing object
     */        
    public static Timing create(boolean start) {
        
        Timing timing = new Timing();
        if (start)
            timing.start();
        return timing;
    }

    /** Starts the measurement, if it is not running yet. */
    public void start() {
        
        long delta = 0;
        if (this.stopTime != null)
            delta = this.stopTime.longValue() -this.startTime.longValue();
        
        if (this.startTime == null)
            this.startTime = Long.valueOf(System.currentTimeMillis() -delta);
    }
    
    /** Stopps the measurement, if it is running. */
    public void stop() {
        
        if (this.startTime != null)
            this.stopTime = Long.valueOf(System.currentTimeMillis());
    }
    
    /** Resets the measurement. */
    public void reset() {
        
        this.startTime = null;
        this.stopTime  = null;
    }
    
    /** Restarts the measurement (reset + start). */
    public void restart() {
        
        this.reset();
        this.start();
    }
    
    /**
     *  Gets the current measured time in milliseconds.
     *  @return the current measured time in milliseconds
     */
    public long timeMillis() {

        if (this.startTime == null)
            return 0;
        return System.currentTimeMillis() -this.startTime.longValue();
    }
    
    /**
     *  Checks whether the currently measured time is greater than or equal
     *  to the specified millisecond.
     *  @param milliseconds milliseconds
     */
    public void assertTimeOut(int milliseconds) {
        if (this.timeMillis() < milliseconds)
            Assert.assertEquals("out of " + milliseconds + " ms", this.timeMillis() + " ms");
    }

    /**
     *  Checks whether the currently measured time is outside of the
     *  specified time frame in milliseconds.
     *  @param millisecondsFrom milliseconds from
     *  @param millisecondsTo   milliseconds To
     */
    public void assertTimeOut(int millisecondsFrom, int millisecondsTo) {
        
        long time = this.timeMillis();
        if (time >= millisecondsFrom && time <= millisecondsTo)
            Assert.assertEquals("out of " + millisecondsFrom + " - " + millisecondsTo + " ms", time + " ms");
    } 

    /**
     *  Checks whether the currently measured time is less than or equal to
     *  the specified millisecond.
     *  @param milliseconds milliseconds
     */
    public void assertTimeIn(int milliseconds) {
        if (this.timeMillis() > milliseconds)
            Assert.assertEquals("in " + milliseconds + " ms", this.timeMillis() + " ms");
    }   
    
    /**
     *  Checks whether the currently measured time is within the specified
     *  time frame in milliseconds.
     *  @param millisecondsFrom milliseconds from
     *  @param millisecondsTo   milliseconds To
     */
    public void assertTimeIn(int millisecondsFrom, int millisecondsTo) {
        
        long time = this.timeMillis();
        if (time < millisecondsFrom || time > millisecondsTo)
            Assert.assertEquals("in " + millisecondsFrom + " - " + millisecondsTo + " ms", time + " ms");
    }   
}