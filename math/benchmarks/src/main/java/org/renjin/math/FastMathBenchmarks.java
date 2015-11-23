package org.renjin.math;

import org.apache.commons.math3.util.FastMath;
import org.openjdk.jmh.annotations.*;

import java.util.concurrent.TimeUnit;

/**
 * Benchmarks Apache Commons FastMath methods
 */
@State(Scope.Thread)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
public class FastMathBenchmarks {
  
  private double x;
  
  @Setup
  public void setup() {
    x = Math.random();
  }
  
  @Benchmark
  public double fastMathAbs() {
    return FastMath.abs(x);
  }
  
  @Benchmark
  public double mathAbs() {
    return Math.abs(x);
  }
  
  @Benchmark
  public double strictMathAbs() {
    return StrictMath.abs(x);
  }
}
