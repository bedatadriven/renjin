package org.renjin.eval;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Ordering;
import org.renjin.sexp.*;
import org.renjin.sexp.Vector;

import java.io.PrintStream;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static java.lang.System.out;

/**
 * Simple profiler intended to be used from the command line.
 * 
 * <p>The profiler can be enabled using the JVM flag -Drenjin.profile=true</p>
 */
public class Profiler {

  public static boolean ENABLED = Boolean.getBoolean("renjin.profile");

  private static final long MIN_LOOP_TIME_RECORD = TimeUnit.MILLISECONDS.toNanos(500);

  private static class FunctionProfile {
    private Symbol symbol;
    private long count;
    private long time;
    private long ownTime;
    private long bytesAllocated;
    private char type;
  }
  
  private static class CallTiming {
    private Symbol symbol;
    private char type;
    private CallTiming parent;
    private long startTime;
    private long childTime;
    private long bytesAllocated;
  }
  
  private static class LoopProfile {
    private FunctionCall call;
    private long time;
    private long iterations;
  }
  
  private static class LoopTiming {
    private FunctionCall call;
    private long startTime;
    private long time;
    private LoopTiming parent;
    private long expectedIterations;
    private long actualIterations;
    public CallTiming parentCall;
  }
  
  
  
  private static Map<Symbol, FunctionProfile> FUNCTION_PROFILES = new IdentityHashMap<>();
  
  private static Map<Symbol, FunctionProfile> TOP_LEVEL_PROFILES = new IdentityHashMap<>();
  
  private static List<LoopTiming> LOOP_TIMINGS = new ArrayList<>();
  
  private static CallTiming CURRENT = null;
  private static LoopTiming CURRENT_LOOP;
  
  private static long LOOP_TIME = 0;
  
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
  public static void functionStart(Symbol functionName, char type) {
    CallTiming timing = new CallTiming();
    timing.symbol = functionName;
    timing.parent = CURRENT;
    timing.startTime = System.nanoTime();
    timing.type = type;

    CURRENT = timing;
  }
  
  public static void functionStart(Symbol functionName, Function functionExpr) {

    if(functionExpr instanceof Closure) {
      functionStart(functionName, 'R');
    } else {
      functionStart(functionName, 'B');
    }
  }
  
  public static void loopStart(FunctionCall call, Vector elements) {
    LoopTiming timing = new LoopTiming();
    timing.call = call;
    timing.parentCall = CURRENT;
    timing.parent = CURRENT_LOOP;
    timing.startTime = System.nanoTime();
    timing.expectedIterations = elements.length();
    
    CURRENT_LOOP = timing;
  }
  

  /**
   * Reports the end of a function call
   */
  public static void functionEnd() {
    long endTime = System.nanoTime();
    long time = endTime - CURRENT.startTime;
    
    // Update the "flat" profile for the given
    // function
    updateMap(FUNCTION_PROFILES, time);
    
    // If this is a top level call, save
    if(CURRENT.parent == null) {
      updateMap(TOP_LEVEL_PROFILES, time);
    }
    
    // If this function is a nested function call, then 
    // mark our parent as the current function, and add our run time
    // to our parent's child time count.
    CURRENT = CURRENT.parent;
    if(CURRENT != null) {
      CURRENT.childTime += time;
    }
  }

  private static void updateMap(Map<Symbol, FunctionProfile> map, long time) {
    FunctionProfile profile = map.get(CURRENT.symbol);
    if(profile == null) {
      profile = new FunctionProfile();
      profile.symbol = CURRENT.symbol;
      profile.type = CURRENT.type;
      map.put(profile.symbol, profile);
    }
    profile.time += time;
    profile.ownTime += (time - CURRENT.childTime);
    profile.count++;
    profile.bytesAllocated += CURRENT.bytesAllocated;
  }

