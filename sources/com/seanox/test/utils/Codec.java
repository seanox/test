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

import java.util.Base64;

/**
 *  Codec utilities for encoding and decoding.<br>
 *  <br>
 *  Codec 1.0 20171212<br>
 *  Copyright (C) 2017 Seanox Software Solutions<br>
 *  All rights reserved.
 *
 *  @author  Seanox Software Solutions
 *  @version 1.0 20171212
 */
public class Codec {
    
    /** Constructor, creates a new Codec object. */
    private Codec() {
    }    

    /**
     *  Encodes a text in Base64.
     *  @param  text text
     *  @return the encoded text
     */
    public static String encodeBase64(String text) {
        
        if (text == null)
            return null;
        return Codec.encodeBase64(text.getBytes());
    }

    /**
     *  Encodes bytes in Base64.
     *  @param  bytes bytes
     *  @return the encoded bytes
     */
    public static String encodeBase64(byte[] bytes) {

        if (bytes == null)
            return null;
        return new String(Base64.getEncoder().encode(bytes));
    }
    
    /**
     *  Encodes a text hexadecimal.
     *  @param  text text
     *  @return the encoded text
     */    
    public static String encodeHex(String text) {

        if (text == null)
            return null;
        return Codec.encodeHex(text.getBytes());
    }

    /**
     *  Encodes bytes in hexadecimal.
     *  @param  bytes bytes
     *  @return the encoded bytes
     */    
    public static String encodeHex(byte[] bytes) {
        
        if (bytes == null)
            return null;
        StringBuilder builder = new StringBuilder(bytes.length *2);
        for (byte digit : bytes)
            builder.append(String.format("%02x", Byte.valueOf(digit)));
        return builder.toString();
    }
}