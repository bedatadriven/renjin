package org.renjin.test;

import org.renjin.sexp.IntVector;

import java.util.concurrent.Callable;

/**
 *  Verify that multiple Renjin sessions can run concurrently 
 *  when the C code contains references to globals
 */
public class GlobalConcurrencyTest {
  
  
  private class DotCallTask implements Callable<IntVector> {


    @Override
    public IntVector call() throws Exception {
      return null;
    }
  }
}
