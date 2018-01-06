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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *  Utilities for text and strings.<br>
 *  <br>
 *  TextUtils 1.1 20180106<br>
 *  Copyright (C) 2018 Seanox Software Solutions<br>
 *  All rights reserved.
 *
 *  @author  Seanox Software Solutions
 *  @version 1.1 20180106
 */
public class TextUtils {
    
    /** Naturally sort comparator */
    public static final Comparator<String> NATURAL_COMPARATOR = new TextUtils.NaturalComparator();
    
    /** Constructor, creates a new TextUtils object. */
    private TextUtils() {
    }    
    
    /**
     *  Splits this string around matches of the given regular expression.
     *  Repeated matches are not combined and creates empty entries in the
     *  array.
     *  @param  string string
     *  @param  regex  the delimiting regular expression
     *  @return the array of strings computed by splitting this string around
     *          matches of the given regular expression
     */
    public static String[] split(String string, String regex) {
        
        Pattern pattern = Pattern.compile(regex);
        List<String> result = new ArrayList<>();
        while (true) {
            Matcher matcher = pattern.matcher(string);
            if (!matcher.find()) {
                if (string.length() > 0)
                    result.add(string);
                break;
            }
            result.add(string.substring(0, matcher.start()));
            string = string.substring(matcher.end());
        }
        
        return result.toArray(new String[0]);
    }
    
    /**
     *  Decodes all output directives after a backslash.
     *  The method is tollerant and keeps incorrect directives.
     *  @param  string string to be decoded 
     *  @return the decoded string
     */
    public static String unescape(String string) {
        
        byte[] bytes;

        int    code;
        int    count;
        int    length;
        int    loop;
        
        if (string == null)
            string = "";
        
        //the data buffer is established
        length = string.length();
        bytes  = new byte[length *2];
        
        for (loop = count = 0; loop < length; loop++) {
            
            //the ASCII code is determined
            code = string.charAt(loop);

            //the hexcode is converted into ASCII character
            if (code == '\\') {
                loop += 2;
                try {code = Integer.parseInt(string.substring(loop -1, loop +1), 16);
                } catch (Throwable throwable) {
                    loop -= 2;
                }
            }

            bytes[count++] = (byte)code;
        }
        
        return new String(Arrays.copyOfRange(bytes, 0, count));
    }
    
    /**
     *  Encodes the control characters: BS, HT, LF, FF, CR, ', ", \ and all
     *  characters outside the ASCII range 0x20-0x7F. The escaping is done by
     *  Slash + ISO or the hexadecimal value.
     *  @param  string string to be escaped
     *  @return the escaped string
     */
    public static String escape(String string) {
        
        byte[] codec;
        byte[] codex;
        byte[] cache;

        int    code;
        int    count;
        int    cursor;
        int    length;
        int    loop;
        
        //determination of data size
        length = string.length();
        
        //the data buffer is established
        cache = new byte[length *3];
        
        codex = ("\b\t\n\f\r\"'\\btnfr\"'\\").getBytes();
        codec = ("0123456789abcdef").getBytes();
        
        for (loop = count = 0; loop < length; loop++) {
            
            //the ASCII code is determined
            code = string.charAt(loop);
            
            cursor = Arrays.binarySearch(codex, (byte)code);
            if (cursor >= 0 && cursor < 8) {
                cache[count++] = '\\';
                cache[count++] = codex[cursor +8];
            } else if (code < 0x20 || code > 0x7F) {
                cache[count++] = '\\';
                cache[count++] = codec[(code >> 4) & 0xF];
                cache[count++] = codec[(code & 0xF)];
            } else cache[count++] = (byte)code;
        }
        
        return new String(Arrays.copyOfRange(cache, 0, count));        
    }
    
    /**
     *  Returns a string without any leading and trailing white spaces.
     *  The value {@code null} becomes an empty string. 
     *  @param  string string to be trimemd
     *  @return string without any leading and trailing white spaces
     */ 
    public static String trim(String string) {
        return TextUtils.trim(string, false);
    }
    
    /**
     *  Returns a string without any leading and trailing white spaces.
     *  The value {@code null} becomes an empty string. If <i>nullable</i> is
     *  {@code true}, {@code null} is returned if the string is empty.
     *  @param  string   string to be trimemd
     *  @param  nullable {@code true} returns {@code null} if string is empty
     *  @return string without any leading and trailing white spaces
     *          or {@code null} is used <i>nullable</i> and the string is empty
     */     
    public static String trim(String string, boolean nullable) {
        
        if (string == null) {
            if (nullable)
                return null;
            string = "";
        } else string = string.trim();
        return string.isEmpty() && nullable ? null : string; 
    }

    /**
     *  Natural sorting of strings with alphanumeric content.
     *  @param  strings array to sort
     *  @return natural sorted string
     */
    public static String[] sortNatural(String... strings) {
        
        List<String> stringList = Arrays.asList(strings);
        Collections.sort(stringList, NATURAL_COMPARATOR);
        return stringList.toArray(new String[0]);
    }
    
    /** Naturally sort comparator */
    private static class NaturalComparator implements Comparator<String> {
        
        /**
         *  Normalizes the numeric fragments that they can be sorted.
         *  @param  string string to be escaped
         *  @return the normalized string
         */
        private static String normalize(String string) {
            
            string = TextUtils.trim(string);
            String buffer = "";
            for (String fragment : string.split("(?:(?<=\\d)(?!\\d))|(?:(?<!\\d)(?=\\d))")) {
                try {
                    fragment = Long.valueOf(fragment).toString();
                    fragment = Long.toString(fragment.length(), 36).toUpperCase() + fragment;
                } catch (Exception exception) {
                }
                buffer += fragment;
            }
            return buffer;
        }

        @Override
        public int compare(String string1, String string2) {

            string1 = NaturalComparator.normalize(string1);
            string2 = NaturalComparator.normalize(string2);
            
            return string1.compareTo(string2);
        }
    }
}