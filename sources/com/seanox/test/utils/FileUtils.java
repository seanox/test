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

import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.OutputStream;
import java.util.regex.Pattern;

/**
 *  Utils for file(system).
 */
public class FileUtils {
    
    /**
     *  Writes all bytes of a file in the passed OutputStream.
     *  @param  file
     *  @param  outputStream
     *  @return the number of transferred bytes
     *  @throws IOException
     *      In case of faulty data access.
     */
    public static long transmitFile(File file, OutputStream outputStream)
            throws IOException {
        
        try (FileInputStream inputStream = new FileInputStream(file)) {
            long volume = 0;
            byte[] bytes = new byte[65535];
            for (int length = 0; (length = inputStream.read(bytes)) >= 0; volume +=length)
                outputStream.write(bytes, 0, length);
            return volume;
        }
    }
    
    /**
     *  Normalizes a path.
     *  @param  path path
     *  @return the normalized path
     */
    public static String normalizePath(String path) {

        if (path != null)
            path = path.trim();
        if (path.isEmpty())
            return null;
        
        //die Pfadangabe wird auf Slash umgestellt
        path = path.replace('\\', '/');

        //mehrfache Slashs werden zusammengefasst
        path = path.replaceAll("/+", "/");

        //der Path wird ggf. ausgeglichen /abc/./def/../ghi -> /abc/ghi
        path = path.replaceAll("/.$", "/./");

        //der Path wird um "/." ausgeglichen
        path = path.replaceAll("(/.(?=/))+", "/");

        //der String wird um "/.." ausgeglichen
        path = path.replaceAll("/..$", "/../");

        int cursor;
        while ((cursor = path.indexOf("/../")) >= 0) {

            String swap;
            swap = path.substring(cursor +3);
            path = path.substring(0, cursor);
            path = path.substring(0, Math.max(0, path.lastIndexOf("/"))).concat(swap);
        }

        //mehrfache Slashs werden zusammengefasst
        path = path.replaceAll("/+", "/");

        //mehrfache Slashs am Ende werden entfernt
        path = path.replaceAll("([^/])/+$", "$1");

        return path;
    }
    
    /**
     *  FilePatternFilter to filter and accept files by a pattern.
     *  The filter supports {@code *} and {@code ?}.
     *  Multiple conditions are separated by commas.<br>
     *    <dir>e.g. {@code new FilenamePatternFilter(".*html, *.jpg")}</dir>
     */
    public static class FilePatternFilter implements FilenameFilter {

        /** compiled pattern */
        private Pattern pattern;
        
        /**
         *  Constructor, creates a new FilePatternFilter object.
         *  @param pattern pattern
         */
        public FilePatternFilter(String pattern) {
            
            if (pattern == null)
                pattern = "";
            pattern = pattern.trim();
            
            pattern = pattern.replace('\\', '/');
            pattern = pattern.replaceAll("\\/+", "/");
            pattern = pattern.replaceAll("\\*", "\\\\E.*\\\\Q");
            pattern = pattern.replaceAll("\\?", "\\\\E.\\\\Q");
            pattern = "^(?:\\Q" + pattern + "\\E)$";
            pattern = pattern.replaceAll("\\s*,+\\s*", "\\\\E)$|^(?:\\\\Q");
            pattern = "(?i)" + pattern;
            this.pattern = Pattern.compile(pattern);
        }

        /**
         *  Tests if a specified file should be included in a file list.
         *  @param  file file this was found
         *  @return {@code true} if and only if the name should be included in
         *          the file list; {@code false} otherwise.
         */
        public boolean accept(File file) {
            return this.accept(file.getParentFile(), file.getName());
        }
        
        @Override
        public boolean accept(File dir, String name) {

            name = dir + "/" + name;
            name = name.replaceAll("\\\\+", "/");
            
            return this.pattern.matcher(name).matches();
        }
    }
    
    /**
     *  Returns the temp directory as file.
     *  @return the temp directory as file
     */
    public static File getTempDir() {
        return new File(System.getProperty("java.io.tmpdir"));
    }
}