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
package com.seanox.test;

import java.io.IOException;
import java.io.PrintStream;
import java.lang.annotation.Annotation;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.rules.ExternalResource;
import org.junit.rules.TestRule;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import com.seanox.test.utils.Accession;
import com.seanox.test.utils.Annotations;
import com.seanox.test.utils.OutputFacadeStream;

/**
 *  AbstractSuite is intended as a basis for implementing complex test
 *  environments.<br>
 *  The test environment is a hierarchical relation of individual tests bundled
 *  in test suites. Test suites can thus combine individual tests and other
 *  partial test suites.<br>
 *  <pre>
 *  Test Environment
 *    |
 *    + Suite 1
 *    .   |
 *    .   + Suite 1.1
 *    .   |   |
 *        |   + Test 1.1.1
 *        .   .
 *        .   .
 *        .   .
 *        |   + Test 1.1.n
 *        | 
 *        + Suite 1.n
 *        .   |
 *        .   + Test 1.n.1
 *        .   .
 *            .
 *            .
 *            + Test 1.n.n
 *  </pre> 
 *  In a good test environment the test can be started at any place.<br>
 *  This presupposes that each test can completely prepare, use and terminate
 *  the test environment.<br>
 *  <br>
 *  AbstractSuite should help here and simplify the implementation of the test
 *  hierarchy.<br>
 *  <br>
 *  The own (Abstract)Suite is the supreme and central static component of all
 *  tests and can use further abstraction layers and sub-suites.
 *  
 *  TODO:
 */
public abstract class AbstractSuite {
    
    /** internal index of executed test units */
    private static volatile List<String> trace;
    
    /** internal counter of executed test units */
    private static volatile int counter;

    /** interacting method for initiation */
    private static volatile Method initiate;
     
    /** interacting method for completion */
    private static volatile Method complete;
    
    /** internal shared system output stream */
    protected final static OutputFacadeStream outputStream = new OutputFacadeStream();

    /** original system output print stream */
    private static volatile PrintStream systemOutputStream;

    /** internal shared error output stream */
    protected final static OutputFacadeStream errorStream = new OutputFacadeStream();

    /** original system error print stream */
    private static volatile PrintStream systemErrorStream;    
    
    /** bootstrap for initiate and complete the suite */
    @ClassRule
    public static final ExternalResource suiteBootstrap = new ExternalResource() {
        
        Class<?> source;
        
        @Override
        public Statement apply(Statement statement, Description description) {
            
            if (this.source == null)
                this.source = description.getTestClass();
            return super.apply(statement, description);
        }
        
        @Override
        protected void before() throws Throwable {
            
            if (++AbstractSuite.counter > 1)
                return;
            AbstractSuite.initiateSuite(Accession.getClassHerachie(this.source));
        }
        
        @Override
        protected void after() {
            
            if (--AbstractSuite.counter > 0)
                return;
            AbstractSuite.completeSuite();
        }
    };
    
    /**
     *  TestWatcher to execute onBefore and onAfter methods before and after of
     *  a test unit. These methods can be used to prepare and finalize
     *  severally test units. 
     */
    @Rule
    public final TestRule suiteTestWatcher = new TestWatcher() {

        @Override
        protected void starting(Description description) {
            
            Class<?> source = description.getTestClass();
            try {AbstractSuite.trace(source, source.getDeclaredMethod(description.getMethodName()));
            } catch (NoSuchMethodException | SecurityException exception) {
                throw new RuntimeException(exception);
            }

            String methodName;
            methodName = description.getMethodName();
            methodName = Character.toUpperCase(methodName.charAt(0)) + methodName.substring(1);
            methodName = "onBefore" + methodName;
            try {
                Method method = AbstractSuite.this.getClass().getDeclaredMethod(methodName);
                method.setAccessible(true);
                method.invoke(AbstractSuite.this);
            } catch (NoSuchMethodException exception) {
            } catch (Exception exception) {
                throw new RuntimeException(exception);
            }
        }
        
        @Override
        protected void finished(Description description) {
            
            String methodName;
            methodName = description.getMethodName();
            methodName = Character.toUpperCase(methodName.charAt(0)) + methodName.substring(1);
            methodName = "onAfter" + methodName;
            try {
                Method method = AbstractSuite.this.getClass().getDeclaredMethod(methodName);
                method.setAccessible(true);
                method.invoke(AbstractSuite.this);
            } catch (NoSuchMethodException exception) {
            } catch (Exception exception) {
                throw new RuntimeException(exception);
            } 
        }
    };    
    
    private static Method locateInteract(Class<? extends Annotation> annotation, Class<?>[] classes) {
        
        for (Class<?> source : classes) {
            if (AbstractSuite.class.equals(source))
                return null;
            if (!AbstractSuite.class.isAssignableFrom(source))
                continue;
            Method[] methods = Annotations.findMethods(source, annotation);
            if (methods == null
                    || methods.length <= 0)
                continue;
            return methods[0];
        }
        return null;
    }
    
    private static void initiateSuite(Class<?>[] herachie) throws Throwable {
        
        AbstractSuite.trace = new ArrayList<>();

        AbstractSuite.initiate = AbstractSuite.locateInteract(Initiate.class, herachie);
        AbstractSuite.complete = AbstractSuite.locateInteract(Complete.class, herachie);
        
        AbstractSuite.systemOutputStream = System.out;
        AbstractSuite.outputStream.mount(AbstractSuite.systemOutputStream);
        System.setOut(new PrintStream(AbstractSuite.outputStream));

        AbstractSuite.systemErrorStream = System.err;
        AbstractSuite.errorStream.mount(AbstractSuite.systemErrorStream);
        System.setErr(new PrintStream(AbstractSuite.errorStream));
        
        if (AbstractSuite.initiate != null) {
            AbstractSuite.initiate.setAccessible(true);
            try {AbstractSuite.initiate.invoke(null);
            } catch (Throwable throwable) {
                if (throwable instanceof InvocationTargetException)
                    throwable = ((InvocationTargetException)throwable).getTargetException();
                throw throwable;
            }
        }
    }

    private static void completeSuite() {

        try {
            if (AbstractSuite.complete != null) {
                AbstractSuite.complete.setAccessible(true);
                try {AbstractSuite.complete.invoke(null);
                } catch (Throwable throwable) {
                    if (throwable instanceof InvocationTargetException)
                        throwable = ((InvocationTargetException)throwable).getTargetException();
                    throw new RuntimeException(throwable);
                }
            }         
        } finally {
            System.setOut(AbstractSuite.systemOutputStream);
            System.setErr(AbstractSuite.systemErrorStream);
            try {AbstractSuite.outputStream.close();
            } catch (IOException exception) {
            }
            try {AbstractSuite.errorStream.close();
            } catch (IOException exception) {
            }
        }
    }

    /**
     *  Writes a trace information to the system output stream.
     *  @param source
     */
    protected static void trace(Class<?> source) {
        AbstractSuite.trace(source, null);
    }

    /**
     *  Writes a trace information to the system output stream.
     *  @param source
     *  @param method
     */
    protected static void trace(Class<?> source, Method method) {
        
        if (!AbstractSuite.trace.contains(source.getName())) {
            System.out.println("[" + source.getName() + "]");
            AbstractSuite.trace.add(source.getName());
        }
        if (method != null)
            System.out.println("[" + source.getName() + "] -> " + method.getName());
    }
    
    //TODO:
    @Documented
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    protected static @interface Initiate {
    }

    //TODO:
    @Documented
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    protected static @interface Complete {
    }
}