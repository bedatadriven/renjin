package org.renjin.gnur;

import org.junit.Test;
import org.renjin.gcc.Gcc;
import org.renjin.gcc.gimple.GimpleCompilationUnit;
import org.renjin.gcc.link.LinkContext;
import org.renjin.gnur.xform.CallGraph;

import java.io.File;
import java.util.Collections;

/**
 * Verify that we can transform global variables a 
 */
public class GlobalTransformTest {
  
  @Test
  public void simple() throws Exception {

    File source = new File("src/test/resources/org/renjin/gnur/globals.c");
    if(!source.exists()) {
      throw new AssertionError("missing source");
    }
    
    File includeDir = new File("src/main/resources/org/renjin/gnur/include");
    
    Gcc gcc = new Gcc();
    gcc.extractPlugin();
    
    GimpleCompilationUnit unit = gcc.compileToGimple(source, "-I", includeDir.getAbsolutePath());
    
    System.out.println(unit);

    LinkContext context = new GnurLinkContextProvider().get();
    CallGraph callGraph = new CallGraph(context, Collections.singletonList(unit));
    callGraph.dumpGraph("target/callgraph.dot");
    
  }
  
}
