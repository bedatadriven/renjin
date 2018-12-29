/*
 * Renjin : JVM-based interpreter for the R language for the statistical analysis
 * Copyright © 2010-2018 BeDataDriven Groep B.V. and contributors
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
package org.renjin.primitives;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.provider.local.LocalFile;
import org.renjin.RVersion;
import org.renjin.RenjinVersion;
import org.renjin.eval.Context;
import org.renjin.eval.EvalException;
import org.renjin.invoke.annotations.*;
import org.renjin.repackaged.guava.annotations.VisibleForTesting;
import org.renjin.repackaged.guava.collect.Lists;
import org.renjin.sexp.*;

import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.lang.management.ThreadMXBean;
import java.net.InetAddress;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class System {

  private static final double MILLISECONDS_PER_SECOND = 1000d;
  

  @Internal("R.home")
  public static String getRHome(@Current Context context) throws URISyntaxException {
    return context.getSession().getHomeDirectory();
  }

  @Internal
  public static ListVector Version() {
    // this is just copied from my local R installation
    // we'll have to see later what makes the most sense to put here,
    // whether we need to pretend to be some version of R
    return ListVector.newNamedBuilder()
        .add("platform", "i386-pc-mingw32")
        .add("arch", "i386")
        .add("os", "mingw32")
        .add("system", "i386, mingw32")
        .add("status", "")
        .add("major", RVersion.MAJOR)
        .add("minor", RVersion.MINOR)
        .add("year", "2009")
        .add("month", "12")
        .add("day", "14")
        .add("engine", "Renjin")
        .add("language", "R")
        .add("svn rev", "50720")
        .add("version.string", "Renjin version " + RenjinVersion.getVersionName())
        .build();

  }

  @Internal("Sys.getenv")
  public static StringVector getEnvironment(@Current Context context, StringVector names, String unset) {
    StringVector.Builder result = new StringArrayVector.Builder();

    Map<String, String> map = context.getSession().getSystemEnvironment();
    if(names.length() == 0) {
      for(Map.Entry<String,String> entry : map.entrySet()) {
        result.add(entry.getKey() + "=" + entry.getValue());
      }
    } else {
      for(String name : names) {
        String value = map.get(name);
        result.add(value == null ? unset : value);
      }
    }
    return result.build();
  }

  @Internal("Sys.setenv")
  public static LogicalVector setEnvironment(@Current Context context, StringVector names, StringVector values) {

    Map<String, String> map = context.getSession().getSystemEnvironment();

    LogicalArrayVector.Builder result = new LogicalArrayVector.Builder();
    for(int i=0;i!=names.length();++i) {
      map.put(names.getElementAsString(i), values.getElementAsString(i));
      result.add(true);
    }
    return result.build();
  }

  @Invisible
  @Internal("Sys.unsetenv")
  public static LogicalVector unsetEnvironment(@Current Context context, StringVector names) {

    Map<String, String> map = context.getSession().getSystemEnvironment();

    LogicalArrayVector.Builder result = new LogicalArrayVector.Builder();
    for(int i=0;i!=names.length();++i) {
      map.remove(names.getElementAsString(i));
      result.add(true);
    }
    return result.build();
  }

  private enum LocaleCategory {
    LC_COLLATE,
    LC_MONETARY,
    LC_NUMERIC,
    LC_TIME,
    LC_MESSAGES,
    LC_PAPER,
    LC_MEASUREMENT;

    String value() {
      return "English_United States.1252";
    }
  }

  private static final int LC_ALL = 1;

  @Internal("Sys.getlocale")
  public static String getLocale(int categoryIndex) {
    if(categoryIndex == LC_ALL) {
      StringBuilder info = new StringBuilder();
      boolean needsSemi = false;
      for(LocaleCategory category : LocaleCategory.values()) {
        if(needsSemi) {
          info.append(';');
        } else {
          needsSemi = true;
        }
        info.append(category.name()).append('=').append(category.value());
      }
      return info.toString();
    } else {
      return LocaleCategory.values()[categoryIndex-2].value();
    }
  }


  @Internal("Sys.setlocale")
  public static String setLocale(int categoryIndex, String locale) {
    java.lang .System.out.println("locale = " + locale);
    return "";
  }
  

  /**
   * A character vector with 18 named components.
   * 
   * The results in the C locale are
   * 
   * <pre>
   * ##    decimal_point     thousands_sep          grouping   int_curr_symbol
   * ##              "."                ""                ""                ""
   * ##  currency_symbol mon_decimal_point mon_thousands_sep      mon_grouping
   * ##               ""                ""                ""                ""
   * ##    positive_sign     negative_sign   int_frac_digits       frac_digits
   * ##               ""                ""             "127"             "127"
   * ##    p_cs_precedes    p_sep_by_space     n_cs_precedes    n_sep_by_space
   * ##            "127"             "127"             "127"             "127"
   * ##      p_sign_posn       n_sign_posn
   * ##            "127"             "127"
   * </pre>
   * 
   * @return It is possible to compile R without support for locales, in which case the value will be <code>NULL</code>.
   */
  @Internal("Sys.localeconv")
  public static Vector getLocaleConventions() {
    return Null.INSTANCE;
  }

  @Internal
  public static StringVector commandArgs(@Current Context context) {
    return context.getSession().getCommandLineArguments();
  }

  /**
   * Report on the optional features which have been compiled into this build of R.
   *
   * @param what
   * @return
   */
  @Internal
  public static LogicalVector capabilities(@Current Context context, StringVector what) {
    LogicalArrayVector.Builder result = new LogicalArrayVector.Builder();
    StringVector.Builder names = new StringVector.Builder();

    for(String capabilityName : what) {
      Capability.forName(capabilityName).ifPresent(capability -> {
        names.add(capability.getCapabilityName());
        result.add(capability.evaluate(context.getSession()));
      });
    }
    result.setAttribute(Symbols.NAMES, names.build());
    return result.build();
  }

  @Internal
  public static LogicalVector capabilities(@Current Context context) {

    LogicalArrayVector.Builder result = new LogicalArrayVector.Builder();
    StringVector.Builder names = new StringVector.Builder();

    for(Capability capability : Capability.values()) {
      names.add(capability.getCapabilityName());
      result.add(capability.evaluate(context.getSession()));
    }
    result.setAttribute(Symbols.NAMES, names.build());
    return result.build();
  }

  @Internal
  public static StringVector date() {
    // R Style Date Format
    // Example in R: Fri Sep  9 12:20:00 2011 
    // Example in Renjin: Fri Sep 09 12:20:00 2011 
    SimpleDateFormat sdf = new SimpleDateFormat("EEE MMM dd HH:mm:ss yyyy");
    StringVector.Builder b = new StringVector.Builder();
    Date d = new Date();
    String parsed = null;
    parsed = sdf.format(d);
    b.add(parsed);
    return (b.build());
  }
  
  @Internal("Sys.info")
  public static StringVector sysInfo() {
    StringVector.Builder sb = new StringVector.Builder();
    sb.add(java.lang.System.getProperty("os.name"));
    sb.add(java.lang.System.getProperty("os.version"));
    /*
     * os.build does not exist! maybe we can put jvm info instead?
     * 
     */
    sb.add(java.lang.System.getProperty("os.build"));
    try{ 
      sb.add(InetAddress.getLocalHost().getHostName());
    }catch(Exception e){
      sb.add("Can not get hostname");
    }
    sb.add(java.lang.System.getProperty("os.arch"));
    /*
     * 
     * login.name does not exist!
     */
    sb.add(java.lang.System.getProperty("login.name"));
    sb.add(java.lang.System.getProperty("user.name"));

    sb.setAttribute("names", new StringArrayVector("sysname", "release", "version", "nodename", "machine", "login", "user"));
    return (sb.build());
  }
  
  @Internal("Sys.getpid")
  public static int SysGetPid(){
    String name;
    try {
      name = java.lang.management.ManagementFactory.getRuntimeMXBean().getName();
    } catch (Exception e) {
      throw new EvalException("Can not catch the pid.", e);
    }
    int atIndex = name.indexOf("@");
    int result =  1;
    try{
      result = Integer.parseInt(name.substring(0,atIndex));
    } catch (Exception e) {
      //Handled string wrong?
    }
    return result;
  }
  
  @Internal("Sys.sleep")
  public static void SysSleep(double seconds){
    try{
      Thread.currentThread().sleep((long)(seconds * 1000));
    }catch (InterruptedException ie){
      throw new EvalException("Sys.sleep interrupted");
    }
  }
  
  /**
   * Returns object of class ‘"proc_time"’ which is a numeric vector of
   * length 5, containing the user, system, and total elapsed times for
   * the currently running R process, and the cumulative sum of user
   * and system times of any child processes spawned by it on which it
   * has waited. 
   *
   * _The ‘user time’ is the CPU time charged for the execution of user
   *  instructions of the calling process. The ‘system time’ is the CPU
   *  time charged for execution by the system on behalf of the calling
   *  process._
   */
  @Builtin("proc.time")
  public static DoubleVector procTime() {

    DoubleArrayVector.Builder result = new DoubleArrayVector.Builder();
    StringVector.Builder names = new StringVector.Builder();


    long totalCPUTime;
    long userCPUTime;
    long elapsedTime;

    // There doesn't seem to be any platform-independent way of accessing
    // CPU use for the whole JVM process, so we'll have to make do
    // with the timings for the thread we're running on.
    //
    // Additionally, the MX Beans may not be available in all environments,
    // so we need to fallback to app
    try {
      ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
      userCPUTime = TimeUnit.NANOSECONDS.toMillis(threadMXBean.getCurrentThreadUserTime());
      totalCPUTime = TimeUnit.NANOSECONDS.toMillis(threadMXBean.getCurrentThreadCpuTime());

      RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();
      elapsedTime = runtimeMXBean.getUptime();
    } catch(Error e) {
      // ThreadMXBean is not available in all environments
      // Specifically, AppEngine will throw variously SecurityErrors or
      // ClassNotFoundErrors if we try to access these classes
      userCPUTime = totalCPUTime = elapsedTime = java.lang.System.currentTimeMillis();
    }

    // user.self
    names.add("user.self");
    result.add( userCPUTime / MILLISECONDS_PER_SECOND );

    
    // sys.self
    names.add("sys.self");
    result.add( (totalCPUTime - userCPUTime) / MILLISECONDS_PER_SECOND );

    // elapsed
    // (wall clock time)
    names.add("elapsed");
    result.add(elapsedTime / MILLISECONDS_PER_SECOND);

    // AFAIK, we don't have any platform independent way of accessing
    // this info.

    // user.child
    names.add("user.child");
    result.add(0);

    // sys.child
    names.add("sys.child");
    result.add(0);

    result.setAttribute(Symbols.NAMES, names.build());
    result.setAttribute(Symbols.CLASS, StringVector.valueOf("proc_time"));
    return result.build();

  }


  @Internal
  public static String machine() {
    return "Unix";
  }
  
  @Internal
  public static void dirchmod(StringVector dir ) {
    // not supported
  }

  @Internal("Sys.chmod")
  public static boolean sysChmod(@Recycle String path, int mode, boolean useUmask) {
    // Not supported on our "platform" 
    // There are many cases where a Sys.chmod call would fail, so I think this 
    /// is a perfectly valid and complete implementation
    return false;
  }
  
  /**
   * ‘Sys.umask’ sets the ‘umask’ and returns the previous value: as a
   * special case ‘mode = NA’ just returns the current value.  It may
   * not be supported (when a warning is issued and ‘"0"’ is returned).
   * For more details see your OS's documentation on the system call
   * ‘umask’, e.g. ‘man 2 umask’
   */
  @Internal("Sys.umask")
  public static int sysChmod(int umask) {
    
    // Not supported on our "platform" 
    // There are many cases where a Sys.chmod call would fail, so I think this 
    /// is a perfectly valid and complete implementation
    return 0;
  }
  
  @Internal("system")
  public static SEXP system(@Current Context context, String command, int flag, SEXP stdin, SEXP stdout, SEXP stderr) throws IOException, InterruptedException {
    boolean invisible = (flag >= 20 && flag < 29);
    boolean minimized = (flag >= 10 && flag < 19);
    
    List<String> args = parseArgs(command);
    ProcessBuilder builder = new ProcessBuilder(args);
    
    FileObject workingDir = context.getSession().getWorkingDirectory();
    if(workingDir instanceof LocalFile) {
      File localDir = new File(workingDir.getURL().getFile());
      builder.directory(localDir);
    }
    Process process = builder.start();
    process.waitFor();
    
    int exitValue = process.exitValue();
    return new IntArrayVector(exitValue);
  }
  
  @VisibleForTesting
  static List<String> parseArgs(String commandLine) {
    List<String> terms = Lists.newArrayList();
    boolean dquoted = false;
    boolean squoted = false;
    char lastChar = 0;
    StringBuilder currentTerm = new StringBuilder();
    for(int i=0;i!=commandLine.length();++i) {
      char c = commandLine.charAt(i);
      if(!dquoted && !squoted && Character.isWhitespace(c)) {
        if(!Character.isWhitespace(lastChar)) {
          terms.add(currentTerm.toString());
          currentTerm.setLength(0);
        }       
      } else if(!squoted && c == '"') {
        dquoted = !dquoted;
      
      } else if(!dquoted && c == '\'') {
        squoted = !squoted;
        
      } else {
        currentTerm.append(c);
      }
      lastChar = c;
    }
    if(currentTerm.length() > 0) {
      terms.add(currentTerm.toString());
    }
    return terms;
  }
  
}
  
