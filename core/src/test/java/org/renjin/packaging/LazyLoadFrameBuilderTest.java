package org.renjin.packaging;

import java.io.File;

import org.junit.Test;
import org.renjin.eval.Context;
import org.renjin.parser.RParser;
import org.renjin.primitives.packaging.Namespace;
import org.renjin.primitives.packaging.NamespaceDef;
import org.renjin.sexp.Closure;
import org.renjin.sexp.Symbol;

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;

import com.google.common.io.Files;

public class LazyLoadFrameBuilderTest {

  @Test
  public void test() throws Exception {
    
    Context tlContext = Context.newTopLevelContext();
    Namespace ns = tlContext.getNamespaceRegistry().createNamespace(new NamespaceDef(), "testns");
    
    Context ctx = tlContext.beginEvalContext(ns.getNamespaceEnvironment());
    ctx.evaluate(RParser.parseSource("f <- function(x) x*x*42\n"));
    
    File envFile = File.createTempFile("nstest", ".RData");
    envFile.deleteOnExit();
    
    LazyLoadFrameBuilder builder = new LazyLoadFrameBuilder(tlContext);
    builder.outputTo(envFile);
    builder.build(ns.getNamespaceEnvironment());
    
    // now reload into a new context
    tlContext = Context.newTopLevelContext();
    tlContext.getNamespaceRegistry().createNamespace(new NamespaceDef(), "testns");
    
    LazyLoadFrame loader = new LazyLoadFrame(tlContext, Files.newInputStreamSupplier(envFile));
    Closure f = (Closure)loader.get(Symbol.get("f"));
    
    assertThat(f.getEnclosingEnvironment(), equalTo(tlContext.getNamespaceRegistry().getNamespace("testns").getNamespaceEnvironment()));
    
  }
  
}
