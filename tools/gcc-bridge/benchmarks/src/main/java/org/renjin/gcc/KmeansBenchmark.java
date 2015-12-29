
package org.renjin.gcc;


import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.profile.StackProfiler;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.renjin.gcc.runtime.DoublePtr;
import org.renjin.gcc.runtime.IntPtr;

import java.util.Random;
import java.util.concurrent.TimeUnit;

@State(Scope.Thread)
@BenchmarkMode({  Mode.AverageTime })
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.MILLISECONDS)
@Fork(value = 1, jvmArgsAppend = {
    "-XX:+UnlockDiagnosticVMOptions",
    "-XX:+TraceClassLoading",
    "-XX:+LogCompilation",
    "-XX:LogFile=kmeans.log",
    "-XX:+PrintAssembly"})
//@Fork(value = 1)
public class KmeansBenchmark {

  double[] a;
  int m = 3467;
  int n = 3467;
  double c[];
  int k = 25;
  int ic1[];
  int ic2[];
  int nc[];
  double an1[]; 
  double an2[];
  int ncp[];
  double d[];
  int itran[];
  int live[];
  int iter = 10;
  double wss[];
  int ifault[];
  
  
  @Setup(Level.Invocation)
  public void prepare() {
    a = new double[m * n];
    int ai = 0;
    // spread out points uniformly in k-space
    for(int column = 0; column < m; ++column) {
      for(int row = 0; row < n; ++row) {
        a[ai++] = ((double) row)/ ((double) n);
      }
    }
    // spread out the initial centers also uniformly
    int ci = 0;
    c = new double[m * k];
    for(int column = 0; column < m; ++column) {
      for(int row = 0; row < k; ++row) {
        c[ci++] = ((double) row) / ((double) k);
      }
    }
    
    ic1 = new int[m];
    ic2 = new int[m];
    nc = new int[k];
    an1 = new double[k];
    an2 = new double[k];
    ncp = new int[k];
    d = new double[m];
    itran = new int[k];
    live = new int[k];
    wss = new double[k];
    ifault = new int[1];
  }

  @Benchmark
  public double[] benchmark() {
    
    org.renjin.gcc.kmns.kmns_(
        new DoublePtr(a),
        m,
        n,
        new DoublePtr(c),
        k,
        new IntPtr(ic1),
        new IntPtr(ic2),
        new IntPtr(nc),
        new DoublePtr(an1),
        new DoublePtr(an2),
        new IntPtr(ncp),
        new DoublePtr(d),
        new IntPtr(itran),
        new IntPtr(live),
        iter,
        new DoublePtr(wss),
        new IntPtr(ifault));
    
    return wss;
  }

  public static void main(String[] args) throws RunnerException {
    Options opt = new OptionsBuilder()
        .include(KmeansBenchmark.class.getName() + ".*")
        .addProfiler(StackProfiler.class)
        .forks(1)
        .build();

    new Runner(opt).run();
  }
}