package org.renjin.gnur.api;

import org.renjin.gcc.annotations.VarArgs;
import org.renjin.gcc.runtime.Ptr;

import java.util.concurrent.TimeUnit;

/**
 * Provides limited emulation of Linux system calls
 */
public class Linux {

  public static final int O_RDONLY = 0;
  public static final int O_WRONLY = 1;
  public static final int O_RDWR = 2;

  public static int open(Ptr pathname, int flags, int mode) { throw new UnsupportedOperationException(); }

  public static Ptr fdopen(int fd, Ptr mode) { throw new UnsupportedOperationException(); }

  public static int close(int fd) { throw new UnsupportedOperationException(); }

  public static int read(int fd, Ptr buf, int count) { throw new UnsupportedOperationException(); }

  public static int write(int fd, Ptr buf, int count) { throw new UnsupportedOperationException(); }

  public static int fstat(int fd, Ptr buf) {
    throw new UnsupportedOperationException();
  }

  public static void waitpid(int pid, Ptr status, int options) { throw new UnsupportedOperationException(); }

  public static int usleep(int microseconds) {
    try {
      Thread.sleep(TimeUnit.MICROSECONDS.toMillis(microseconds));
      return 0;

    } catch (InterruptedException e) {
      // ERRINT: 3407;
      return -1;
    } catch (Exception e) {
      return -1;
    }
  }

  public static int pipe(Ptr fds) { throw new UnsupportedOperationException(); }

  public static int fork() { throw new UnsupportedOperationException(); }

  public static int setpgid(int pid, int pgid) { throw new UnsupportedOperationException(); }

  public static int prctl(int option, long arg2, long arg3,
                          long arg4, long arg5) { throw new UnsupportedOperationException(); }

  /**
   * The C library function void (*signal(int sig, void (*func)(int)))(int) sets a function
   * to handle signal i.e. a signal handler with signal number sig.
   */
  public static int signal(int sig, Ptr handler) { throw new UnsupportedOperationException(); }


  public static long sysconf(int name) { throw new UnsupportedOperationException(); }
  public static int fcntl(int fd, int cmd, @VarArgs Ptr varArgs) { throw new UnsupportedOperationException(); }
  public static int execvp(Ptr file, Ptr argv) { throw new UnsupportedOperationException(); }
  public static int raise(int sig) { throw new UnsupportedOperationException(); }
  public static int gettimeofday(Ptr tv, Ptr tz) { throw new UnsupportedOperationException(); }
  public static int kill(int pid, int sig) { throw new UnsupportedOperationException(); }

  /**
   * Returns a string describing the signal
   * number passed in the argument sig.  The string can be used only until
   * the next call to strsignal().
   */
  public static Ptr strsignal(int sig) { throw new UnsupportedOperationException(); }

  public static int poll(Ptr fds, int nfds, int timeout) { throw new UnsupportedOperationException(); }

  public static int dup(int oldfd) {
    throw new UnsupportedOperationException();
  }

  public static int dup2(int oldfd, int newfd) { throw new UnsupportedOperationException(); }

  public static int dup3(int oldfd, int newfd, int flags) {
    throw new UnsupportedOperationException();
  }

  public static int sigemptyset(Ptr set) { throw new UnsupportedOperationException(); }

  public static int sigaddset(Ptr set, int signum) { throw new UnsupportedOperationException(); }

  public static int sigdelset(Ptr set, int signum) {
    throw new UnsupportedOperationException();
  }

  public static void sigprocmask() { throw new UnsupportedOperationException(); }

  public static Ptr mmap(Ptr addr, int length, int prot, int flags, int fd, int offset) {
    throw new UnsupportedOperationException("TODO");
  }
  public static int munmap(Ptr addr, int length) {
    throw new UnsupportedOperationException("TODO");
  }

  public int access(Ptr pathname, int mode) {
    throw new UnsupportedOperationException("TODO");
  }

  public static int faccessat(int dirfd, Ptr pathname, int mode, int flags) {
    throw new UnsupportedOperationException("TODO");
  }
}
