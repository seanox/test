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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *  Utils for text and strings.
 */
public class TextUtils {
    
    /** Naturally sort comparator */
    public static final Comparator<String> NATURAL_COMPARATOR = new TextUtils.NaturalComparator();
    
    /**
     *  Splits this string around matches of the given <a
     *  href="../util/regex/Pattern.html#sum">regular expression</a>.
     *  Trailing empty strings ARE THEREFORE INCLUDED in the resulting array.  
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
                result.add(string);
                break;
            }
            result.add(string.substring(0, matcher.start()));
            string = string.substring(matcher.end());
        }
        
        return result.toArray(new String[0]);
    }
    
    /**
     *  Extracts all lines of a string.
     *  Empty lines ARE THEREFORE INCLUDED in the resulting array.  
     *  @param  string  string
     *  @return the array of lines
     */
    public static String[] extractLines(String string) {
        
        int cursor;
        if (string == null)
            string = "";
        List<String> lineList = new ArrayList<>();
        string = string.replaceAll("(?s)((\r\n)|(\n\r)|[\r\n])", "\n");
        while ((cursor = string.indexOf('\n')) >= 0) {
            lineList.add(string.substring(0, cursor));
            string = string.substring(cursor +1);
        }
        if (string.length() > 0)
            lineList.add(string);
        return lineList.toArray(new String[0]);
    }
    
    /**
     *  Dekodiert alle Ausgabe-Direktiven nach einem Backslash.
     *  Die Methode ist tollerant und erh&auml;t fehlerhafte Direktiven.
     *  @param  string zu dekodierender String
     *  @return der dekodierte String
     */
    public static String unescape(String string) {
        
        byte[] bytes;

        int    code;
        int    count;
        int    length;
        int    loop;
        
        if (string == null)
            string = "";
        
        //Datenpuffer wird eingerichtet
        length = string.length();
        bytes  = new byte[length *2];
        
        for (loop = count = 0; loop < length; loop++) {
            
            //der ASCII Code wird ermittelt
            code = string.charAt(loop);

            //der Hexcode wird in das ASCII Zeichen umgesetzt
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
     *  Kodiert im String die Steuerzeichen: BS, HT, LF, FF, CR, ', ",  \ und
     *  alle Zeichen ausserhalb vom ASCII-Bereich 0x20-0x7F. Die Maskierung
     *  erfolgt per Slash + ISO oder dem Hexadezimal-Wert.
     *  @param  string zu maskierender String
     *  @return der String mit den ggf. maskierten Zeichen.
     *  @throws Exception
     *      Im Fall nicht erwarteter Fehler
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
        
        //Datengroesse wird ermittelt
        length = string.length();
        
        //Datenpuffer wird eingerichtet
        cache = new byte[length *3];
        
        codex = ("\b\t\n\f\r\"'\\btnfr\"'\\").getBytes();
        codec = ("0123456789abcdef").getBytes();
        
        for (loop = count = 0; loop < length; loop++) {
            
            //der ASCII Code wird ermittelt
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

        @Override
        public int compare(String o1, String o2) {

            o1 = o1 == null ? "" : o1.trim();
            o1 = o1.replaceAll("\\x00", " ");
            o1 = o1.replaceAll("(\\d+)", "\00$1\00");
            String o11 = "";
            for (String s : o1.split("\\00")) {
                try {
                    s = Long.valueOf(s).toString();
                    s = Long.toString(s.length(), 36).toUpperCase() + s;
                } catch (Exception exception) {
                }
                o11 += s;
            }
            
            o2 = o2 == null ? "" : o2.trim();
            o2 = o2.replaceAll("\\x00", " ");
            o2 = o2.replaceAll("(\\d+)", "\00$1\00");
            String o22 = "";
            for (String s : o2.split("\\00")) {
                try {
                    s = Long.valueOf(s).toString();
                    s = Long.toString(s.length(), 36).toUpperCase() + s;
                } catch (Exception exception) {
                }
                o22 += s;
            }
            
            return o11.compareTo(o22);
        }
    }
}