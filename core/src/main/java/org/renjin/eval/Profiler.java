package org.renjin.eval;

import com.google.common.collect.Lists;
import com.google.common.collect.Ordering;
import org.renjin.sexp.Symbol;

import java.io.PrintStream;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Simple profiler intended to be used from the command line.
 * 
 * <p>The profiler can be enabled using the JVM flag -Drenjin.profile=true</p>
 */
public class Profiler {

  public static final boolean ENABLED = Boolean.getBoolean("renjin.profile");

  private static class FunctionProfile {
    private Symbol symbol;
    private long count;
    private long time;
    private long ownTime;
    private long bytesAllocated;
  }
  
  private static class CallTiming {
    private Symbol symbol;
    private CallTiming parent;
    private long startTime;
    private long childTime;
    private long bytesAllocated;
  }
  
  private static Map<Symbol, FunctionProfile> FUNCTION_PROFILES = new IdentityHashMap<>();
  
  private static CallTiming CURRENT = null;
  
  private static long startTime = System.nanoTime();

  /**
   * Clears any existing function profiles, and restarts the 
   * program timer.
   */
  public static void reset() {
    FUNCTION_PROFILES.clear();
    startTime = System.nanoTime();
  }

  /**
   * Reports the start of a function call
   * @param functionName the name of the function being called
   */
  public static void functionStart(Symbol functionName) {
    CallTiming timing = new CallTiming();
    timing.symbol = functionName;
    timing.parent = CURRENT;
    timing.startTime = System.nanoTime();

    CURRENT = timing;
  }

  /**
   * Reports the end of a function call
   */
  public static void functionEnd() {
    long endTime = System.nanoTime();
    long time = endTime - CURRENT.startTime;
    
    // Update the "flat" profile for the given
    // function
    FunctionProfile profile = FUNCTION_PROFILES.get(CURRENT.symbol);
    if(profile == null) {
      profile = new FunctionProfile();
      profile.symbol = CURRENT.symbol;
      FUNCTION_PROFILES.put(profile.symbol, profile);
    }
    profile.time += time;
    profile.ownTime += (time - CURRENT.childTime);
    profile.count++;
    profile.bytesAllocated += CURRENT.bytesAllocated;
    
    // If this function is a nested function call, then 
    // mark our parent as the current function, and add our run time
    // to our parent's child time count.
    CURRENT = CURRENT.parent;
    if(CURRENT != null) {
      CURRENT.childTime += time;
    }
  }

  /**
   * Records a memory allocation attempt
   * @param size the size, in bits of the array elements allocated
   * @param length the number of elements
   */
  public static void memoryAllocated(int size, int length) {
    if(CURRENT != null) {
      CURRENT.bytesAllocated += (length * (size/8));
    }
  }

  /**
   * Dumps the results of the profile to the given PrintStream.
   */
  public static void dump(PrintStream out) {
    
    long totalRunningTime = (System.nanoTime() - startTime);

    List<FunctionProfile> profiles = Lists.newArrayList(FUNCTION_PROFILES.values());
    Collections.sort(profiles, Ordering.natural().onResultOf(new com.google.common.base.Function<FunctionProfile, Long>() {
      @Override
      public Long apply(FunctionProfile input) {
        return input.ownTime;
      }
    }).reverse());
    
    out.println();
    out.println(String.format("%-25s%5s%10s%10s%4s%10s", "Function", "Count", "Time", "Own Time", "%", "kb Alloc"));
    
    for (FunctionProfile profile : profiles) {
      out.println(String.format("%-25s%5d%10d%10d%3.0f%%%10s",
          profile.symbol.getPrintName(),
          profile.count,
          TimeUnit.NANOSECONDS.toMillis(profile.time),
          TimeUnit.NANOSECONDS.toMillis(profile.ownTime),
          ((double)profile.ownTime) / (double)totalRunningTime * 100d,
          formatAlloc(profile.bytesAllocated)));
    }
  }

  private static String formatAlloc(long bytes) {
    if(bytes < 1024) {
      return "";
    } 
    double kb = bytes / 1024d;
    if(kb < 1024) {
      return String.format("%.1f kb", kb);
    } 
    double mb = kb / 1024d;
    if(mb < 1024) {
      return String.format("%.1f mb", mb);
    }
    double gb = mb / 1024d;
    return String.format("%.1f gb", gb);
  }
}
