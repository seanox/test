/**
 *  LIZENZBEDINGUNGEN - Seanox Software Solutions ist ein Open-Source-Projekt,
 *  im Folgenden Seanox Software Solutions oder kurz Seanox genannt.
 *  Diese Software unterliegt der Version 2 der GNU General Public License.
 *
 *  Seanox Test SDK
 *  Copyright (C) 2017 Seanox Software Solutions
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
 *  OutputFacadeStream 1.0 20171231<br>
 *  Copyright (C) 2017 Seanox Software Solutions<br>
 *  All rights reserved.
 *
 *  @author  Seanox Software Solutions
 *  @version 1.0 20171231
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
     *  @param outputStreams
     */
    public OutputFacadeStream(OutputStream... outputStreams) {
        
        if (outputStreams == null)
            outputStreams = new OutputStream[0];
        this.outputStreams = new HashSet<>();
        this.mount(outputStreams);
    }
    
    /**
     *  Adds one or more output streams.
     *  @param outputStreams
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
     *  @param outputStreams
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
        for (OutputStream outputStream : streams)
            outputStream.write(data, offset, length);
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
        
        @Override
        public void close() throws IOException {
            
            OutputFacadeStream.this.unmount(this);
            super.close();
        }
    }
}