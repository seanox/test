/**
 *  LIZENZBEDINGUNGEN - Seanox Software Solutions ist ein Open-Source-Projekt,
 *  im Folgenden Seanox Software Solutions oder kurz Seanox genannt.
 *  Diese Software unterliegt der Version 2 der GNU General Public License.
 *
 *  Seanox Test Utilities
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

import java.io.IOException;
import java.io.OutputStream;

/**
 *  Multiple data stream.
 *  Bundles multiple data streams and uses them as one data stream.
 */
public class OutputStreams extends OutputStream {
    
    /** array of data streams */
    private OutputStream[] outputStreams;
    
    /**
     *  Constructor creates a new OutputStreams object.
     *  @param outputStreams
     */
    public OutputStreams(OutputStream... outputStreams) {
        this.outputStreams = outputStreams;
    }

    @Override
    public void write(int data) throws IOException {

        for (OutputStream outputStream : this.outputStreams)
            outputStream.write(data);
    }
    
    @Override
    public void write(byte[] data, int offset, int length) throws IOException {
        
        for (OutputStream outputStream : this.outputStreams)
            outputStream.write(data, offset, length);
    }
    
    @Override
    public void flush() throws IOException {

        for (OutputStream outputStream : this.outputStreams)
            outputStream.flush();
    }
    
    @Override
    public void close() throws IOException {

        for (OutputStream outputStream : this.outputStreams)
            outputStream.close();
    }
}