/**
 *  LIZENZBEDINGUNGEN - Seanox Software Solutions ist ein Open-Source-Projekt,
 *  im Folgenden Seanox Software Solutions oder kurz Seanox genannt. Diese
 *  Software unterliegt der Version 2 der GNU General Public License.
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

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 *  Utilities for manipulation and direct object access.
 */
public class Accession {

    /** Constructor, creates a new Accession object. */
    private Accession() {
        return;
    }

    /**
     *  Gets a field of an object, even if it is not public or in a superclass.
     *  @param  object object
     *  @param  name   name of the field
     *  @return the determined field, otherwise {@code null}
     *  @throws SecurityException
     */
    public static Field getField(Object object, String name) {

        Class<?>       sheet;
        Field          field;
        List<Class<?>> list;

        if (name == null)
            return null;
        name = name.trim();
        if (name.isEmpty())
            return null;

        sheet = (object instanceof Class) ? (Class<?>)object : object.getClass();

        for (list = new ArrayList<>(); sheet != null; sheet = sheet.getSuperclass()) {

            if (list.contains(sheet))
                continue;
            list.add(sheet);
            
            try {field = sheet.getDeclaredField(name);
            } catch (NoSuchFieldException exception) {
                continue;
            }
            
            field.setAccessible(true);
            
            return field;
        }

        return null;
    }

    /**
     *  Gets the value of a field from an object, even if it is not public or
     *  in a superclass. Primitive data types are returned as a corresponding
     *  wrapper object.
     *  @param  object object
     *  @param  name   name of the field
     *  @return the value of the field, primitive data types are returned as a
     *          corresponding wrapper object
     *  @throws IllegalAccessException
     *      In the case of an access violation.
     *  @throws NoSuchFieldException
     *      If the field does not exist.
     */
    public static Object get(Object object, String name)
            throws IllegalAccessException, NoSuchFieldException {

        Field field;

        field = Accession.getField(object, name);

        if (field == null)
            throw new NoSuchFieldException();

        if (field.getType().equals(Boolean.TYPE))
            return Boolean.valueOf(field.getBoolean(object));
        else if (field.getType().equals(Byte.TYPE))
            return Byte.valueOf(field.getByte(object));
        else if (field.getType().equals(Character.TYPE))
            return Character.valueOf(field.getChar(object));
        else if (field.getType().equals(Double.TYPE))
            return Double.valueOf(field.getDouble(object));
        else if (field.getType().equals(Float.TYPE))
            return Float.valueOf(field.getFloat(object));
        else if (field.getType().equals(Integer.TYPE))
            return Integer.valueOf(field.getInt(object));
        else if (field.getType().equals(Long.TYPE))
            return Long.valueOf(field.getLong(object));
        else if (field.getType().equals(Short.TYPE))
            return Short.valueOf(field.getShort(object));

        else if (!field.getType().isPrimitive())
            return field.get(object);

        return null;
    }
    
    /**
     *  Ermittelt die angegebene Methode, auch nicht &ouml;ffentliche oder
     *  diese sich in einer verwendeten Superklasse befindet. R&uuml;ckgabe die
     *  angegebene Methode oder {@code null} wenn diese nicht ermittelt oder
     *  freigegeben werden kann.
     *  @param  object Bezugsobjekt
     *  @param  name   Name der Methode
     *  @param  types  Typenklassen der Argumente als Array
     *  @return die angegebene Methode, sonst {@code null}
     */
    public static Method getMethod(Object object, String name, Class<?>[] types) {

        Class<?> sheet;
        Method   method;

        //der Name wird bereinigt
        name  = (name == null) ? "" : name.trim();

        sheet = (object instanceof Class) ? (Class)object : object.getClass();

        for (method = null; method == null && sheet != null; sheet = sheet.getSuperclass()) {

            //die Methode wird ermittelt und freigegeben
            try {(method = sheet.getDeclaredMethod(name, types)).setAccessible(true);
            } catch (Exception exception) {

                //keine Fehlerbehandlung vorgesehen
            }
        }

        return method;
    }
    