  public static void loopEnd(int iterations) {
    long endTime = System.nanoTime();
    long time = endTime - CURRENT_LOOP.startTime;
    
    LOOP_TIME += time;
    
    if(time > MIN_LOOP_TIME_RECORD) {
      CURRENT_LOOP.time = time;
      CURRENT_LOOP.actualIterations = iterations;
      LOOP_TIMINGS.add(CURRENT_LOOP);
    }
    
    CURRENT_LOOP = CURRENT_LOOP.parent;
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

  public static void dumpTotalRunningTime() {
    long totalRunningTime = (System.nanoTime() - startTime);

    double seconds = TimeUnit.NANOSECONDS.toSeconds(totalRunningTime);
    double minutes = seconds / 60d;
    
    out.println("Completed in " + minutes + " minutes");

  }
  
  /**
   * Dumps the results of the profile to the given PrintStream.
   */
  public static void dump(PrintStream out) {
    
    long totalRunningTime = (System.nanoTime() - startTime);

    printTopFunctions(out, totalRunningTime);
    printFunctionTimings(out, totalRunningTime);
    printLoopTimings(out);
  }


  private static void printTopFunctions(PrintStream out, final double totalRunningTime) {

    List<FunctionProfile> profiles = Lists.newArrayList(TOP_LEVEL_PROFILES.values());
    Collections.sort(profiles, Ordering.natural().onResultOf(new com.google.common.base.Function<FunctionProfile, Long>() {
      @Override
      public Long apply(FunctionProfile input) {
        return input.time;
      }
      }).reverse());
    Iterables.filter(profiles, new Predicate<FunctionProfile>() {
      @Override
      public boolean apply(FunctionProfile input) {
        return (((double) input.time) / totalRunningTime) > 0.01;
      }
      });

    out.println();
    out.println("TOP-LEVEL FUNCTION CALLS");
    out.println("==================");
    out.println();
    out.println(String.format("  %-25s%5s%10s%10s%4s%10s", "Function", "Count", "Time", "Own Time", "%", "kb Alloc"));

    printProfiles(out, totalRunningTime, Iterables.limit(profiles, 10));
  }


  private static void printFunctionTimings(PrintStream out, double totalRunningTime) {

    List<FunctionProfile> profiles = Lists.newArrayList(FUNCTION_PROFILES.values());
    Collections.sort(profiles, Ordering.natural().onResultOf(new com.google.common.base.Function<FunctionProfile, Long>() {
      @Override
      public Long apply(FunctionProfile input) {
        return input.ownTime;
      }
      }).reverse());


    out.println();
    out.println("FUNCTION CALLS BY OWN TIME");
    out.println("==========================");
    out.println();
    out.println(String.format("  %-25s%5s%10s%10s%4s%10s", "Function", "Count", "Time", "Own Time", "%", "kb Alloc"));

    printProfiles(out, totalRunningTime, profiles);
  }

  private static void printProfiles(PrintStream out, double totalRunningTime, Iterable<FunctionProfile> profiles) {
    for (FunctionProfile profile : profiles) {
      out.println(String.format("%c %-25s%5d%10d%10d%3.0f%%%10s",
          profile.type,
          profile.symbol.getPrintName(),
          profile.count,
          TimeUnit.NANOSECONDS.toMillis(profile.time),
          TimeUnit.NANOSECONDS.toMillis(profile.ownTime),
          ((double)profile.ownTime) / totalRunningTime * 100d,
          formatAlloc(profile.bytesAllocated)));
    }
  }

  private static void printLoopTimings(PrintStream out) {
    out.println();
    out.println("LONG RUNNING LOOPS");
    out.println("==================");
    
    List<LoopTiming> loops = Lists.newArrayList(LOOP_TIMINGS);
    Collections.sort(loops, Ordering.<Long>natural().onResultOf(new com.google.common.base.Function<LoopTiming, Long>() {
      @Override
      public Long apply(LoopTiming input) {
        return input.time;
      }
    }));


    out.println(String.format("%-25s%10s%10s", "Function", "Iterations", "Time"));

    for (LoopTiming loop : loops) {
      out.println(String.format("%-25s%10d%10d",
          loop.parentCall.symbol.getPrintName(),
          loop.actualIterations,
          TimeUnit.NANOSECONDS.toMillis(loop.time)));
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
