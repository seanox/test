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
import java.util.List;

import com.seanox.test.utils.Executor.Worker.Filter;

/** 
 *  The executor provides control and monitoring to perform asynchronous tasks.
 *  The tasks are implemented as {@link Executor.Worker}.
 *  As a worker, single and multiple instances are supported.<br>
 *      <dir><b>Example for a single instance of an anonymous class</b></dir>
 *  <pre>
 *  Executor executor = Executor.create(50, new Worker() {
 *      protected void execute() {
 *          ...
 *      }
 *  });
 *  executor.execute();
 *  </pre>
 *      <dir><b>Example for a single instance with monitoring and timeout</b></dir>
 *  <pre>
 *  Executor executor = Executor.create(50, new Worker() {
 *      protected void execute() {
 *          ...
 *      }
 *  });
 *  executor.execute();
 *  executor.await(60000);
 *  </pre>
 *      <dir><b>Example for a multible instance with monitoring and timeout</b></dir>
 *  <pre>
 *  Executor executor = Executor.create(50, ExampleWorker.class);
 *  executor.execute();
 *  executor.await(60000);
 *  
 *  public class ExampleWorker extends Worker {
 *      public void prepare() {
 *          ...
 *      }
 *      public void execute() {
 *          ...
 *      }
 *  }
 *  </pre>
 */
public class Executor {

    /** List of established worker threads */
    private volatile List<Thread> threads;
    
    /** List of established workers */
    private volatile List<Worker> workers;

    /** Current status of executor */
    private volatile int status;

    /** Counter of completed threads/worksers */
    private volatile int count;

    /** Constants for status STARTED */
    private static final int STARTED = 1;
    
    /** Constants for status TERMINATED */
    private static final int TERMINATED = 2;
    
    /** Constants for status INTERRUPTED */
    private static final int INTERRUPTED = 4;
    
    /** Constructor, creates a new Executor object. */
    private Executor() {
        
        this.threads = new ArrayList<>();
        this.workers = new ArrayList<>();
    }
    
    /**
     *  Creates a new Executor with a specific number of workers.
     *  The created set of workers is ready to execute.
     *  @param  size   number of workers
     *  @param  worker worker implementation
     *  @return the created Executor with a set of workers is ready to execute
     */
    public static Executor create(int size, Worker worker) {

        if (size <= 0)
            throw new IllegalArgumentException();
        if (worker == null)
            throw new IllegalArgumentException();
        
        Executor executor = new Executor();
        for (int loop = 0; loop < size; loop++) {
            Thread thread = new Thread() {
                @Override
                public void interrupt() {
                    super.interrupt();
                    worker.interrupt();
                }
                @Override
                public void run() {
                    try {
                        while (executor.status < 1)
                            try {Thread.sleep(25);
                            } catch (InterruptedException exception) {
                                executor.interrupt();
                                return;
                            }
                        if (executor.status == 1)
                            try {worker.execute();
                            } catch (Throwable throwable) {
                                worker.throwable = throwable;
                            }
                    } finally {
                        executor.count++;
                    }
                }
            };
            executor.threads.add(thread);
            thread.start();
        }
        return executor;
    }
    
    /**
     *  Creates a new Executor with a specific number of workers.
     *  The created set of workers is ready to execute.
     *  @param  size   number of workers
     *  @param  worker worker class
     *  @return the created Executor with a set of workers is ready to execute
     *  @throws Exception
     *      If errors occur during the initialization of the workers.
     */
    public static Executor create(int size, Class<? extends Worker> worker)
            throws Exception {

        if (size <= 0)
            throw new IllegalArgumentException();
        if (worker == null)
            throw new IllegalArgumentException();

        Executor executor = new Executor();
        for (int loop = 0; loop < size; loop++) {
            Worker instance = worker.newInstance();
            instance.prepare();
            Thread thread = new Thread() {
                @Override
                public void interrupt() {
                    super.interrupt();
                    instance.interrupt();
                }
                @Override
                public void run() {
                    try {
                        while (executor.status < 1)
                            try {Thread.sleep(25);
                            } catch (InterruptedException exception) {
                                executor.interrupt();
                                return;
                            }
                        if (executor.status == 1)
                            try {instance.execute();
                            } catch (Throwable throwable) {
                                instance.throwable = throwable;
                            }
                    } finally {
                        executor.count++;
                    }
                }
            };
            executor.threads.add(thread);
            thread.start();
        }
        return executor;
    }
    
    /**
     *  Starts all workers.
     *  The method does not block because the workers are working
     *  asynchronously. For monitoring, the methods {@link #await()} and
     *  {@link #await(long)} are used.
     */
    public void execute() {
        
        synchronized (this) {
            if ((this.status & STARTED) != 0)
                throw new IllegalStateException();
            this.status |= STARTED;
        }
    }
    
    /**
     *  Tests whether this executor has been executed.
     *  @return {@code true} if this executor has been executed
     */
    public boolean isExecuted() {
        return (this.status & STARTED) != 0;
    } 
    
    /** Interrupts this executor and all established workers. */
    public void interrupt() {
        
        synchronized (this) {
            if ((this.status & STARTED) == 0
                    || (this.status & INTERRUPTED) != 0)
                throw new IllegalStateException();
            this.status |= INTERRUPTED;
        }        
    }
    
