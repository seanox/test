/**
 * LIZENZBEDINGUNGEN - Seanox Software Solutions ist ein Open-Source-Projekt,
 * im Folgenden Seanox Software Solutions oder kurz Seanox genannt.
 * Diese Software unterliegt der Version 2 der GNU General Public License.
 *
 * Seanox Test SDK
 * Copyright (C) 2017 Seanox Software Solutions
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of version 2 of the GNU General Public License as published
 * by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 * more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */
package com.seanox.test.utils;

import java.lang.management.ManagementFactory;

import com.sun.management.OperatingSystemMXBean;

/**
 * Platform-specific informations for the operating system on which the
 * Java virtual machine is running.<br>
 * <br>
 * SystemInfo 1.0 20171231<br>
 * Copyright (C) 2017 Seanox Software Solutions<br>
 * All rights reserved.
 *
 * @author  Seanox Software Solutions
 * @version 1.0 20171231
 */
public class SystemInfo {

    private static SystemInfo instance;
    
    /** Managed bean with system information */
    private volatile OperatingSystemMXBean osmx;     
    
    static {
        SystemInfo.instance = new SystemInfo();
    }
    
    private SystemInfo() {
        this.osmx = (OperatingSystemMXBean)ManagementFactory.getOperatingSystemMXBean();
    }
    
    /**
     * Returns the operating system architecture.
     * @return the operating system architecture
     */        
    public static String getArch() {
        return SystemInfo.instance.osmx.getArch();
    }
    
    /**
     * Returns the number of processors available to the Java virtual
     * machine. This value may change during a particular invocation of
     * the virtual machine.
     * @return the number of processors available to the virtual
     *         machine; never smaller than one
     */        
    public static int getAvailableProcessors() {
        return SystemInfo.instance.osmx.getAvailableProcessors();
    }
    
    /**
     * Returns the operating system name.
     * @return the operating system name.
     */        
    public static String getName() {
        return SystemInfo.instance.osmx.getName();
    }
    
    /**
     * Returns the system load average for the last minute.
     * The system load average is the sum of the number of runnable
     * entities queued to the {@linkplain #getAvailableProcessors
     * available processors} and the number of runnable entities running
     * on the available processors averaged over a period of time. The way
     * in which the load average is calculated is operating system
     * specific but is typically a damped time-dependent average.
     * @return the system load average; or a negative value if not
     *         available
     */        
    public static double getSystemLoadAverage() {
        return SystemInfo.instance.osmx.getSystemLoadAverage();
    }
    
    /**
     * Returns the operating system version.
     * @return the operating system version.
     */        
    public static String getVersion() {
        return SystemInfo.instance.osmx.getVersion();
    }
    
    /**
     * Returns the amount of virtual memory that is guaranteed to be
     * available to the running process in bytes, or -1 if this operation
     * is not supported.
     * @return the amount of virtual memory that is guaranteed to be
     *         available to the running process in bytes, or -1 if this
     *         operation is not supported
     */
    public static long getCommittedVirtualMemorySize() {
        return SystemInfo.instance.osmx.getCommittedVirtualMemorySize();
    }
   
    /**
     * Returns the amount of free physical memory in bytes.
     * @return the amount of free physical memory in bytes
     */
    public static long getFreePhysicalMemorySize() {
        return SystemInfo.instance.osmx.getFreePhysicalMemorySize();
    }
    
    /**
     * Returns the amount of free swap space in bytes.
     * @return the amount of free swap space in bytes
     */
    public static long getFreeSwapSpaceSize() {
        return SystemInfo.instance.osmx.getFreeSwapSpaceSize();
    }
    
    /**
     * Returns the "recent cpu usage" for the Java VM process.
     * @return the "recent cpu usage" for the Java VM process
     */
    public static double getProcessCpuLoad() {
        return SystemInfo.instance.osmx.getProcessCpuLoad();
    }
    
    /**
     * Returns the CPU time used by the process on which the Java virtual
     * machine is running in nanoseconds.
     * @return the CPU time used by the process on which the Java virtual
     *         machine is running in nanoseconds
     */
    public static long getProcessCpuTime() {
        return SystemInfo.instance.osmx.getProcessCpuTime();
    }
    
    /**
     * Returns the "recent cpu usage" for the whole system.
     * @return the "recent cpu usage" for the whole system
     */
    public static double getSystemCpuLoad() {
        return SystemInfo.instance.osmx.getSystemCpuLoad();
    }

    /**
     * Returns the "recent memory usage" for the whole system.
     * @return the "recent memory usage" for the whole system
     */
    public static long getSystemMemoryLoad() {
        Runtime runtime = Runtime.getRuntime();
        return runtime.totalMemory() -runtime.freeMemory();
    }        
    
    /**
     * Returns the total amount of physical memory in bytes.
     * @return the total amount of physical memory in bytes
     */
    public static long getTotalPhysicalMemorySize() {
        return SystemInfo.instance.osmx.getTotalPhysicalMemorySize();
    }
    
    /**
     * Returns the total amount of swap space in bytes.
     * @return the total amount of swap space in bytes
     */
    public static long getTotalSwapSpaceSize() {
        return SystemInfo.instance.osmx.getTotalSwapSpaceSize();
    }
}