    /**
     *  F&uuml;hrt die angegebene Methode ohne weitere Argumente am aus.
     *  R&uuml;ckgabe des R&uuml;ckgabewerts der Methode als Objekt. Einfache
     *  Datentypen werden als Wrapper seiner Art zur&uuml;ckgegeben.
     *  @param  object Bezugsobjekt
     *  @param  name   auszuf&uuml;hrende Methode
     *  @return der R&uuml;ckgabewert des Methodenaufrufs
     *  @throws IllegalAccessException
     *      Im Fall der Zugriffsverletzungen auf die Methode
     *  @throws InvocationTargetException
     *      Im Fall auftretende Exceptions beim Aufruf
     *  @throws NoSuchMethodException
     *      Im Fall, wenn die Methode nicht vorhanden ist
     */
    public static Object invoke(Object object, String name)
            throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        return Accession.invoke(object, name, null, null);
    }

    /**
     *  F&uuml;hrt die angegebene Methode mit den als Array von Objekten
     *  &uuml;bergebenen Argumenten am Objekt aus. Da die Objekte auch die Basis
     *  der &uuml;bergebenen Datentypen spezifizieren, kann diese Methode nicht
     *  f&uuml;r einfache Datentypen als Argumente verwendet werden.
     *  R&uuml;ckgabe des R&uuml;ckgabewerts der Methode als Objekt. Einfache
     *  Datentypen werden als Wrapper seiner Art zur&uuml;ckgegeben.
     *  @param  object    Bezugsobjekt
     *  @param  name      auszuf&uuml;hrende Methode
     *  @param  arguments Argumente als Array von Objekten
     *  @return der R&uuml;ckgabewert des Methodenaufrufs
     *  @throws IllegalAccessException
     *      Im Fall der Zugriffsverletzungen auf die Methode
     *  @throws InvocationTargetException
     *      Im Fall auftretende Exceptions beim Aufruf
     *  @throws NoSuchMethodException
     *      Im Fall, wenn die Methode nicht vorhanden ist
     */
    public static Object invoke(Object object, String name, Object[] arguments)
            throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {

        Class<?>[] array;
        Class<?>[] types;

        int        loop;

        //die Klassen der Argumente werden ermittelt
        for (loop = 0, types = new Class[0]; arguments != null && loop < arguments.length; loop++) {
            array = new Class[types.length +1];
            System.arraycopy(types, 0, array, 0, types.length);
            array[types.length] = (arguments[loop] == null) ? null : arguments[loop].getClass();
            types = array;
        }

        return Accession.invoke(object, name, types, arguments);
    }

    /**
     *  F&uuml;hrt die angegebene Methode mit den als Array von Datentypen und
     *  Objekten &uuml;bergebenen Argumenten am Objekt aus. Die Spezifierung
     *  einfacher Datentypen erfolgt dabei &uuml;ber den Typ des entsprechenden
     *  Wrappers (Bsp. int = Integer.Type). R&uuml;ckgabe des R&uuml;ckgabewerts
     *  der Methode als Objekt. Einfache Datentypen werden als Wrapper seiner
     *  Art zur&uuml;ckgegeben.
     *  @param  object    Bezugsobjekt
     *  @param  name      auszuf&uuml;hrende Methode
     *  @param  types     Datentypen als Array von Klassen
     *  @param  arguments Argumente als Array von Objekten
     *  @return der R&uuml;ckgabewert des Methodenaufrufs
     *  @throws IllegalAccessException
     *      Im Fall der Zugriffsverletzungen auf die Methode
     *  @throws InvocationTargetException
     *      Im Fall auftretende Exceptions beim Aufruf
     *  @throws NoSuchMethodException
     *      Im Fall, wenn die Methode nicht vorhanden ist
     */
    public static Object invoke(Object object, String name, Class<?>[] types, Object[] arguments)
            throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {

        Method method;

        //der Name wird bereinigt
        name = (name == null) ? "" : name.trim();

        //die Methode wird ermittelt und freigegeben
        method = Accession.getMethod(object, name, types);

        if (method == null)
            throw new NoSuchMethodException();

        //die Methode wird ausgefuehrt
        return method.invoke(object, arguments);
    }
}