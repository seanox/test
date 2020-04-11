/**
 *  LIZENZBEDINGUNGEN - Seanox Software Solutions ist ein Open-Source-Projekt,
 *  im Folgenden Seanox Software Solutions oder kurz Seanox genannt.
 *  Diese Software unterliegt der Version 2 der GNU General Public License.
 *
 *  Seanox Test SDK
 *  Copyright (C) 2020 Seanox Software Solutions
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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.HashSet;

/**
 *  Multiple output streams are combined into one output stream.
 *  All data written to this output stream are distributed to all registered
 *  output streams. The output streams can be flexibly added and removed at
 *  runtime or created temporarily.<br>
 *  <br>
 *  OutputFacadeStream 1.1.0 20200411<br>
 *  Copyright (C) 2020 Seanox Software Solutions<br>
 *  All rights reserved.
 *
 *  @author  Seanox Software Solutions
 *  @version 1.1.0 20200411
 */
public class OutputFacadeStream extends OutputStream {
    
    /** array of data streams */
    protected HashSet<OutputStream> outputStreams;
    
    /** Constructor creates a new OutputMultiStream object. */
    public OutputFacadeStream() {
        this((OutputStream[])null);
    }    
    
    /**
     *  Constructor creates a new OutputMultiStream object.
     *  @param outputStreams output streams
     */
    public OutputFacadeStream(OutputStream... outputStreams) {
        
        if (outputStreams == null)
            outputStreams = new OutputStream[0];
        this.outputStreams = new HashSet<>();
        this.mount(outputStreams);
    }
    
    /**
     *  Adds one or more output streams.
     *  @param outputStreams output streams
     */
    public void mount(OutputStream... outputStreams) {

        if (outputStreams == null)
            return;
        synchronized (OutputFacadeStream.class) {
            this.outputStreams.addAll(Arrays.asList(outputStreams));
        }
    }

    /**
     *  Removes one or more output streams.
     *  @param outputStreams output streams
     */
    public void unmount(OutputStream... outputStreams) {

        if (outputStreams == null)
            return;
        synchronized (OutputFacadeStream.class) {
            this.outputStreams.removeAll(Arrays.asList(outputStreams));
        }
    }
    
    /**
     *  Creates a capture stream to get the changes from now.
     *  @return the created capture stream
     */
    public Capture capture() {
        
        Capture capture = new Capture();
        this.mount(capture);
        return capture;
    }

    @Override
    public void write(int data) throws IOException {

        OutputStream[] streams;
        synchronized (OutputFacadeStream.class) {
            streams = this.outputStreams.toArray(new OutputStream[0]);
        }
        for (OutputStream outputStream : streams)
            outputStream.write(data);
    }
    
    @Override
    public void write(byte[] data, int offset, int length) throws IOException {
        
        OutputStream[] streams;
        synchronized (OutputFacadeStream.class) {
            streams = this.outputStreams.toArray(new OutputStream[0]);
        }
        for (OutputStream outputStream : streams) {
            outputStream.write(data, offset, length);
            outputStream.flush();
        }
    }
    
    @Override
    public void flush() throws IOException {

        OutputStream[] streams;
        synchronized (OutputFacadeStream.class) {
            streams = this.outputStreams.toArray(new OutputStream[0]);
        }
        for (OutputStream outputStream : streams)
            outputStream.flush();
    }
    
    @Override
    public void close() throws IOException {

        OutputStream[] streams;
        synchronized (OutputFacadeStream.class) {
            streams = this.outputStreams.toArray(new OutputStream[0]);
        }
        for (OutputStream outputStream : streams)
            outputStream.close();
    }
    
    /** Capture stream to get the changes from the stream. */
    public class Capture extends ByteArrayOutputStream {
        
        /** Constructor, creates a new Capture object. */
        private Capture() {
        }

        /**
         *  Wait until a line break can be detected.
         *  Without a line break, the method will block.
         *  @throws InterruptedException
         */        
        public void await()
                throws InterruptedException {
            try {this.await(-1);
            } catch (TimeoutException exception) {
                return;
            }            
        }
        
        /**
         *  Wait until a line break can be detected.
         *  Optionally, a timeout can be specified if the methods should not
         *  block endlessly.
         *  @param  timeout
         *  @throws TimeoutException
         *      In case when a timeout has been set that has been exceeded.
         *  @throws InterruptedException
         */
        public void await(long timeout)
                throws TimeoutException, InterruptedException {
            
            String content = this.toString();
            
            long timing = System.currentTimeMillis();
            while (true) {
                if (timeout >= 0
                        && (System.currentTimeMillis() >= timing +timeout))
                    throw new TimeoutException();
                if (content.length() != this.toString().length()
                        && this.toString().matches("(?s)^.*[\r\n]+$"))
                    break;
                Thread.sleep(25);
            }
        }
        
        /**
         *  Wait until the pattern can be detected.
         *  Without the pattern, the method will block.
         *  @param  pattern
         *  @throws InterruptedException
         */          
        public void await(String pattern)
                throws InterruptedException {
            try {this.await(pattern, -1);
            } catch (TimeoutException exception) {
                return;
            }
        }

        /**
         *  Wait until a pattern can be detected.
         *  Optionally, a timeout can be specified if the methods should not
         *  block endlessly.
         *  @param  pattern
         *  @param  timeout
         *  @throws TimeoutException
         *      In case when a timeout has been set that has been exceeded.
         *  @throws InterruptedException
         */        
        public void await(String pattern, long timeout)
                throws TimeoutException, InterruptedException {
            
            if (pattern == null
                    || pattern.trim().isEmpty())
                throw new IllegalArgumentException("Invalid pattern");

            long timing = System.currentTimeMillis();
            while (true) {
                if (timeout >= 0
                        && (System.currentTimeMillis() >= timing +timeout))
                    throw new TimeoutException();
                String content = this.toString();
                if (content.matches(pattern))
                    break;
                Thread.sleep(25);
            }
        }

        @Override
        public void close() throws IOException {
            
            OutputFacadeStream.this.unmount(this);
            super.close();
        }
        
        /** TimeoutException */
        public class TimeoutException extends Exception {

            private static final long serialVersionUID = 4355449388335541238L;
        }
    }
}