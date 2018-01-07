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

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 *  Utilities for resources.
 *  
 *  Resources are a simple text content from the ClassPath.<br>
 *  The content is based on a text file (file extension: txt) which is locate
 *  to a class in the same package.<br>
 *  <br>
 *  Furthermore, the content consists of sections.<br>
 *  Sections begin at the beginning of the line with {@code #### <name>} and
 *  ends with the following or the file end.<br>
 *  The name is unrestricted. The names of the methods can be used in the
 *  context. When the resource is called, the file is searched in the package
 *  of the class and from this file, only the segments of the currently
 *  executed method are used.<br>
 *  <br>
 *  Furthermore, the name of a section can be extended by decimal numbers at
 *  the end {@code #### <name>_<number>}. These are used as indexes.<br>
 *  <br>
 *  ResourceUtils 2.0 20180107<br>
 *  Copyright (C) 2018 Seanox Software Solutions<br>
 *  All rights reserved.
 *
 *  @author  Seanox Software Solutions
 *  @version 2.0 20180107
 */
public class ResourceUtils {
    
    /** Constructor, creates a new ResourceUtils object. */
    private ResourceUtils() {
    }      
    
    /**
     *  Determines the context (package, class, method) from the current call.
     *  @return context (package, class, method) from the current call
     */
    private static StackTraceElement getContext() {
        
        Throwable throwable = new Throwable();
        for (StackTraceElement stackTraceElement : throwable.getStackTrace())
            if (!ResourceUtils.class.getName().equals(stackTraceElement.getClassName()))
                return stackTraceElement;
        return null;
    }
    
    /**
     *  Determines the context content for the called class.
     *  @return content to the called class, otherwise {@code null}
     */
    public static String getContentPlain() {

        StackTraceElement stackTraceElement = ResourceUtils.getContext();
        
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
     *  @return (sub)content for the called class as array, otherwise {@code null}
     */
    public static String[] getContentSet() {
        return ResourceUtils.getContentSet(null);
    }

    /**
     *  Determines the context (sub)content for the called class as array.
     *  @param  name name of content
     *  @return (sub)content for the called class as array, otherwise {@code null}
     */
    public static String[] getContentSet(String name) {
        
        if (name == null)
            name = ResourceUtils.getContext().getMethodName();
        
        String content = ResourceUtils.getContentPlain();
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
    public static String getContent() {
        return ResourceUtils.getContent(-1);
    }
    
    /**
     *  Determines the context content for the called class and method name.
     *  @param  index optional index for sub content 
     *  @return content to the called class and method name,
     *          otherwise {@code null}
     */
    public static String getContent(int index) {
        return ResourceUtils.getContent(index, true);
    }
    
    /**
     *  Determines the context content for the called class and method name.
     *  @param  normalize converts line breaks into the system standard
     *  @return content to the called class and method name,
     *          otherwise {@code null}
     */
    public static String getContent(boolean normalize) {
        return ResourceUtils.getContent(-1, normalize);
    }
    
    /**
     *  Determines the context content for the called class and method name.
     *  @param  index     optional index for sub content 
     *  @param  normalize converts line breaks into the system standard
     *  @return content to the called class and method name,
     *          otherwise {@code null}
     */
    public static String getContent(int index, boolean normalize) {

        StackTraceElement stackTraceElement = ResourceUtils.getContext();
        return ResourceUtils.getContent(stackTraceElement.getMethodName(), index, normalize);
    }    
    
    /**
     *  Determines the context content for the called class and specified name.
     *  @param  name name of content
     *  @return content to the called class and specified name,
     *          otherwise {@code null}
     */
    public static String getContent(String name) {
        return ResourceUtils.getContent(name, -1);
    }
    
    /**
     *  Determines the context content for the called class and specified name.
     *  @param  name  name of content
     *  @param  index optional index for sub content 
     *  @return content to the called class and specified name,
     *          otherwise {@code null}
     */
    public static String getContent(String name, int index) {
        return ResourceUtils.getContent(name, index, true);
    }
    
    /**
     *  Determines the context content for the called class and specified name.
     *  @param  name      name of content
     *  @param  normalize converts line breaks into the system standard
     *  @return content to the called class and specified name,
     *          otherwise {@code null}
     */
    public static String getContent(String name, boolean normalize) {
        return ResourceUtils.getContent(name, -1, normalize);
    }
    
    /**
     *  Determines the context content for the called class and specified name.
     *  @param  name      name of content
     *  @param  index     optional index for sub content 
     *  @param  normalize converts line breaks into the system standard
     *  @return content to the called class and specified name,
     *          otherwise {@code null}
     */
    public static String getContent(String name, int index, boolean normalize) {

        if (index > 0)
            name += "_" + index;
        
        String content = ResourceUtils.getContentPlain();
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