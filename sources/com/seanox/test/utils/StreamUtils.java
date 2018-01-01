/**
 *  LIZENZBEDINGUNGEN - Seanox Software Solutions ist ein Open-Source-Projekt,
 *  im Folgenden Seanox Software Solutions oder kurz Seanox genannt. Diese
 *  Software unterliegt der Version 2 der GNU General Public License.
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

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;

/**
 *  Utilities for streams.<br>
 *  <br>
 *  StreamUtils 1.0 20171212<br>
 *  Copyright (C) 2017 Seanox Software Solutions<br>
 *  All rights reserved.
 *
 *  @author  Seanox Software Solutions
 *  @version 1.0 20171212
 */
public class StreamUtils {
    
    /** Constructor, creates a new StreamUtils object. */
    private StreamUtils() {
    }    

    /**
     *  Reads all bytes from a data stream.
     *  @param  input input stream
     *  @return readed bytes as array
     *  @throws IOException
     *      In case of incorrect access to the data stream
     */
    public static byte[] read(InputStream input)
            throws IOException {
        return StreamUtils.read(input, false);
    }
    
    /**
     *  Reads all bytes from a data stream.
     *  @param  input input stream
     *  @param  smart reads until the data stream no longer supplies data.
     *  @return readed bytes as array
     *  @throws IOException
     *      In case of incorrect access to the data stream
     */
    public static byte[] read(InputStream input, boolean smart)
            throws IOException {

        if (!(input instanceof BufferedInputStream))
            input = new BufferedInputStream(input);

        byte[] bytes = new byte[65535];
        ByteArrayOutputStream result = new ByteArrayOutputStream();
        int size;
        while ((size = input.read(bytes)) >= 0) {
            result.write(bytes, 0, size);
            if (smart && input.available() <= 0
                    && result.size() > 0)
                break;
        } 
        
        return result.toByteArray();
    }
    
    /**
     *  Forwards the contents of a data stream.
     *  The methods works like the {@link InputStream#read(byte[])}.
     *  @param  input  data stream from
     *  @param  output data stream to
     *  @return the total number of read/forward bytes, or -1 if there is no
     *          more data because the end of the stream has been reached
     *  @throws IOException
     *      If the first byte cannot be read for any reason other than the end
     *      of the stream, if the input stream has been closed, or if some other
     *      I/O error occurs
     */
    public static long transmit(InputStream input, OutputStream output)
            throws IOException {
        return StreamUtils.transmit(input, output, 0);
    }
    
    /**
     *  Forwards the contents of a data stream.
     *  The methods works like the {@link InputStream#read(byte[])}.
     *  @param  input  data stream from
     *  @param  output data stream to
     *  @param  offset skips over and discards bytes of data from the input stream
     *  @return the total number of read/forward bytes, or -1 if there is no
     *          more data because the end of the stream has been reached
     *  @throws IOException
     *      If the first byte cannot be read for any reason other than the end
     *      of the stream, if the input stream has been closed, or if some other
     *      I/O error occurs
     */
    public static long transmit(InputStream input, OutputStream output, long offset)
            throws IOException {

        input.skip(offset);
        long volume = 0;
        byte[] bytes = new byte[65535];
        for (int length = 0;
                (length = input.read(bytes)) >= 0;
                volume +=length)
            output.write(bytes, 0, length);
        return volume;
    }      
    
    /**
     *  Reads the last bytes from a data stream.
     *  @param  input  input stream
     *  @param  length number of bytes at the end
     *  @return readed bytes as array
     *  @throws IOException
     *      In case of incorrect access to the data stream
     */    
    public static byte[] tail(InputStream input, int length)
            throws IOException {
        return StreamUtils.tail(input, length, false);
    }
    
    /**
     *  Reads the last bytes from a data stream.
     *  @param  input  input stream
     *  @param  length number of bytes at the end
     *  @param  smart  reads until the data stream no longer supplies data
     *  @return readed bytes as array
     *  @throws IOException
     *      In case of incorrect access to the data stream
     */       
    public static byte[] tail(InputStream input, int length, boolean smart)
            throws IOException {
        
        if (length < 0)
            throw new IllegalArgumentException("Invalid length");
        
        if (!(input instanceof BufferedInputStream))
            input = new BufferedInputStream(input);        
        
        byte[] bytes = new byte[65535];
        ByteArrayOutputStream result = new ByteArrayOutputStream(0);
        int size;
        while ((size = input.read(bytes)) >= 0) {
            result.write(bytes, 0, size);
            if (result.size() > length) {
                byte[] temp = result.toByteArray();
                temp = Arrays.copyOfRange(temp, temp.length -length, temp.length);
                result.reset();
                result.write(temp);
            }
            if (smart && input.available() <= 0
                    && result.size() > 0)
                break;
        } 
        
        return result.toByteArray();
    }    
}