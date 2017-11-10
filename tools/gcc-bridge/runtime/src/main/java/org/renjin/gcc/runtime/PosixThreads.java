/**
 * Renjin : JVM-based interpreter for the R language for the statistical analysis
 * Copyright © 2010-2016 BeDataDriven Groep B.V. and contributors
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, a copy is available at
 * https://www.gnu.org/licenses/gpl-2.0.txt
 */
package org.renjin.gcc.runtime;

import java.lang.invoke.MethodHandle;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Provides a mapping from the Posix Threads API (pthreads) to
 * the Java Concurrency APIs.
 */
public class PosixThreads {


  public static final int SUCCESS = 0;



  private static class PosixThread extends Thread {

    private MethodHandle startRoutine;
    private Ptr arg;
    private Ptr result;

    public PosixThread(MethodHandle startRoutine, Ptr arg) {
      this.startRoutine = startRoutine;
      this.arg = arg;
    }

    @Override
    public synchronized void start() {
      try {
        result = (Ptr) startRoutine.invoke(arg);
      } catch (Throwable throwable) {
        throwable.printStackTrace();
      }
    }
  }

  private static final AtomicInteger NEXT_THREAD_ID = new AtomicInteger(0);

  private static final ConcurrentMap<Integer, PosixThread> THREAD_MAP = new ConcurrentHashMap<>();

  public static int pthread_attr_init(Ptr attr) {
    return SUCCESS;
  }

  public static int pthread_attr_destroy(Ptr attr) {
    return SUCCESS;
  }

  public static int pthread_create(Ptr thread, Ptr attr, MethodHandle startRoutine, Ptr arg) {
    PosixThread posixThread = new PosixThread(startRoutine, arg);
    posixThread.start();

    int threadId = NEXT_THREAD_ID.incrementAndGet();

    THREAD_MAP.put(threadId, posixThread);

    thread.setInt(threadId);

    return SUCCESS;
  }

  public static int pthread_join(int threadId, Ptr valuePtr) {

    PosixThread posixThread = THREAD_MAP.get(threadId);

    try {
      posixThread.join();
    } catch (InterruptedException e) {
      e.printStackTrace();
    }

    valuePtr.setPointer(posixThread.result);

    return SUCCESS;
  }

  public static int pthread_mutex_init(Ptr mutex, Ptr attr) {
    return SUCCESS;
  }

  public static int pthread_mutexattr_init(Ptr attr) {
    return SUCCESS;
  }


  public static int pthread_mutexattr_gettype(Ptr attr, Ptr type) {
    throw new UnsupportedOperationException("TODO");
  }

  public static int pthread_mutexattr_settype(Ptr attr, int type) {
    return SUCCESS;
  }


  public static int pthread_mutex_lock(Ptr mutex) {
    return SUCCESS;
  }

  public static int pthread_mutex_trylock(Ptr mutex) {
    return SUCCESS;
  }

  public static int pthread_mutex_unlock(Ptr mutex) {
    return SUCCESS;
  }

  public static int pthread_mutex_destroy(Ptr mutex) {
    return SUCCESS;
  }

  public static int pthread_mutexattr_destroy(Ptr attr) {
    return SUCCESS;
  }

  public static int pthread_once(Ptr onceControl, MethodHandle initRoutine) throws Throwable {
    synchronized (onceControl.getArray()) {
      if(onceControl.getInt(0) == 0) {
        onceControl.setInt(0, 1);
        initRoutine.invoke();
      }
    }
    return SUCCESS;
  }
}