    /**
     *  Tests whether this executor has been interrupted.
     *  @return {@code true} if this executor has been interrupted
     */
    public boolean isInterrupted() {
        return (this.status & INTERRUPTED) != 0;
    }
    
    /**
     *  Waits until all workers are finished.
     *  @return {@code true} all workers are finished and not interrupted.
     *  @throws InterruptedException
     *      When the threads have been interrupted.
     */
    public boolean await()
            throws InterruptedException {
        return this.await(-1);
    }
    
    /**
     *  Waits until all workers are finished Or the timeout has been reached.
     *  @param  timeout timeout in milliseconds 
     *  @return {@code true} all workers are finished and not interrupted.
     *  @throws InterruptedException
     *      When the threads have been interrupted.
     */    
    public boolean await(long timeout)
            throws InterruptedException {
        
        long timing = System.currentTimeMillis();
        while (this.count < this.threads.size()
                && (timeout < 0 || System.currentTimeMillis() -timing < timeout)
                && (this.status & INTERRUPTED) == 0)
            Thread.sleep(25);
        
        if ((this.status & INTERRUPTED) == 0
                && (timeout < 0 || System.currentTimeMillis() -timing < timeout))
            return true;
        
        for (Thread thread : this.threads)
                thread.interrupt();
        
        return false;
    }
    
    /**
     *  Tests whether this executor has been terminated.
     *  @return {@code true} if this executor has been terminated
     */
    public boolean isTerminated() {
        return (this.status & TERMINATED) != 0;
    }
    
    /**
     *  Tests whether this executor has been failed.
     *  @return {@code true} if this executor has been failed
     */
    public boolean isFailed() {
        return this.getWorkers(Worker.Filter.FAILED).length > 0;
    }
    
    /**
     *  Gets all established workers.
     *  @return all established workers 
     */
    public Worker[] getWorkers() {
        return this.getWorkers(Worker.Filter.ALL);
    }

    /**
     *  Gets all established workers to the specified filters.
     *  Without a filter, gets all workers.
     *  @param  filters {@link Executor.Worker.Filter}
     *  @return all established workers 
     */
    public Worker[] getWorkers(Worker.Filter... filters) {
        
        if (filters == null
                || filters.length == 0)
            filters = new Worker.Filter[] {Worker.Filter.ALL};

        List<Filter> matcher = Arrays.asList(filters);
        List<Worker> workers = new ArrayList<>();
        for (Worker worker : this.workers)
            if (matcher.contains(Worker.Filter.ALL)
                    || (matcher.contains(Worker.Filter.FAILED)
                            && worker.throwable != null)
                    || (matcher.contains(Worker.Filter.INTERRUPTED)
                            && worker.isInterrupted())
                    || (matcher.contains(Worker.Filter.STARTED)
                            && worker.execute)
                    || (matcher.contains(Worker.Filter.TERMINATED)
                            && worker.terminate))
                workers.add(worker);
        
        return workers.toArray(new Worker[0]);
    }
    
    /**
     *  Internal class for a worker.
     *  A worker creates a set of requests, performs them and collects the
     *  responses.
     */
    public abstract static class Worker {
        
        /** possibly occurring error */
        private Throwable throwable;
        
        /** indicator for: was executed */
        private boolean execute;
        
        /** indicator for: was interrupted */
        private boolean interrupt;
        
        /** indicator for: was terminated */
        private boolean terminate;

        /**
         *  Constructor, creates a new worker object.
         *  @param  group
         *  @throws Exception
         */
        protected Worker() {
            return;
        }

        /**
         *  Implement the preparing for the execution of the worker.
         *  @throws Exception
         *        If errors occur during the execution of the workers.
         */
        protected void prepare()
                throws Exception {
            return;
        }

        /**
         *  Implement the task of the worker.
         *  @throws Exception
         *        If errors occur during the execution of the workers.
         */
        protected abstract void execute()
                throws Exception;
        
        /**
         *  Tests whether this worker has been executed.
         *  @return {@code true} if this worker has been executed
         */        
        public boolean isExecuted() {
            return this.execute;
        }        

        /** Interrupts this workers. */
        protected void interrupt() {
            this.interrupt = true;
        }
        
        /**
         *  Tests whether this worker has been interrupted.
         *  @return {@code true} if this worker has been interrupted
         */            
        public boolean isInterrupted() {
            return this.interrupt;
        }
        
        /**
         *  Tests whether this worker has been terminated.
         *  @return {@code true} if this worker has been terminated
         */            
        public boolean isTerminated() {
            return this.terminate;
        }
        
        /**
         *  Tests whether this worker has been faild.
         *  @return {@code true} if this worker has been faild
         */            
        public boolean isFailed() {
            return this.throwable != null;
        }
        
        /** Enum with filters */
        public static enum Filter {
            
            /** Gets all Workers */
            ALL,
            
            /** Gets all started Workers */
            STARTED,
            
            /** Gets all termindated Workers */
            TERMINATED,
            
            /** Gets all interrupted Workers */
            INTERRUPTED,
            
            /** Gets all failed Workers */
            FAILED;
        }
    }
}