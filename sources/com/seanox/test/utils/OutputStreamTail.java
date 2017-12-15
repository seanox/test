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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 *  Data stream filter for querying the latest data (tail).
 */
public class OutputStreamTail extends OutputStream {
    
    /** data buffer */
    private volatile ByteArrayOutputStream data;
    
    /** maximum amount of data */
    private volatile int size;
    
    /** Constructor creates a new OutputStreamTail object for 65535 bytes. */
    public OutputStreamTail() {
        this(65535);
    }
    
    /**
     *  Constructor creates a new OutputStreamTail object for a amount of data.
     *  @param size
     */
    public OutputStreamTail(int size) {
        
        if (size <= 0)
            throw new IllegalArgumentException();
        this.size = size;
        this.data = new ByteArrayOutputStream(size);
    }

    @Override
    public void write(int data) throws IOException {

        byte[] temp;
        
        synchronized (this.data) {
            if (this.data.size() >= this.size) {
                temp = this.data.toByteArray();
                this.data.reset();
                this.write(temp, 1, temp.length -1);
            }
            
            this.data.write(data);
        }
    }
    
    @Override
    public void write(byte[] data, int offset, int length) throws IOException {
        
        byte[] temp;
        
        synchronized (this.data) {

            int size = 0;
            if (length > this.size)
                size = Math.max(0, length -this.size);

            temp = new byte[length];
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
            return this.data.toByteArray();
        }
    }
    
    @Override
    public String toString() {
        return new String(this.toByteArray());
    }
}