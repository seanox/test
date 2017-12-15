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
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 *  Utils for resources.
 *      <dir>Context Content</dir>
 *  This is a simple text content from the ClassPath.<br>
 *  The content is based on a text file (file extension: txt) which is locate
 *  to a class in the same package. The content consists of sections. Sections
 *  also correspond to the names of methods from the context class.<br>
 *  A section starts at the beginning of the line with {@code #### <name>} and
 *  ends with the following or the file end.
 *      <dir>Context Sub-Content</dir>
 *  Context with decimal decimal value, separated by an underscore.
 *  Sub-Content can also be used as a set.    
 */
public class ResourceUtils {
    
    /**
     *  Determines the context (package, class, method) from the current call.
     *  @return context (package, class, method) from the current call
     */
    private static StackTraceElement getCurrentContext() {
        
        Throwable throwable = new Throwable();
        for (StackTraceElement stackTraceElement : throwable.getStackTrace()) {
            if (!ResourceUtils.class.getName().equals(stackTraceElement.getClassName()))
                return stackTraceElement;
        }
        return null;
    }
    
    /**
     *  Determines the context content for the called class.
     *  @param  name      name of content
     *  @param  normalize converts line breaks into the system standard
     *  @return content to the called class, otherwise {@code null}
     */
    public static String getCurrentContextContent() {

        StackTraceElement stackTraceElement = ResourceUtils.getCurrentContext();
        Class<?> context;
        try {context = Class.forName(stackTraceElement.getClassName());
        } catch (ClassNotFoundException exception) {
            return null;
        }
        String contextName = stackTraceElement.getClassName().replaceAll("\\.", "/") + ".txt";
        InputStream inputStream = context.getClassLoader().getResourceAsStream(contextName);
        try {return new String(StreamUtils.read(inputStream));
        } catch (IOException exception) {
            return null;
        } 
    }    
    
    /**
     *  Determines the context (sub)content for the called class as array.
     *  @param  name      name of content
     *  @param  normalize converts line breaks into the system standard
     *  @return (sub)content for the called class as array, otherwise {@code null}
     */
    public static String[] getContextContentSet() {
        return ResourceUtils.getContextContentSet(null);
    }

    /**
     *  Determines the context (sub)content for the called class as array.
     *  @param  name      name of content
     *  @param  normalize converts line breaks into the system standard
     *  @return (sub)content for the called class as array, otherwise {@code null}
     */
    public static String[] getContextContentSet(String name) {
        
        if (name == null)
            name = ResourceUtils.getCurrentContext().getMethodName();
        
        String content = ResourceUtils.getCurrentContextContent();
        if (content == null)
            return null;
        List<String> contextList = new ArrayList<>(); 
        String filter = "^#{4,}" + Pattern.LINE_SPACE + "*(" + name + "+(?:_\\d+)*)" + Pattern.LINE_SPACE + "*$";
        for (String line : content.split("[\r\n]+"))
            if (line.matches(filter))
                contextList.add(line);
        Collections.sort(contextList, TextUtils.NATURAL_COMPARATOR);
        return contextList.toArray(new String[0]);
    }
    
    /**
     *  Determines the context content for the called class and method name.
     *  @return content to the called class and method name,
     *          otherwise {@code null}
     */
    public static String getContextContent() {
        return ResourceUtils.getContextContent(-1);
    }
    
    /**
     *  Determines the context content for the called class and method name.
     *  @param  index optional index for sub content 
     *  @return content to the called class and method name,
     *          otherwise {@code null}
     */
    public static String getContextContent(int index) {
        return ResourceUtils.getContextContent(index, true);
    }
    
    /**
     *  Determines the context content for the called class and method name.
     *  @param  normalize converts line breaks into the system standard
     *  @return content to the called class and method name,
     *          otherwise {@code null}
     */
    public static String getContextContent(boolean normalize) {
        return ResourceUtils.getContextContent(-1, normalize);
    }
    
    /**
     *  Determines the context content for the called class and method name.
     *  @param  index     optional index for sub content 
     *  @param  normalize converts line breaks into the system standard
     *  @return content to the called class and method name,
     *          otherwise {@code null}
     */
    public static String getContextContent(int index, boolean normalize) {

        StackTraceElement stackTraceElement = ResourceUtils.getCurrentContext();
        return ResourceUtils.getContextContent(stackTraceElement.getMethodName(), index, normalize);
    }    
    
    /**
     *  Determines the context content for the called class and specified name.
     *  @param  name name of content
     *  @return content to the called class and specified name,
     *          otherwise {@code null}
     */
    public static String getContextContent(String name) {
        return ResourceUtils.getContextContent(name, -1);
    }
    
    /**
     *  Determines the context content for the called class and specified name.
     *  @param  name  name of content
     *  @param  index optional index for sub content 
     *  @return content to the called class and specified name,
     *          otherwise {@code null}
     */
    public static String getContextContent(String name, int index) {
        return ResourceUtils.getContextContent(name, index, true);
    }
    
    /**
     *  Determines the context content for the called class and specified name.
     *  @param  name      name of content
     *  @param  normalize converts line breaks into the system standard
     *  @return content to the called class and specified name,
     *          otherwise {@code null}
     */
    public static String getContextContent(String name, boolean normalize) {
        return ResourceUtils.getContextContent(name, -1, normalize);
    }
    
    /**
     *  Determines the context content for the called class and specified name.
     *  @param  name      name of content
     *  @param  index     optional index for sub content 
     *  @param  normalize converts line breaks into the system standard
     *  @return content to the called class and specified name,
     *          otherwise {@code null}
     */
    public static String getContextContent(String name, int index, boolean normalize) {

        if (index > 0)
            name += "_" + index;
        
        String content = ResourceUtils.getCurrentContextContent();
        if (content == null)
            return null;
        String filter = "^(?s)(?:.*?" + Pattern.LINE_BREAK + "){0,1}#{4,}" + Pattern.LINE_SPACE + "*" + name + Pattern.LINE_SPACE + "*" + Pattern.LINE_BREAK
                + "(.*?)" + "(?:" + Pattern.LINE_BREAK + "{0,1}#{4,}" + Pattern.LINE_SPACE + "*[a-zA-Z0-9_]+" + Pattern.LINE_SPACE + "*" + Pattern.LINE_BREAK + ".*){0,1}$";
        if (!content.matches(filter))
            return null;
        content = content.replaceAll(filter, "$1");
        if (normalize)
            content = content.replaceAll(Pattern.LINE_BREAK, System.lineSeparator());
        return content;
    }
}