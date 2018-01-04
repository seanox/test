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

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
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
 *  environments.
 *  
 *  <h3>General</h3>
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
 *  tests and can use further abstraction layers and sub-suites.<br>
 *  <pre>
 *  AbstractSuite
 *    |
 *    + AbstractSubSuite (Layer 1)
 *    .   |
 *    .   + AbstractSubSuite (Layer n)
 *    .   |   |
 *        |   + Test 1
 *        .   .
 *        .   .
 *        .   .
 *        |   + Test n
 *        |
 *        + AbstractSubSuite (Layer n +1)
 *        .   |
 *        .   + Test 1
 *        .   .
 *            .
 *            .
 *            + Test n
 *  </pre> 
 *  
 *  <h3>What does AbstractSuite do?</h3>
 *  AbstractSuite takes care of providing the test environment no matter where
 *  the test is started.<br>
 *  The mostly static architecture of JUnit provides various possibilities for
 *  preparation and finalization. However, it is difficult to centralize and
 *  generalize them.<br>
 *  AbstractSuite helps with additional interactors (like events).<br>
 *  It is possible to annotate central methods and sequences that are executed
 *  with start and end of the test environment, start and end of test classes,
 *  or executed before and after the execution of tests.<br>
 *  Additional central I/O interfaces (e.g. {@link System.out} and
 *  {@link System.err}) are redirected so that they can be better included in
 *  the tests.
 *  
 *  <h3>What do I have to do?</h3>
 *  A test environment with AbstractSuite is based on hierarchical (sub)suites
 *  and tests.<br>
 *  Even if it is a static construction, it is important that all components
 *  inherit according to this hierarchy. Thus, the test environment knows which
 *  prerequisites are required for the execution of a test. This allows you to
 *  start the test at any point in the test environment.
 *  
 *  <h3>Interactors (Sequence)</h3>
 *  {@link Initiate}: Called before the first test and initializes the test
 *  environment. The corresponding method is annotated. In the hierarchy,
 *  multiple methods can be annotated, always the most qualified (nearest)
 *  method is used.<br> 
 *  <br>
 *  {@link BeforeClass}: The original JUnit annotation annotates methods that
 *  are called before or when a test class is initiated. In the hierarchy,
 *  multiple methods can be annotated, always the most qualified (nearest)
 *  method is used.<br> 
 *  <br>
 *  {@link BeforeTest}: This annotation is used in conjunction with the JUinit
 *  annotation {@link Test}. It defines a sequence of methods that are executed
 *  before a test.<br> 
 *  <br>
 *  {@link AfterTest}: This annotation is used in conjunction with the JUinit
 *  annotation {@link Test}. It defines a sequence of methods that are executed
 *  after a test.<br> 
 *  <br>
 *  {@link AfterClass}: The original JUnit annotation annotates methods that
 *  are called after or when a test class is terminated. In the hierarchy,
 *  multiple methods can be annotated, always the most qualified (nearest)
 *  method is used.<br> 
 *  <br>
 *  {@link Terminates}: Called after the last test and terminates the test
 *  environment. The corresponding method is annotated. In the hierarchy,
 *  multiple methods can be annotated, always the most qualified (nearest)
 *  method is used.<br> 
 *  <br>
 *  AbstractSuite 1.0 20171212<br>
 *  Copyright (C) 2017 Seanox Software Solutions<br>
 *  All rights reserved.
 *
 *  @author  Seanox Software Solutions
 *  @version 1.0 20171212
 */
public abstract class AbstractSuite {
    
    /** internal index of executed test units */
    private static volatile List<String> trace;
    
    /** internal counter of executed test units */
    private static volatile int counter;

    /** interacting method for initiation */
    private static volatile Method initiate;
     
    /** interacting method for termination */
    private static volatile Method terminate;
    
    /** internal shared system output stream */
    protected final static OutputFacadeStream outputStream = new OutputFacadeStream();

    /** original system output print stream */
    private static volatile PrintStream systemOutputStream;

    /** internal shared error output stream */
    protected final static OutputFacadeStream errorStream = new OutputFacadeStream();

    /** original system error print stream */
    private static volatile PrintStream systemErrorStream;    
    
    /** bootstrap for initiate and terminate the suite */
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
            AbstractSuite.terminateSuite();
        }
    };
    
    /** rule to execute interactors {@link BeforeTest} and {@link AfterTest} */
    @Rule
    public final TestRule suiteTestWatcher = new TestWatcher() {

        private void initiate(Description description, Class<? extends Annotation> type) {
            
            Class<?> source;
            Method   method;
            
            source = description.getTestClass();
            try {AbstractSuite.trace(source, source.getDeclaredMethod(description.getMethodName()));
            } catch (NoSuchMethodException | SecurityException exception) {
                throw new RuntimeException(exception);
            }
            
            try {
                source = AbstractSuite.this.getClass();
                method = source.getDeclaredMethod(description.getMethodName());
                for (Annotation annotation : method.getAnnotationsByType(type)) {
                    if (annotation instanceof BeforeTest)
                        for (String name : ((BeforeTest)annotation).value())
                            Accession.invoke(AbstractSuite.this, name);
                    if (annotation instanceof AfterTest)
                        for (String name : ((AfterTest)annotation).value())
                            Accession.invoke(AbstractSuite.this, name);
                }
            } catch (NoSuchMethodException exception) {
            } catch (Exception exception) {
                throw new RuntimeException(exception);
            }
        }
        
        @Override
        protected void starting(Description description) {
            this.initiate(description, BeforeTest.class);
        }
        
        @Override
        protected void finished(Description description) {
            this.initiate(description, AfterTest.class);
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
        AbstractSuite.terminate = AbstractSuite.locateInteract(Terminate.class, herachie);
        
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

    private static void terminateSuite() {

        try {
            if (AbstractSuite.terminate != null) {
                AbstractSuite.terminate.setAccessible(true);
                try {AbstractSuite.terminate.invoke(null);
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
    
    /**
     *  Annotates a method, which is called before the first test is execuded
     *  and initializes the test environment. The corresponding method is
     *  annotated. In the hierarchy, multiple methods can be annotated, always
     *  the most qualified (nearest) method is used.
     */
    @Documented
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    protected static @interface Initiate {
    }

    /**
     *  Annotates a method, which is called after the last test has been
     *  executed and initializes the test environment. The corresponding method
     *  is annotated. In the hierarchy, multiple methods can be annotated,
     *  always the most qualified (nearest) method is used.
     */
    @Documented
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    protected static @interface Terminate {
    }
    
    /**
     *  Annotates a method, which is called before a test is execuded and
     *  prepares the test environment. This annotation supports sequences,
     *  meaning that a sequence of methods can be defined here, which are
     *  called one after the other.
     */
    @Documented
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    protected static @interface BeforeTest {
        String[] value();
    }

    /**
     *  Annotates a method, which is called after a test has been execuded
     *  and restores the test environment. This annotation supports sequences,
     *  meaning that a sequence of methods can be defined here, which are
     *  called one after the other.
     */
    @Documented
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    protected static @interface AfterTest {
        String[] value();
    }    
}