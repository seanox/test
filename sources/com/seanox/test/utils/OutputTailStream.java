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

/**
 *  Data stream filter for querying the latest data (tail).
 *  The data stream supports two modes.
 *  
 *  <h3>Bufferd Mode</h3>
 *  Is initialized via {@link #OutputTailStream(int)}.<br>
 *  The tail has a maximum length and always contains the last written data.
 *  
 *  <h3>Dynamic Mode</h3>
 *  Is initialized via {@link #OutputTailStream()}.<br>
 *  The length of the tail is dynmic. It always contains the data written since
 *  the last call of {@link #toByteArray()} and {@link #toString()}. In other
 *  words, the method {@link #toByteArray()} and {@link #toString()} always
 *  reset the tail.<br>
 *  <br>
 *  OutputTailStream 1.0 20171212<br>
 *  Copyright (C) 2017 Seanox Software Solutions<br>
 *  All rights reserved.
 *
 *  @author  Seanox Software Solutions
 *  @version 1.0 20171212
 */
public class OutputTailStream extends OutputStream {
    
    /** data buffer */
    protected volatile ByteArrayOutputStream data;
    
    /** maximum amount of data */
    protected volatile int size;
    
    /** Constructor creates a new OutputStreamTail object for 65535 bytes. */
    public OutputTailStream() {
        this.data = new ByteArrayOutputStream(65535);
    }

    /**
     *  Constructor creates a new OutputStreamTail object for a amount of data.
     *  @param size
     */
    public OutputTailStream(int size) {
        
        if (size <= 0)
            throw new IllegalArgumentException();
        this.size = size;
        this.data = new ByteArrayOutputStream(size);
    }

    @Override
    public void write(int data) throws IOException {

        byte[] temp;
        
        synchronized (this.data) {
            if (this.size > 0
                    && this.data.size() >= this.size) {
                temp = this.data.toByteArray();
                this.data.reset();
                this.write(temp, 1, temp.length -1);
            }
            
            this.data.write(data);
        }
    }
    
    @Override
    public void write(byte[] data, int offset, int length) throws IOException {
        
        synchronized (this.data) {

            if (this.size > 0) {
                int size = 0;
                if (length > this.size)
                    size = Math.max(0, length -this.size);
    
                byte[] temp = new byte[length];
                System.arraycopy(data, offset +size, temp, 0, length -size);
                data = temp;
                
                if (this.data.size() +data.length <= this.size) {
                    this.data.write(data);
                    return;
                }
                
                temp = this.data.toByteArray();
                this.data.reset();
                
                size = Math.max(0, this.size -data.length);
                if (size > 0)
                    this.data.write(temp, temp.length -size, size);
            }
            
            this.data.write(data);
        }
    }
    
    @Override
    public void flush() throws IOException {
        synchronized (this.data) {
            this.data.flush();
        }
    }
    
    @Override
    public void close() throws IOException {
        synchronized (this.data) {
            this.data.close();
        }
    }
    
    /**
     *  Creates a newly allocated byte array.
     *  @return the current contents of this output stream, as a byte array
     */
    public byte[] toByteArray() {
        synchronized (this.data) {
            try {return this.data.toByteArray();
            } finally {
                if (this.size <= 0)
                    this.data.reset();
            }
        }
    }
    
    @Override
    public String toString() {
        return new String(this.toByteArray());
    }
}